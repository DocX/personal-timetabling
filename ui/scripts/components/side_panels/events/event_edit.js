// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    ActivityOccurance = require('models/activity_occurance'),
    Interval = require('models/interval'),
    PanelBase = require('components/side_panels/panel_base'),
    NestedDomainForm = require('components/domain_template_forms/nested_domain_form'),
    DomainTemplate = require('models/domain_template');

return PanelBase.extend({

	template: 
		"<div>" +
			"<p><strong>Event detail</strong> <a class='' style='float:right' data-role='cancel_btn'>Cancel</a></p>" +
			"<label>Name</label>" +
			"<input type='text' name='name' class='fill-width' />" +
			"<label>Activity</label>" +
			"<p><a href='#' data-role='activity-name'></a></p>" +

			"<legend>Duration</legend>" +
			"<p>At least <input type='number' min='1' name='duration_min' class='input-mini' /> optimal <input type='number' min='0' name='duration_max' class='input-mini'/> min</p>" +

			"<legend>Event scheduling window</legend>" +
			"<input type='text' name='range_from' class='datetime fill-width' />" +
			"<label>to</label>" +
			"<input type='text' name='range_to' class='datetime fill-width' />" +

			"<legend><strong>Time domain</strong> (green area)</legend>" +
			"<div class='domain-form-container'>" +
				"<div class='domain-form'></div>" +
			"</div>" + 

			"<div>" +
			"<input type='button' class='btn btn-primary fill-width' data-role='save' value='Save changes'/>" +
			"<input type='button' class='btn btn-danger fill-width' data-role='delete' value='Delete event'/>" +
			"</div>" +

		"</div>",

	// 1 duration input unit in seconds
	duration_unit: 60,

	events: {
		'click input[data-role=save]': 'save',
		'click a[data-role=cancel_btn]': 'remove',
		'click input[data-role=delete]': 'delete',
	},

	initialize: function() {
		this.$el.append(this.template);

		// initialize datetimepickers

		this.$name_input = this.$el.find('[name=name]');
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
				var start_old = this.model.get('schedule_since');
				var end =  moment.asUtc(this.$to_input.datetimepicker('getDate'));
				var dur = end.diff(start_old,'seconds');
				end = start_new.clone().add(dur, 'seconds');
				this.$to_input.datetimepicker('setDate', moment.asLocal(end).toDate());

				this.model.set('schedule_since',start_new);
				this.model.set('schedule_deadline', end);

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
				this.model.set('schedule_deadline', moment.asUtc(this.$to_input.datetimepicker('getDate')));
				this.refresh_ranges_view();

			}, this),
			onSelect: _.bind(function (selectedDateTime){
				this.$from_input.datetimepicker('option', 'maxDate', this.$to_input.datetimepicker('getDate') );

			}, this),
			firstDay: 1
		});

		// domain form

		this.domain_model = new DomainTemplate();
		this.domain_model.set('domain_attributes', this.model.get('domain_attributes'));
		this.domain_form = new NestedDomainForm({
			el: this.$el.find('.domain-form'),
			model: this.domain_model.get('domain_attributes')
		});

		this.listenTo(this.domain_form, 'change', this.set_domain);

		this.update_from_model();

		this.listenTo(this.options.activities_view.calendar,  'columns_updated', this.refresh_domains_view);
		this.set_domain();

		//this.listenTo(this.domain_form.model, 'changed', this.set_domain);

	},

	update_from_model: function() {

		this.$name_input.val(this.model.get('name'));
		this.$from_input.datetimepicker('setDate', moment.asLocal(this.model.get('schedule_since')).toDate());
		this.$to_input.datetimepicker('setDate', moment.asLocal(this.model.get('schedule_deadline')).toDate());
		this.$duration_min_input.val(this.model.get('min_duration') / this.duration_unit );
		this.$duration_max_input.val(this.model.get('max_duration')  / this.duration_unit )

		// refresh ranges intervals
		this.refresh_ranges_view();
	},

	refresh_ranges_view: function() {
		this.remove_intervals_view(this.ranges_intervals);

		var from = moment.asUtc(this.$from_input.datetimepicker('getDate'));
		var to = moment.asUtc(this.$to_input.datetimepicker('getDate'));
		this.ranges_intervals = this.options.activities_view.calendar.display_intervals(
			[new Interval({start: from, end: to})],
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

      	this.update_to_model();

      	this.domain_preview = this.model.fetchPreviewDomainIntervals(
      		range.start, 
      		range.end);

      	this.domain_preview.xhr.success(
      		_.bind(function() {
      			if (this.removed) {return; }
				this.remove_intervals_view(this.domain_model_preview);
				this.domain_model_preview = this.options.activities_view.calendar.display_intervals(
					this.domain_preview.collection.models,
					function(box) {box.addClass('domain-highlight ')}
				);
			}, this)
		);
	},

	remove: function() {
		this.domain_preview && this.domain_preview.xhr.abort();
		this.remove_intervals_view(this.domain_model_preview);
		this.remove_intervals_view(this.ranges_intervals);

		PanelBase.prototype.remove.apply(this); 
	},

	set_domain: function() {
		// set domain from domain form to activity model
		this.model.set('domain_attributes', this.domain_form.model );
		//this.activity.trigger('change:domain_template');

		// reset domain view

		this.refresh_domains_view();
	},

	update_to_model: function() {
		// load from form
		this.model.set({
			schedule_since: moment.asUtc(this.$from_input.datetimepicker('getDate')),
			schedule_deadline: moment.asUtc(this.$to_input.datetimepicker('getDate')),
			domain_attributes: this.domain_form.model,
			name: this.$name_input.val(),
			min_duration: this.$duration_min_input.val() * this.duration_unit,
			max_duration: this.$duration_max_input.val() * this.duration_unit,
		});
	},

	save: function() {
		this.update_to_model();

		this.model.save()
		.success(_.bind(function() {
			this.options.activities_view.reload_activities();
			this.remove();
		},this))
		.error(_.bind(function() {
			alert('failed to save changes to event');
		}));
	},

	delete: function() {
		if (confirm('Are you sure you want delete event + ' + this.model.get('name'))) {
			
			this.model.destroy();
			this.remove();
		}
	}
	
});

});