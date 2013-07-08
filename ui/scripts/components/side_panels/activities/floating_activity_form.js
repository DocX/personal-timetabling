// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    FloatingActivityStub = require('models/floating_activity_stub'),
    ActivityOccurance = require('models/activity_occurance'),
    PanelBase = require('components/side_panels/panel_base'),
    ActivityRepeatingForm = require('components/side_panels/activities/activity_repeating_form'),
    NestedDomainForm = require('components/domain_template_forms/nested_domain_form'),
    DomainTemplate = require('models/domain_template');

return PanelBase.extend({

	template: 
		"<div>" +
			"<label>How long activity do you want to allocate</label>" +
			"At least <input type='number' min='1' name='duration_min' class='input-mini' /> optimal <input type='number' min='0' name='duration_max' class='input-mini'/> min" +
			"<legend><strong>Event occurrences windows</strong> (blue areas)</legend>" +
			"<p>Define event window for first occurrence and their repeating. Think about this the same way as when you are creating fixed events, only with exception you don't know exact time for them.</p>" +
			"<input type='text' name='range_from' class='datetime fill-width' />" +
			"<label>to</label>" +
			"<input type='text' name='range_to' class='datetime fill-width' />" +
			"<legend><strong>Repeating</strong></legend>" +
			"<div class='repeat-form-controls'></div>" +

			"<legend><strong>At times</strong> (green area)</legend>" +
			"<p>Here define times when event occurrences can be allocated, or when they can not. Green area will be cropped by blue area for each event occurrence defined above." +
			"<div class='domain-form-container'>" +
				"<div class='domain-form'></div>" +
			"</div>" + 



		"</div>",

	// 1 duration input unit in seconds
	duration_unit: 60,

	events: {
		'change [name=duration_min]': 'set_duration_min',
		'change [name=duration_max]': 'set_duration_max',
		'click [name^=weekday_]': 'set_weekdays',
		'click [name^=time_]': 'set_hours',
	},

	default_domain: function() {
		return {
			type: 'stack',
			data: {
				actions: [
					{
						action: 'mask',
						// day hours 7am-6pm
						domain: {
							type: 'boundless',
							data: {
								from: '2013-07-05T07:00Z',
								duration: {duration: 660, unit: 'minute'},
								period: {duration:1,unit:'day'}
							}
						}
					},
					{
						action: 'add',
						// working days
						domain: {
							type: 'boundless',
							data: {
								from: '2013-07-01T00:00Z',
								duration: {duration: 5, unit: 'day'},
								period: {duration:1,unit:'week'}
							}
						}
					},
				]
			}
		}
	},

	initialize: function() {
		this.$el.append(this.template);

		// initialize datetimepickers

		this.$from_input = this.$el.find('[name=range_from]');
		this.$to_input = this.$el.find('[name=range_to]');
		this.$duration_min_input = this.$el.find('[name=duration_min]');
		this.$duration_max_input = this.$el.find('[name=duration_max]');

		this.$from_input.datetimepicker({ 
			onClose: _.bind(function(dateText, inst) {
				if (this.$to_input.val() != '') {
					var testStartDate = this.$from_input.datetimepicker('getDate');
					var testEndDate = this.$to_input.datetimepicker('getDate');
					if (testStartDate > testEndDate)
						this.$to_input.datetimepicker('setDate', testStartDate);
				}
				else {
					this.$to_input.val(dateText);
				}

				// update model
				// get duration
				var start_new = moment.asUtc(this.$from_input.datetimepicker('getDate'));
				var start_old = this.activity.get('from');
				var end =  moment.asUtc(this.$to_input.datetimepicker('getDate'));
				var dur = end.diff(start_old,'seconds');
				end = start_new.clone().add(dur, 'seconds');
				this.$to_input.datetimepicker('setDate', moment.asLocal(end).toDate());

				this.activity.set('from',start_new);
				this.activity.set('to', end);

				this.refresh_ranges_view();

				// move view to be visible
				this.options.activities_view.show_date(moment.asUtc(this.$from_input.datetimepicker('getDate')))
			}, this),
			onSelect: _.bind(function (selectedDateTime){
				this.$to_input.datetimepicker('option', 'minDate', this.$from_input.datetimepicker('getDate') );
			}, this),
			firstDay: 1,
		});

		this.$to_input.datetimepicker({ 
			onClose: _.bind(function(dateText, inst) {
				if (this.$from_input.val() != '') {
					var testStartDate = this.$from_input.datetimepicker('getDate');
					var testEndDate = this.$to_input.datetimepicker('getDate');
					if (testStartDate > testEndDate)
						this.$from_input.datetimepicker('setDate', testEndDate);
				}
				else {
					this.$from_input.val(dateText);
				}

				// compute range duration and set to activity
				this.activity.set('to', moment.asUtc(this.$to_input.datetimepicker('getDate')));
				this.refresh_ranges_view();

			}, this),
			onSelect: _.bind(function (selectedDateTime){
				this.$from_input.datetimepicker('option', 'maxDate', this.$to_input.datetimepicker('getDate') );

			}, this),
			firstDay: 1
		});

		// domain form
		this.domain_model = new DomainTemplate();
		this.domain_model.set('domain_attributes', this.default_domain());
		this.domain_form = new NestedDomainForm({
			el: this.$el.find('.domain-form'),
			model: this.domain_model.get('domain_attributes')
		});


		this.activity = new FloatingActivityStub();
		this.update_from_model(this.activity);


		// initialize repeating definition form
		this.repeating_form = new ActivityRepeatingForm({
			el: this.$el.find('.repeat-form-controls')
		});

		this.listenTo(this.repeating_form, 'change', this.update_repeating_to_model);
		//this.listenTo(this.activity, 'change', this.update_repeating_from_model);

		this.listenTo(this.options.activities_view.calendar,  'columns_updated', this.refresh_domains_view);

		//this.listenTo(this.domain_form.model, 'changed', this.set_domain);
		this.listenTo(this.domain_form, 'change', this.set_domain);
		this.set_domain();

	},

	update_from_model: function(acitivity) {
		var definition = this.activity.get('definition');

		this.$from_input.datetimepicker('setDate', moment.asLocal(acitivity.get('from')).toDate());
		this.$to_input.datetimepicker('setDate', moment.asLocal(acitivity.get('to')).toDate());
		this.$duration_min_input.val(acitivity.get('duration_min') / this.duration_unit );
		this.$duration_max_input.val(acitivity.get('duration_max')  / this.duration_unit )

		// refresh ranges intervals
		this.refresh_ranges_view();
	},

	set_duration_min: function() {
		this.activity.set('duration_min', this.$duration_min_input.val() * this.duration_unit);
	},

	set_duration_max: function(){
		this.activity.set('duration_max', this.$duration_max_input.val() * this.duration_unit);
	},

	update_repeating_to_model: function() {
		var repeating = this.repeating_form.get_repeating_def();
		this.activity.set('repeating', repeating );

		this.refresh_ranges_view();
	},

	update_repeating_from_model: function(m) {
		this.repeating_form.update_from(m.get('repeating'));

		// refresh ranges intervals
		this.refresh_ranges_view();
	},

	refresh_ranges_view: function() {
		this.remove_intervals_view(this.ranges_intervals);

		this.ranges_intervals = this.options.activities_view.calendar.display_intervals(
			this.activity.get_ranges_intervals(),
			function(box) {box.addClass('domain-highlight floating-activity-form')}
		);

		
	},

	remove_intervals_view: function(intervals) {
		if (intervals) {
			for (var i = intervals.length - 1; i >= 0; i--) {
				intervals[i].remove();
			};
		}
	},

	refresh_domains_view: function() {
		// gets display date range
      	var range = this.options.activities_view.calendar.showing_dates();

      	this.domain_model.fetchPreviewIntervals(
      		range.start, 
      		range.end)
      	.success(
      		_.bind(function() {
				this.remove_intervals_view(this.domain_model_preview);
				this.domain_model_preview = this.options.activities_view.calendar.display_intervals(
					this.domain_model.preview_intervals_collection.models,
					function(box) {box.addClass('domain-highlight ')}
				);this.domain_model_preview
			}, this)
		);
	},

	remove: function() {
		this.remove_intervals_view(this.domain_model_preview);
		this.remove_intervals_view(this.ranges_intervals);

		PanelBase.prototype.remove.apply(this); 
	},

	set_domain: function() {
		// set domain from domain form to activity model
		this.activity.set('domain_template', this.domain_form.model );
		//this.activity.trigger('change:domain_template');

		// reset domain view

		this.refresh_domains_view();
	},
	
});

});