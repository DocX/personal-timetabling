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
			"First day <input class='date' type='text' name='dayly_first' />" +
		"</div>" +

		"<legend>Interval</legend>" +
		"<label>Start hour</label>" +
		"<input name='dayly_start_hour' type='number' min='0' max='23' />" +
		"<label>End hour</label>" +
		"<input name='dayly_end_hour' type='number' min='1' max='24' />",


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
		this.$el.find('[name=dayly_start_hour]').val(moment.utc(this.model.data.from).hour());
		this.$el.find('[name=dayly_end_hour]').val( moment.utc(this.model.data.from).hour() + this.model.data.duration.duration);

		if (this.model.data.from) {
			// strip time part of from time
			this.set_datetime('[name=dayly_first]', moment.utc(this.model.data.from).startOf('day').toJSON());
		} else {
			this.set_datetime('[name=dayly_first]', moment.toJSON());
		}
	},

	update_to_model: function() {
		var start_hour = this.$el.find('[name=dayly_start_hour]').val();
		var end_hour = this.$el.find('[name=dayly_end_hour]').val();

		var from = moment.utc(this.get_datetime('[name=dayly_first]')).startOf('day');
		from.add(Number(start_hour), 'h');

		this.model.data.from = from.toJSON();
		this.model.data.duration.duration = end_hour - start_hour;
		this.model.data.duration.unit = 'hour';
	},
});
});