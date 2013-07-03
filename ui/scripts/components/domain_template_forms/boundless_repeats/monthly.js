// (c) 2013 Lukas Dolezal
"use strict";

/*

boundless domain form part - daly repeat

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
			"First month <input type=number min='1' max='12' name='monthly_first_month' class='input-mini' />/<input type=number min='1930' max='2200' name='monthly_first_year' class='input-mini' />" +
		"</div>" +

		"<label>Date in month</label>" +
		"<input name='monthly_month_date' type=number min=1 max=31 />" +
		"<p>Note: Months that do not contain entered date will be skipped</p>",

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
		var from_mdate = moment.utc(this.model.data.from).date();
		var first_month = moment.utc(this.model.data.from).month();
		var first_year = moment.utc(this.model.data.from).year();

		this.$el.find('[name=monthly_first_year]').val(first_year);
		this.$el.find('[name=monthly_first_month]').val( first_month);
		this.$el.find('[name=monthly_month_date]').val( from_mdate);

	},

	update_to_model: function() {
		var month_date = this.$el.find('[name=monthly_month_date]').val();
		var first_year = this.$el.find('[name=monthly_first_year]').val();
		var first_month = this.$el.find('[name=monthly_first_month]').val();

		var from = moment.utc().startOf('day').year(first_year).month(first_month).date(month_date);

		this.model.data.from = from.toJSON();
		this.model.data.duration.duration = 1;
		this.model.data.duration.unit = 'day';
	},
});
});