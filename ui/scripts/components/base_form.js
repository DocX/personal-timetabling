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
	// utils
	setup_pickers: function() {
		this.$el.find('input.datetime').datetimepicker();
		this.$el.find('input.time').timepicker();
		this.$el.find('input.date').datepicker();
	},

	get_datetime: function(selector) {
		return moment.asUtc(this.$el.find(selector).datetimepicker('getDate')).toJSON()
	},

	set_datetime: function(selector, date) {
		this.$el.find(selector).datetimepicker(
			'setDate', moment.asLocal(date).toDate());
	},

	get_time: function(selector) {
		return moment.asUtc(this.$el.find(selector).timepicker('getDate')).toJSON()
	},

	set_time: function(selector, date) {
		this.$el.find(selector).timepicker(
			'setDate', moment.asLocal(date).toDate());
	},
});

});