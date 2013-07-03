// (c) 2013 Lukas Dolezal
"use strict";

/*

bounded domain form part

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
			"<p><strong>Single interval</strong></p>" +
			"<label>From</label>" +
			"<input type='text' name='bounded_from' class='datetime' />" +
			"<label>To</label>" +
			"<input type='text' name='bounded_to' class='datetime' />" +					
		"</div>",

	initialize: function() {
		this.$el.append(this.template);

		this.$el.find('input.datetime').datetimepicker();

		this.load_from_model();
	},

	load_from_model: function() {
		if (this.model.data.from) {
			this.$el.find('input[name=bounded_from]').datetimepicker('setDate', moment(this.model.data.from.replace(/Z$/,'')).toDate());
		}
		if (this.model.data.to) {
			this.$el.find('input[name=bounded_to]').datetimepicker('setDate', moment(this.model.data.to.replace(/Z$/,'')).toDate());
		}
	},

	update_to_model: function() {
		$.extend(this.model.data, {
			from: moment.asUtc(this.$el.find('input[name=bounded_from]').datetimepicker('getDate')).toJSON(),
			to: moment.asUtc(this.$el.find('input[name=bounded_to]').datetimepicker('getDate')).toJSON(),
		});
		this.trigger('change');
	}

})
});