// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
	moment = require('moment');

return Backbone.Model.extend({

	defaults: {
		start: moment.utc(),
		end: moment.utc(),
	},

	parse: function(data) {
		// mapping to bounded interval rb model
		return {
			start: moment.utc(data.data.from),
			end: moment.utc(data.data.to)
		};
	},

	isInInterval: function(date_start, duration) {
		var date_end = date_start.clone().add('s', duration);

		var this_start = this.get('start');
		var this_end = this.get("end");

		return !(date_start.isBefore(this_start) || date_end.isAfter(this_end));
	}

});

});