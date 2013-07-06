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
		"<div class='form-inline'><p>" +
			"<label class='radio'><input type='radio' name='repeating' value='once' checked='checked' /> Once</label> " +
			"<label class='radio'><input type='radio' name='repeating' value='repeat' /> Repeat</label> " +
		"</p></div>" +

		"<div class='repeating-controls hide'>"+	
			"<label>Every</label>" +
			"<div class='form-inline'><p>" +
				"<input type='number' name='repeat-each' min='1' class='input-mini' value='1' /> " +
				"<label><input type='radio' name='repeat-each-unit' value='days' /> days</label> " + 
				"<label><input type='radio' name='repeat-each-unit' value='weeks' /> weeks</label> " +
				"<label><input type='radio' name='repeat-each-unit' value='months' /> months</label> " +
			"</p></div>" +

			"<div data-role='repeat-unit-options' class='form-inline' data-unit='weeks'>" +
				"<label><input type='checkbox' name='weekly_weekday' value='1'> Mo</label> "+
				"<label><input type='checkbox' name='weekly_weekday' value='2'> Tu</label> "+
				"<label><input type='checkbox' name='weekly_weekday' value='3'> We</label> "+
				"<label><input type='checkbox' name='weekly_weekday' value='4'> Th</label> "+
				"<label><input type='checkbox' name='weekly_weekday' value='5'> Fr</label> "+
				"<label><input type='checkbox' name='weekly_weekday' value='6'> Sa</label> "+
				"<label><input type='checkbox' name='weekly_weekday' value='0'> Su</label> "+
			"</div>" +
			
			"<label>Ends</label>" +
			"<p>" +
			"<label class='radio'>" +
				"<input type='radio' name='repeat-until-unit' value='date' />" +
				" On " +
				"<input type='text' name='repeat-until-date' class='date input-small' /> " +
			"</label>" +
			"<label class='radio'>" +
				"After " +
				"<input type='radio' name='repeat-until-unit' value='repeats' checked /> " +
				"<input type='number' name='repeat-until-repeats' min='1' class='input-mini' value='3' /> " +
				" occurrences" +
			"</label>" +
			"</p>" +

		"</div>",


	events: {
		'change input[name=repeating]': 'toggle_repeat',
		'change input[name=repeat-until-unit]': function() { this.toggle_repeat_until() },
		'change input[name=repeat-each]': 'trigger_change',
		'change input[name=repeat-until-repeats]': 'trigger_change',
		'change input[name=repeat-until-date]': 'trigger_change',
		'click input[name=repeat-each-unit]': 'period_unit_change',
	},

	initialize: function() {
		this.$el.append(this.template);

		this.$el.find('[name=repeat-until-date]').datepicker({
			firstDay: 1,
			onSelect: _.bind(function(date, el) {$(el).datepicker('hide'); this.trigger_change()}, this)
		});

		this.$el.find('[name=weekly_weekday]').click(_.bind(this.trigger_change, this));

		this.set_period_unit('days');
	},

	update_controls_from: function(repeating_def) {
		this.set_period_unit(repeating_def.period_unit);
		this.$el.find('[name=repeat-each]').val(repeating_def.period_duration);
		
		if (repeating_def.until_type == 'date' && repeating_def.until) {
			this.$el.find('[name=repeat-until-date]').datepicker('setDate', moment.asLocal(repeating_def.until).toDate());
		} else if (repeating_def.until_type == 'repeats') {
			this.$el.find('[name=repeat-until-repeats]').val(repeating_def.until);
		}

		this.$el.find('[name=repeat-until-unit]').prop('checked', false).filter('[value='+repeating_def.until_type+']').prop('checked', true);

		this.$el.find('[data-role=repeat-unit-options]').hide();
		this.$el.find('[data-role=repeat-unit-options][data-unit='+this.period_unit+']').show();		
		this.$el.find('[name=weekly_weekday]').prop('checked', false);
		if(repeating_def.period_unit == 'weeks' && 'weekdays' in repeating_def.period_unit_options) {
			for (var i = 0; i < repeating_def.period_unit_options.weekdays.length; i++) {
				this.$el.find('[name=weekly_weekday][value='+ repeating_def.period_unit_options.weekdays[i] +']').prop('checked', true);
			};
		}
	},

	set_period_unit: function(unit) {
		this.period_unit = unit;
		this.$el.find('[name=repeat-each-unit][value='+unit+']').prop('checked', true);
		this.$el.find('[data-role=repeat-unit-options]').hide();
		this.$el.find('[data-role=repeat-unit-options][data-unit='+unit+']').show();
	},

	period_unit_change: function(ev) {
		this.set_period_unit($(ev.target).val());

		this.trigger_change();
	},

	toggle_repeat_until: function(){
		var repeat_until_unit = this.$el.find('[name=repeat-until-unit]:checked').val();

		if (repeat_until_unit == 'date' && !this.$el.find('[name=repeat-until-date]').datepicker('getDate')) {
			this.$el.find('[name=repeat-until-date]').datepicker('setDate', moment().startOf('day').add(1,'d').toDate());
		}
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
		repeating_def.until_type = this.$el.find('[name=repeat-until-unit]:checked').val();
		if (repeating_def.until_type == 'date') {
			repeating_def.until = this.$el.find('[name=repeat-until-date]').datepicker('getDate') && moment.asUtc(this.$el.find('[name=repeat-until-date]').datepicker('getDate'));
		} else {
			repeating_def.until = this.$el.find('[name=repeat-until-repeats]').val();
		}

		if (this.period_unit == 'weeks') {
			var checked_weekdays = [];
			this.$el.find('[name=weekly_weekday]:checked').each(function() {
				checked_weekdays.push($(this).val());
			});

			repeating_def.period_unit_options = {weekdays: checked_weekdays};
		}

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

	},


});

});