// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons');


return Backbone.View.extend({

	template: 
		"<p>" +
			"<label class='radio'><input type='radio' name='repeating' value='once' checked='checked' /> Once</label>" +
			"<label class='radio'><input type='radio' name='repeating' value='repeat' /> Repeat</label>" +
		"</p>" +

		"<div class='repeating-controls hide'>"+	
			"<label>Each</label>" +
			"<input type='number' name='repeat-each' min='1' value='1' /> <span class='repeat-unit'></span>" +
			
			"<label>Until</label>" +
			"<input type='number' name='repeat-until-repeats' min='1' class='fill-width' value='3' /> " +
			"<input type='text' name='repeat-until-date' class='datetime fill-width' style='display:none' /> " +

			"<label class='radio'><input type='radio' name='repeat-until-unit' value='date' /> Date</label>" +
			"<label class='radio'><input type='radio' name='repeat-until-unit' value='repeats' checked /> Repeats</label>" +
		"</div>",


	events: {
		'change input[name=repeating]': 'toggle_repeat',
		'change input[name=repeat-until-unit]': function() { this.toggle_repeat_until() },
		'change input[name=repeat-each]': 'trigger_change',
		'change input[name=repeat-until-repeats]': 'trigger_change',
		'change input[name=repeat-until-date]': 'trigger_change',
	},

	initialize: function() {
		this.$el.append(this.template);

		this.$el.find('[name=repeat-until-date]').datetimepicker({
			firstDay: 1
		});

		this.set_period_unit('days');
	},

	update_controls_from: function(repeating_def) {

		this.$el.find('[name=repeat-each]').val(repeating_def.period_duration);
		this.$el.find('[name=repeat-until-repeats]').val(repeating_def.until_repeats);
		if (repeating_def.until_date)
			this.$el.find('[name=repeat-until-date]').datetimepicker('setDate', moment.asLocal(repeating_def.until_date));

		this.$el.find('[name=repeat-until-unit]').prop('checked', false).filter('[value='+repeating_def.until_type+']').prop('checked', true);
	},

	set_period_unit: function(unit) {
		this.period_unit = unit;
		this.$el.find('span.repeat-unit').text(unit);
	},

	toggle_repeat_until: function(){
		var repeat_until_unit = this.$el.find('[name=repeat-until-unit]:checked').val();

		this.$el.find('[name=repeat-until-repeats]').toggle(repeat_until_unit == 'repeats');
		this.$el.find('[name=repeat-until-date]').toggle(repeat_until_unit == 'date');

		this.trigger_change();
	},

	get_repeating_def: function() {
		var repeating = this.$el.find('[name=repeating]:checked').val();

		if (repeating == 'once') {
			return false;
		}

		var repeating_def = {};

		repeating_def.period_duration = this.$el.find('[name=repeat-each]').val();
		repeating_def.period_unit = this.period_unit;
		repeating_def.until_repeats = this.$el.find('[name=repeat-until-repeats]').val();
		repeating_def.until_date = this.$el.find('[name=repeat-until-date]').datetimepicker('getDate') && moment.asUtc(this.$el.find('[name=repeat-until-date]').datetimepicker('getDate'));
		repeating_def.until_type = this.$el.find('[name=repeat-until-unit]:checked').val();

		return repeating_def;
	},

	trigger_change: function() {
		this.trigger('change');
	},

	toggle_repeat:function(){
		var repeating = this.$el.find('[name=repeating]:checked').val();

		this.$el.find('.repeating-controls').toggle(repeating == 'repeat');

		this.trigger_change();
	},

	update_from: function(repeating) {
		this.$el.find('[name=repeating]').prop('checked', false).filter('[value='+(repeating == false ? 'once' : 'repeat') +']').prop('checked', true);
		if (repeating != false) {
			this.update_controls_from(repeating);
		}

		this.$el.find('.repeating-controls').toggle(repeating != false);
	},


});

});