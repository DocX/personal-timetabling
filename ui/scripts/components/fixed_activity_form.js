// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    Activity = require('models/activity'),
    ActivityOccurance = require('models/activity_occurance'),
    PanelBase = require('components/panel_base');
    
return PanelBase.extend({

	template: 
		"<div id='activity_definition_fixed'>" +
			"<p>Label</p>" +
			"<input type='text' name='fixed_from' class='datetime input-small input-inline' />" +
			" - " +
			"<input type='text' name='fixed_to' class='datetime input-small input-inline' />" +
		"</div>",

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

				this.first_occurance.set('start', moment.asUtc(this.$from_input.datetimepicker('getDate')));
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

				this.first_occurance.set('end', moment.asUtc(this.$to_input.datetimepicker('getDate')));
			}, this),
			onSelect: _.bind(function (selectedDateTime){
				this.$from_input.datetimepicker('option', 'maxDate', this.$to_input.datetimepicker('getDate') );
			}, this),
			firstDay: 1
		});


		this.activity = new Activity();
		this.first_occurance = new ActivityOccurance({min_duration: 60, max_duration:-1});

		this.activity.get('occurances').add(this.first_occurance);

		this.first_occurance.on('change', function(m) {
			this.$from_input.datetimepicker('setDate', m.get('start').toDate());
			this.$to_input.datetimepicker('setDate', m.get('end').toDate());
		}, this);
	},



});

});