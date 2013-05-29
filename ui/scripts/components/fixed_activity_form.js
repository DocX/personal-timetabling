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
    PanelBase = require('components/panel_base'),
    ActivityRepeatingForm = require('components/activity_repeating_form');
    
return PanelBase.extend({

	template: 
		"<div id='activity_definition_fixed'>" +
			"<label>Since</label>" +
			"<input type='text' name='fixed_from' class='datetime fill-width' />" +
			"<label>Until</label>" +
			"<input type='text' name='fixed_to' class='datetime fill-width' />" +
			"<p>TIP: You can move first occurence box to setup date range of acitivity</p>" +

			"<div class='repeat-form-controls'></div>" +
			"<p class='repeating-tip hide'>TIP: move the last occurence box to change number of repeats</p>" +
		"</div>",

	events: {
		
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

		
		this.activity = new FixedActivityStub();
		this.activity.first_occurance().set('start', this.options.activities_view.get_date_aligned_to_view_grid(
			this.activity.first_occurance().get('start')
		));
		this.activity.first_occurance().on('change', this.update_from_model , this);
		this.activity.on('change:repeating', this.update_repeating_from_model, this);

		this.repeating_form = new ActivityRepeatingForm({
			el: this.$el.find('.repeat-form-controls')
		});

		this.listenTo(this.repeating_form, 'changed', this.update_repeating_to_model);

		this.set_repeat_period_unit();
		this.options.activities_view.on('geometry_changed', this.set_repeat_period_unit, this);

		this.update_from_model(this.activity.first_occurance());
	},

	update_from_model: function(m) {
		this.$from_input.datetimepicker('setDate', moment.asLocal(m.get('start')).toDate());
		this.$to_input.datetimepicker('setDate', moment.asLocal(m.get('end')).toDate());
	},

	set_repeat_period_unit: function() {
		this.repeating_form.set_period_unit(this.options.activities_view.get_view_geometry_name());
	},

	update_repeating_to_model: function() {
		var repeating = this.repeating_form.get_repeating_def();
		this.activity.set('repeating', repeating );

		this.$el.find('.repeating-tip').toggle(repeating != false);
	},

	update_repeating_from_model: function(m) {
		this.repeating_form.update_from(m.get('repeating'));
	}

	

});

});