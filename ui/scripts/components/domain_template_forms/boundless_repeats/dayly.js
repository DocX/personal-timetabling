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
		"<label>Day hour</label>" +
		"<div data-role='hour-slider'/>" +
		"<p data-role='hour-values'></p>",


	initialize: function() {
		this.$el.append(this.template);
		this.$el_not_every = this.$el.find('[data-role=not-every-one]');
		this.setup_pickers();
		this.$hour_slider = this.$el.find('[data-role=hour-slider]');
		this.$hour_slider.slider({
			range: true,
		      min: 0,
		      max: 24,
		      values: [ 0, 24 ],
		      slide: _.bind(this.slide, this)
		});

		this.slide(null, {values:[0,24]});
	},

	set_period_number: function(number) {
		if (number > 1) {
			this.$el_not_every.show();
		} else{
			this.$el_not_every.hide();
		}
	},

	slide: function(e, ui) {
		this.$el.find('[data-role=hour-values]').text(ui.values[0] + ':00 - ' + ui.values[1] + ':00');
	},

	load_from_model: function() {

		this.$hour_slider.slider('values', [
				moment.utc(this.model.data.from).hour(),
				moment.utc(this.model.data.from).hour() + this.model.data.duration.duration
			])

		if (this.model.data.from) {
			// strip time part of from time
			this.set_datetime('[name=dayly_first]', moment.utc(this.model.data.from).startOf('day').toJSON());
		} else {
			this.set_datetime('[name=dayly_first]', moment.toJSON());
		}
	},

	update_to_model: function() {
		var values = this.$hour_slider.slider('values');
		var start_hour = values[0];
		var end_hour = values[1];

		var from = moment.utc(this.get_datetime('[name=dayly_first]')).startOf('day');
		from.add(Number(start_hour), 'h');

		this.model.data.from = from.toJSON();
		this.model.data.duration.duration = end_hour - start_hour;
		this.model.data.duration.unit = 'hour';
	},
}, {
	domain_label: function(domain) {
		var fromh = moment.utc(domain.data.from).hour();
		var toh = moment.utc(domain.data.from).hour() + domain.data.duration.duration;
		var label = fromh + '-' + toh + 'h';

		if (domain.data.period.duration > 1) {
			label += ' every ' + domain.data.period.duration + ' day';
		}

		return label;
	}
});
});