// (c) 2013 Lukas Dolezal
"use strict";

/*

boundless domain form part

*/

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons');

return Backbone.View.extend({

	template:
		"<div>" +
			"<label>Repeat interval starting at</label>" +
			"<input type=text name='boundless_from' class='datetime' />" +
			"<label>Lasting</label>" +
			"<div class='input-append'>" +
				"<input type=number min='1' name='boundless_duration' class='span2'> <span id='boundless_duration_unit'/>" +
			"</div>" +
			"<div class='input-append'>" +
			"<label>Each</label>" +
				"<input type=number min='1' name='boundless_period' class='span2'> <span id='boundless_period_unit'/>" +
			"</div>" +
		"</div>",

	duration_unit_template: 
		"<select class='input-mini'>" +
			"<option value='hour'>Hours</option>" +
			"<option value='day'>Days</option>" +
			"<option value='week'>Weeks</option>" +
			"<option value='month'>Months</option>" +
		"</select>", 


	initialize: function() {
		this.$el.append(this.template);

		this.$el.find('input.datetime').datetimepicker();

		this.$el.find('#boundless_duration_unit').append($(this.duration_unit_template).attr('name', 'boundless_duration_unit'));
		this.$el.find('#boundless_period_unit').append($(this.duration_unit_template).attr('name', 'boundless_period_unit'));

		this.load_from_model();
	},

	load_from_model: function() {
		this.model.data.from && this.$el.find('input[name=boundless_from]').datetimepicker('setDate', moment(this.model.data.from.replace(/Z$/,'')).toDate());
		this.model.data.duration &&  this.$el.find('input[name=boundless_duration]').val(this.model.data.duration.duration);
		this.model.data.duration && this.$el.find('[name=boundless_duration_unit]').val(this.model.data.duration.unit);
		this.model.data.period && this.$el.find('input[name=boundless_period]').val(this.model.data.period.duration);
		this.model.data.period && this.$el.find('[name=boundless_period_unit]').val(this.model.data.period.unit);
	},

	update_to_model: function() {
		$.extend(this.model.data, {
			// discard timezone part
			from: moment.asUtc(this.$el.find('input[name=boundless_from]').datetimepicker('getDate')).toJSON(),
			duration: {
				duration: this.$el.find('input[name=boundless_duration]').val(),
				unit: this.$el.find('[name=boundless_duration_unit]').val(),
			},
			period: {
				duration: this.$el.find('input[name=boundless_period]').val(),
				unit: this.$el.find('[name=boundless_period_unit]').val(),
			}
		});
		this.trigger('change');
	}

})
});