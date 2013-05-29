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
    ActivityRepeatingForm = require('components/activity_repeating_form');
    
return PanelBase.extend({

	template: 
		"<div>" +
			"<label>How long activity do you want to allocate</label>" +
			"<input type='number' min='1' name='duration_middle' class='input-small' /> ± <input type='number' min='0' name='duration_interval' class='input-small'/> minutes" +
			"<label>In range from</label>" +
			"<input type='text' name='range_from' class='datetime fill-width' />" +
			"<label>to deadlinee</label>" +
			"<input type='text' name='range_to' class='datetime fill-width' />" +
			"<p>TIP: You can move first occurence box to setup date range of acitivity</p>" +


			"<label>In times</label>" +
			"<div class='time_def form-inline'>" +
				"<label><input type='checkbox' name='time_morning' value='add' >Morning 8 - 11</label> "+
				"<label><input type='checkbox' name='time_noon' value='add' >Noon 11 - 13</label> "+
				"<label><input type='checkbox' name='time_afternoon' value='add' >Afternoon 13 - 16</label> "+
				"<label><input type='checkbox' name='time_evening' value='add' >Evening 16 - 20</label> "+
				"<label><input type='checkbox' name='time_lateevening' value='add' >Late evening 20 - 24</label> "+
				"<label><input type='checkbox' name='time_night' value='add' >Night 0 - 8</label>"+
			"</div>" +

			"<label>Weekdays</label>" +
			"<div class='weekdays_def form-inline'>" +
				"<label><input type='checkbox' name='weekday_1' value='add' >Mon</label> "+
				"<label><input type='checkbox' name='weekday_2' value='add' >Tue</label> "+
				"<label><input type='checkbox' name='weekday_3' value='add' >Wed</label> "+
				"<label><input type='checkbox' name='weekday_4' value='add' >Thu</label> "+
				"<label><input type='checkbox' name='weekday_5' value='add' >Fri</label> "+
				"<label><input type='checkbox' name='weekday_6' value='add' >Sat</label> "+
				"<label><input type='checkbox' name='weekday_7' value='add' >Sun</label> "+
			"</div>"+

			"<div class='repeat-form-controls'></div>" +

		"</div>",

	// 1 duration input unit in seconds
	duration_unit: 60,

	events: {
		'change [name=duration_interval]': 'set_duration_interval',
		'change [name=duration_middle]': 'set_duration_middle',
		'click [name^=weekday_]': 'set_weekdays',
		'click [name^=time_]': 'set_hours',
	},

	initialize: function() {
		this.$el.append(this.template);

		// initialize datetimepickers

		this.$from_input = this.$el.find('[name=range_from]');
		this.$to_input = this.$el.find('[name=range_to]');
		this.$duration_middle_input = this.$el.find('[name=duration_middle]');
		this.$duration_interval_input = this.$el.find('[name=duration_interval]');

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

		this.listenTo(this.repeating_form, 'changed', this.update_repeating_to_model);
		this.activity.on('change:repeating', this.update_repeating_from_model, this);

		this.set_repeat_period_unit();
		this.options.activities_view.on('geometry_changed', this.set_repeat_period_unit, this);	
	},

	update_from_model: function(m) {
		var definition = this.activity.get('definition');

		this.$from_input.datetimepicker('setDate', moment.asLocal(definition.from).toDate());
		this.$to_input.datetimepicker('setDate', moment.asLocal(definition.to).toDate());
		this.$duration_middle_input.val(m.get('duration') / this.duration_unit );
		this.$duration_interval_input.val(this.activity.get('duration_interval') / this.duration_unit )

		var activity_days = this.activity.get('weekdays');
		this.$el.find('input[name^=weekday_]').prop('checked', false);
		for (var i = activity_days.length - 1; i >= 0; i--) {
			this.$el.find('[name=weekday_'+activity_days[i]+']').prop('checked', true);
		};

		var activity_times = this.activity.get('hours');
		this.$el.find('input[name^=time_]').prop('checked', false);
		for (var i = activity_times.length - 1; i >= 0; i--) {
			this.$el.find('[name=time_'+activity_times[i]+']').prop('checked', true);
		};

		// refresh ranges intervals
		this.refresh_ranges_view();
	},

	set_duration_interval: function() {
		this.activity.set('duration_interval', this.$duration_interval_input.val() * this.duration_unit);
	},

	set_duration_middle: function(){
		this.activity.first_occurance().set('duration', this.$duration_middle_input.val() * this.duration_unit);
	},

	set_hours: function() {
		var selected = [];
		this.$el.find('[name^=time_]').each(function() {
			if ($(this).prop('checked') == true) {
				selected.push($(this).attr('name').substr(5));
			}
		});

		this.activity.set('hours', selected);
	},

	set_weekdays: function() {
		var selected = [];
		this.$el.find('[name^=weekday_]').each(function() {
			if ($(this).prop('checked') == true) {
				selected.push($(this).attr('name').substr(8));
			}
		});

		this.activity.set('weekdays', selected);
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
	}
	
});

});