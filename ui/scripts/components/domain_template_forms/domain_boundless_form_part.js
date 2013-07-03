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
    momentAddons = require('lib/moment.addons'),
    DaylyRepeatForm = require('components/domain_template_forms/boundless_repeats/dayly'),
    WeeklyRepeatForm = require('components/domain_template_forms/boundless_repeats/weekly'),
    MonthlyRepeatForm = require('components/domain_template_forms/boundless_repeats/monthly');

return Backbone.View.extend({

	template:
		"<div>" +
			"<legent>Repeating single interval</label>" +
			
			"<legend>Repeat</legend>" +
			"<div class=''>" +
				"<label><input type=radio name='boundless_period_unit' value='day'> dayly</label> " + 
				"<label><input type=radio name='boundless_period_unit' value='week'> weekly</label> " + 
				"<label><input type=radio name='boundless_period_unit' value='month'> monthly</label> " + 
			"</div>" +
			"<p>every <input type='number' name='boundless_period' class='input-small' min=1 /> day/week/month</p>" +
			"<div data-role='interval-form'></div>" +
		"</div>",

	events: {
		'change [name=boundless_period]': 'set_every_number',
		'change [name=boundless_period_unit]': 'select_period',
	},

	initialize: function() {
		this.$el.append(this.template);

		this.$interval_form = this.$el.find('[data-role=interval-form]');

		this.model.data = $.extend({
			from: moment().toJSON(),
			duration: {duration: 1, unit:'hour'},
			period: {duration:1, unit:'day'}
		}, this.model.data);

		this.load_from_model();
	},

	set_every_number: function(e) {
		var period_num =  this.$el.find('[name=boundless_period]').val();
		this.interval_form && this.interval_form.set_period_number(period_num);
	},

	select_period: function() {
		this.set_form(this.$el.find('[name=boundless_period_unit]:checked').val());
	},

	set_form: function(period) {
		this.interval_form && this.interval_form.remove();
		var el_for_form = $('<div />');
		this.$interval_form.append(el_for_form);

		switch(period) {
			case 'day':
				this.interval_form = new DaylyRepeatForm({el: el_for_form, model: this.model, parent:this});
				break;
			case 'week':
				this.interval_form = new WeeklyRepeatForm({el: el_for_form, model: this.model, parent:this});
				break;
			case 'month':
				this.interval_form = new MonthlyRepeatForm({el: el_for_form, model: this.model, parent:this});
				break;
		}

		this.interval_form.load_from_model();
		this.set_every_number();
	},

	load_from_model: function() {
		if (!(this.model.data && this.model.data.period && this.model.data.period.unit)) {
			return;
		}

		this.$el.find('[name=boundless_period_unit][value='+this.model.data.period.unit+']').prop('checked', true);
		this.$el.find('input[name=boundless_period]').val(this.model.data.period.duration);

		this.set_form(this.model.data.period.unit);
	},

	update_to_model: function() {
		$.extend(this.model.data, {
			period: {
				duration: this.$el.find('input[name=boundless_period]').val(),
				unit: this.$el.find('input[name=boundless_period_unit]:checked').val(),
			}
		});

		this.interval_form.update_to_model();

		this.trigger('change');
	},

	from_nested_save: function() {}

}, {

	domain_label: function(domain) {
		var string = '';
		switch(domain.data.period.unit) {
			case 'day':
				string = DaylyRepeatForm.domain_label(domain);
				break;
			case 'week':
				string = WeeklyRepeatForm.domain_label(domain);
				break;
			case 'month':
				string = MonthlyRepeatForm.domain_label(domain);
				break;
		}
		return string;
	}
})
});