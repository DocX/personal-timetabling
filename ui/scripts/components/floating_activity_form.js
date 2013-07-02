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
    PanelBase = require('components/panel_base'),
    ActivityRepeatingForm = require('components/activity_repeating_form'),
    NestedDomainForm = require('components/nested_domain_form'),
    SimpleDomainForm = require('components/simple_domain_form');
    
return PanelBase.extend({

	template: 
		"<div>" +
			"<label>How long activity do you want to allocate</label>" +
			"At least <input type='number' min='1' name='duration_min' class='input-mini' /> optimal <input type='number' min='0' name='duration_max' class='input-mini'/> min" +
			"<label>In range from</label>" +
			"<input type='text' name='range_from' class='datetime fill-width' />" +
			"<label>to deadlinee</label>" +
			"<input type='text' name='range_to' class='datetime fill-width' />" +
			"<p>TIP: You can move first occurence box to setup date range of acitivity</p>" +

			"<legend><strong>Time domain</strong></legend>" +
			"<a href='#' data-role='complex-domain-switch'>Switch to complex editor</a>" +
			"<div class='domain-form-container'>" +
				"<div class='domain-form'></div>" +
			"</div>" +

			"<legend><strong>Repeating</strong></legend>" +
			"<div class='repeat-form-controls'></div>" +

		"</div>",

	// 1 duration input unit in seconds
	duration_unit: 60,

	events: {
		'change [name=duration_min]': 'set_duration_min',
		'change [name=duration_max]': 'set_duration_max',
		'click [name^=weekday_]': 'set_weekdays',
		'click [name^=time_]': 'set_hours',
		'click a[data-role=complex-domain-switch]': 'switch_to_complex_domain_editor',
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

				// update occurance model start
				this.activity.first_occurance().set('start', moment.asUtc(this.$from_input.datetimepicker('getDate')));

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
				this.activity.set('range_duration', moment.asUtc(this.$to_input.datetimepicker('getDate')).diff(
					 moment.asUtc(this.$from_input.datetimepicker('getDate')), 'seconds'
					));
			}, this),
			onSelect: _.bind(function (selectedDateTime){
				this.$from_input.datetimepicker('option', 'maxDate', this.$to_input.datetimepicker('getDate') );

			}, this),
			firstDay: 1
		});


		this.activity = new FloatingActivityStub();
		this.activity.first_occurance().set('start', this.options.activities_view.get_date_aligned_to_view_grid(
			this.activity.first_occurance().get('start')
		));

		this.activity.first_occurance().on('change', this.update_from_model , this);
		this.update_from_model(this.activity.first_occurance());


		// initialize repeating definition form
		this.repeating_form = new ActivityRepeatingForm({
			el: this.$el.find('.repeat-form-controls')
		});

		this.listenTo(this.repeating_form, 'change', this.update_repeating_to_model);
		this.activity.on('change:repeating', this.update_repeating_from_model, this);

		this.set_repeat_period_unit();
		this.options.activities_view.on('geometry_changed', this.set_repeat_period_unit, this);	

		// domain form
		this.domain_form = new SimpleDomainForm({
			el: this.$el.find('.domain-form')
		});
		//this.listenTo(this.domain_form.model, 'changed', this.set_domain);
		this.listenTo(this.domain_form, 'change', this.set_domain);
		this.set_domain();

	},

	update_from_model: function(m) {
		var definition = this.activity.get('definition');

		this.$from_input.datetimepicker('setDate', moment.asLocal(definition.from).toDate());
		this.$to_input.datetimepicker('setDate', moment.asLocal(definition.to).toDate());
		this.$duration_min_input.val(definition.duration_min / this.duration_unit );
		this.$duration_max_input.val(m.get('duration')  / this.duration_unit )

		// refresh ranges intervals
		this.refresh_ranges_view();
	},

	set_duration_min: function() {
		this.activity.set('duration_min', this.$duration_min_input.val() * this.duration_unit);
	},

	set_duration_max: function(){
		this.activity.set('duration_max', this.$duration_max_input.val() * this.duration_unit);
		this.activity.first_occurance().set('duration', this.$duration_max_input.val() * this.duration_unit);
	},

	set_repeat_period_unit: function() {
		this.repeating_form.set_period_unit(this.options.activities_view.get_view_geometry_name());
	},

	update_repeating_to_model: function() {
		var repeating = this.repeating_form.get_repeating_def();
		this.activity.set('repeating', repeating );

		//this.$el.find('.repeating-tip').toggle(repeating != false);
	},

	update_repeating_from_model: function(m) {
		this.repeating_form.update_from(m.get('repeating'));

		// refresh ranges intervals
		this.refresh_ranges_view();
	},

	refresh_ranges_view: function() {
		this.remove_intervals_view();

		this.ranges_intervals = this.options.activities_view.calendar.display_intervals(
			this.activity.get_ranges_intervals(),
			function(box) {box.addClass('domain-highlight')}
		);
	},

	remove_intervals_view: function() {
		if (this.ranges_intervals) {
			for (var i = this.ranges_intervals.length - 1; i >= 0; i--) {
				this.ranges_intervals[i].remove();
			};
		}
	},

	remove: function() {
		this.remove_intervals_view();

		PanelBase.prototype.remove.apply(this); 
	},

	set_domain: function() {
		// set domain from domain form to activity model
		this.activity.set('domain_template', this.domain_form.model );
		//this.activity.trigger('change:domain_template');
	},

	switch_to_complex_domain_editor: function() {
		var domain = this.domain_form.model;

		this.domain_form.remove();

		this.$el.find('.domain-form-container').append("<div class='domain-form'></div>");
		this.domain_form = new NestedDomainForm({
			el: this.$el.find('.domain-form'),
			// TODO fix, nested form do not accepts model
			model: domain
		});

		// hide switch
		this.$el.find('a[data-role=complex-domain-switch]').hide();

		this.listenTo(this.domain_form, 'change', this.set_domain);
	}
	
});

});