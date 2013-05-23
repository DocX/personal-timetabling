// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    FixedActivityStub = require('models/fixed_activity_stub'),
    ActivityOccurance = require('models/activity_occurance'),
    PanelBase = require('components/panel_base');
    
return PanelBase.extend({

	template: 
		"<div id='activity_definition_fixed'>" +
			"<label>Since</label>" +
			"<input type='text' name='fixed_from' class='datetime fill-width' />" +
			"<label>Until</label>" +
			"<input type='text' name='fixed_to' class='datetime fill-width' />" +

			"<p>" +
				"<label class='radio'><input type='radio' name='repeating' value='once' checked='checked' /> Once</label>" +
				"<label class='radio'><input type='radio' name='repeating' value='repeat' /> Repeat</label>" +
			"</p>" +

			"<div class='repeat-form hide'>"+
				"<label>Each</label>" +
				"<input type='number' name='repeat-each' min='1' value='1' /> <span class='repeat-unit'></span>" +
				
				"<label>Until</label>" +
				"<input type='number' name='repeat-until-repeats' min='1' class='fill-width' value='3' /> " +
				"<input type='text' name='repeat-until-date' class='datetime fill-width' style='display:none' /> " +

				"<label class='radio'><input type='radio' name='repeat-until-unit' value='date' /> Date</label>" +
				"<label class='radio'><input type='radio' name='repeat-until-unit' value='repeats' checked /> Repeats</label>" +
			"</div>"+
		"</div>",

	events: {
		'change input[name=repeating]': 'toggle_repeat',
		'change input[name=repeat-until-unit]': function() { this.toggle_repeat_until() },
		'change input[name=repeat-each]': 'set_repeating_model',
		'change input[name=repeat-until-repeats]': 'set_repeating_model',
		'change input[name=repeat-until-date]': 'set_repeating_model',
	},

	initialize: function(){
		this.$el.append(this.template);

		this.$from_input = this.$el.find('[name=fixed_from]');
		this.$to_input = this.$el.find('[name=fixed_to]');

		this.$from_input.datetimepicker({ 
			onClose: _.bind(function(dateText, inst) {
				if (this.$to_input.val() != '') {
					var testStartDate = this.$from_input.datetimepicker('getDate');
					var testEndDate = this.$to_input.datetimepicker('getDate');
					if (testStartDate > testEndDate)
						this.$to_input.datetimepicker('setDate', testStartDate);
				}
				else {
					this.$to_input.val(dateText);
				}

				// update occurance model
				this.activity.first_occurance().set('start', moment.asUtc(this.$from_input.datetimepicker('getDate')));

				// move view to be visible
				this.options.activities_view.show_date(moment.asUtc(this.$from_input.datetimepicker('getDate')))
			}, this),
			onSelect: _.bind(function (selectedDateTime){
				this.$to_input.datetimepicker('option', 'minDate', this.$from_input.datetimepicker('getDate') );
			}, this),
			firstDay: 1,
		});

		this.$to_input.datetimepicker({ 
			onClose: _.bind(function(dateText, inst) {
				if (this.$from_input.val() != '') {
					var testStartDate = this.$from_input.datetimepicker('getDate');
					var testEndDate = this.$to_input.datetimepicker('getDate');
					if (testStartDate > testEndDate)
						this.$from_input.datetimepicker('setDate', testEndDate);
				}
				else {
					this.$from_input.val(dateText);
				}

				this.activity.first_occurance().set('end', moment.asUtc(this.$to_input.datetimepicker('getDate')));
			}, this),
			onSelect: _.bind(function (selectedDateTime){
				this.$from_input.datetimepicker('option', 'maxDate', this.$to_input.datetimepicker('getDate') );
			}, this),
			firstDay: 1
		});

		this.$el.find('[name=repeat-until-date]').datetimepicker({
			firstDay: 1
		});


		this.activity = new FixedActivityStub();
		this.activity.first_occurance().set('start', this.options.activities_view.get_date_aligned_to_view_grid(
			this.activity.first_occurance().get('start')
		));
		this.activity.first_occurance().on('change', this.update_from_model , this);;
		this.activity.on('change:repeating', this.update_repeating_from_model, this);

		this.set_repeat_period_unit();
		this.options.activities_view.on('geometry_changed', this.set_repeat_period_unit, this);


		this.update_from_model(this.activity.first_occurance())
	},

	update_from_model: function(m) {
		this.$from_input.datetimepicker('setDate', moment.asLocal(m.get('start')).toDate());
		this.$to_input.datetimepicker('setDate', moment.asLocal(m.get('end')).toDate());
	},

	update_repeating_from_model: function(m) {
		this.$el.find('[name=repeating]').prop('checked', false).filter('[value='+(m.get('repeating') == false ? 'once' : 'repeat') +']').prop('checked', true);
		if (m.get('repeating') != false) {
			this.$el.find('[name=repeat-each]').val(m.get('repeating').period_duration);
			this.$el.find('[name=repeat-until-repeats]').val(m.get('repeating').until_repeats);
			if (m.get('repeating').until_date)
				this.$el.find('[name=repeat-until-date]').datetimepicker('setDate', moment.asLocal(m.get('repeating').until_date));

			this.$el.find('[name=repeat-until-unit]').prop('checked', false).filter('[value='+m.get('repeating').until_type+']').prop('checked', true);
		}

		this.$el.find('.repeat-form').toggle(m.get('repeating') != false);
	},

	toggle_repeat:function(){
		var repeating = this.$el.find('[name=repeating]:checked').val();

		this.$el.find('.repeat-form').toggle(repeating == 'repeat');

		if (repeating == 'repeat') {
			this.set_repeating_model();
		} else {
			this.activity.set('repeating', false);
		}
	},

	set_repeat_period_unit: function() {

		this.$el.find('span.repeat-unit').text(this.options.activities_view.get_view_geometry_name());
	},

	toggle_repeat_until: function(){
		var repeat_until_unit = this.$el.find('[name=repeat-until-unit]:checked').val();

		this.$el.find('[name=repeat-until-repeats]').toggle(repeat_until_unit == 'repeats');
		this.$el.find('[name=repeat-until-date]').toggle(repeat_until_unit == 'date');
	},

	set_repeating_model: function() {
		var repeating_def = {};

		repeating_def.period_duration = this.$el.find('[name=repeat-each]').val();
		repeating_def.period_unit = this.options.activities_view.get_view_geometry_name();
		repeating_def.until_repeats = this.$el.find('[name=repeat-until-repeats]').val();
		repeating_def.until_date = this.$el.find('[name=repeat-until-date]').datetimepicker('getDate') && moment.asUtc(this.$el.find('[name=repeat-until-date]').datetimepicker('getDate'));
		repeating_def.until_type = this.$el.find('[name=repeat-until-unit]:checked').val();

		this.activity.set('repeating', repeating_def);
	},

});

});