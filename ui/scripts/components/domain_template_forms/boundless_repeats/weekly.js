// (c) 2013 Lukas Dolezal
"use strict";

/*

boundless domain form part - weekly repeat

*/

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    BaseForm = require('components/base_form');

return BaseForm.extend({

	template:
		"<div data-role='not-every-one'>"+
			"First week <input type=number min='1' max='53' name='weekly_first_weeknum' class='input-mini' />/<input type=number min='1930' max='2200' name='weekly_first_year' class='input-small' />" +
		"</div>" +

		"<label>Weekday</label>" +
		"<div class='form-inline'>" +
			"<label><input type='radio' name='weekly_weekday' value='working'> Mon - Fri</label> "+
			"<label><input type='radio' name='weekly_weekday' value='weekend'> Weekend</label> <br> "+
			"<label><input type='radio' name='weekly_weekday' value='1'> Mon</label> "+
			"<label><input type='radio' name='weekly_weekday' value='2'> Tue</label> "+
			"<label><input type='radio' name='weekly_weekday' value='3'> Wed</label> "+
			"<label><input type='radio' name='weekly_weekday' value='4'> Thu</label> "+
			"<label><input type='radio' name='weekly_weekday' value='5'> Fri</label> "+
			"<label><input type='radio' name='weekly_weekday' value='6'> Sat</label> "+
			"<label><input type='radio' name='weekly_weekday' value='0'> Sun</label> "+
		"</div>"+
		"<p>Note: Repeating interval defines only one atomic interval, not set. For set of weekdays add each day to stack or use weekdays type.</p>",

	initialize: function() {
		this.$el.append(this.template);
		this.$el_not_every = this.$el.find('[data-role=not-every-one]');
		this.setup_pickers();
	},

	set_period_number: function(number) {
		if (number > 1) {
			this.$el_not_every.show();
		} else{
			this.$el_not_every.hide();
		}
	},

	load_from_model: function() {
		var from_day = moment.utc(this.model.data.from).day();
		var week_num = moment.utc(this.model.data.from).isoWeek();
		var week_year = moment.utc(this.model.data.from).startOf('week').day(1).year();

		this.$el.find('[name=weekly_first_weeknum]').val(week_num);
		this.$el.find('[name=weekly_first_year]').val( week_year);

		var weekly_weekday = '';
		if (this.model.data.duration.duration == 5 && from_day == 1) {
			weekly_weekday = 'working'
		} else if (this.model.data.duration.duration == 2 && from_day == 6) {
			weekly_weekday = 'weekend'
		} else {
			weekly_weekday = from_day
		}

		this.$el.find('[name=weekly_weekday][value='+weekly_weekday+']').prop('checked', true);
	},

	update_to_model: function() {
		var first_week = this.$el.find('[name=weekly_first_weeknum]').val();
		var first_year = this.$el.find('[name=weekly_first_year]').val();
		var weekday_val = this.$el.find('[name=weekly_weekday]:checked').val();
		
		var weekday;
		var days;
		switch(weekday_val) {
			case 'working':
				weekday=1;
				days = 5;
				break;
			case 'weekend':
				weekday=6;
				days=2;
				break;
			default:
				weekday = weekday_val;
				days=1;
				break;
		}

		var from = moment.utc().startOf('day').year(first_year).day(weekday).isoWeek(first_week);

		this.model.data.from = from.toJSON();
		this.model.data.duration.duration = days;
		this.model.data.duration.unit = 'day';
	},
},{
	domain_label: function(domain) {
		var from_day = moment.utc(domain.data.from).format('ddd');

		if (domain.data.duration.duration == 5) {
			from_day = 'weekdays'
		} else if (domain.data.duration.duration == 2) {
			from_day = 'weekends'
		}

		var label;
		if (domain.data.period.duration > 1) {
			label = from_day + ' every ' + domain.data.period.duration + ' weeks';
		} else {
			label = from_day;
		}

		return label;
	}

});
});