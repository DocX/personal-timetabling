// (c) 2013 Lukas Dolezal
"use strict";

/*

Simple domain editor. 
Domain of selected days of week masked by selected hours in day.

*/

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    DomainTemplate = require('models/domain_template');
    
return Backbone.View.extend({

	template: 
		"<label>In times</label>" +
		"<div class='time_def form-inline'>" +
			"<label><input type='checkbox' name='time_morning' value='add' >Morning 8 - 11</label> "+
			"<label><input type='checkbox' name='time_noon' value='add' >Noon 11 - 13</label> "+
			"<label><input type='checkbox' name='time_afternoon' value='add' >Afternoon 13 - 16</label> "+
			"<label><input type='checkbox' name='time_evening' value='add' >Evening 16 - 20</label> "+
			"<label><input type='checkbox' name='time_lateevening' value='add' >Late evening 20 - 24</label> "+
			"<label><input type='checkbox' name='time_night' value='add' >Night 0 - 8</label>"+
		"</div>" +

		"<label>Weekdays</label>" +
		"<div class='weekdays_def form-inline'>" +
			"<label><input type='checkbox' name='weekday_1' value='add' >Mon</label> "+
			"<label><input type='checkbox' name='weekday_2' value='add' >Tue</label> "+
			"<label><input type='checkbox' name='weekday_3' value='add' >Wed</label> "+
			"<label><input type='checkbox' name='weekday_4' value='add' >Thu</label> "+
			"<label><input type='checkbox' name='weekday_5' value='add' >Fri</label> "+
			"<label><input type='checkbox' name='weekday_6' value='add' >Sat</label> "+
			"<label><input type='checkbox' name='weekday_7' value='add' >Sun</label> "+
		"</div>",


	events: {
		'click [type=checkbox]': 'set_domain_model',
	},

	defaults: {
		weekdays : [1,2,3,4,5],
		hours: ['morning','noon', 'afternoon']
	},

	domain_templates_weekdays: {
		//monday
		1: {type: 'boundless', data: {from: '2013-05-06T00:00:00Z', duration: {duration: 1, unit: 'day'}, period: {duration: 1, unit:'week'}}},
		//tuesday
		2: {type: 'boundless', data: {from: '2013-05-07T00:00:00Z', duration: {duration: 1, unit: 'day'}, period: {duration: 1, unit:'week'}}},
		//wednesday
		3: {type: 'boundless', data: {from: '2013-05-08T00:00:00Z', duration: {duration: 1, unit: 'day'}, period: {duration: 1, unit:'week'}}},
		// thursday
		4: {type: 'boundless', data: {from: '2013-05-09T00:00:00Z', duration: {duration: 1, unit: 'day'}, period: {duration: 1, unit:'week'}}},
		// friday
		5: {type: 'boundless', data: {from: '2013-05-10T00:00:00Z', duration: {duration: 1, unit: 'day'}, period: {duration: 1, unit:'week'}}},
		// saturday
		6: {type: 'boundless', data: {from: '2013-05-11T00:00:00Z', duration: {duration: 1, unit: 'day'}, period: {duration: 1, unit:'week'}}},
		// sunday
		7: {type: 'boundless', data: {from: '2013-05-12T00:00:00Z', duration: {duration: 1, unit: 'day'}, period: {duration: 1, unit:'week'}}},
	},

	domain_templates_daytime: {
		'morning'  	  : {type: 'boundless', data: {from: '2013-05-06T08:00:00Z', duration: {duration: 3, unit: 'hour'}, period:{duration:1, unit:'day'}}},
		'noon'        : {type: 'boundless', data: {from: '2013-05-06T11:00:00Z', duration: {duration: 2, unit: 'hour'}, period:{duration:1, unit:'day'}}}, 
		'afternoon'    : {type: 'boundless', data: {from: '2013-05-06T13:00:00Z', duration: {duration: 3, unit: 'hour'}, period:{duration:1, unit:'day'}}},
		'evening'     : {type: 'boundless', data: {from: '2013-05-06T16:00:00Z', duration: {duration: 4, unit: 'hour'}, period:{duration:1, unit:'day'}}},
		'lateevening' : {type: 'boundless', data: {from: '2013-05-06T20:00:00Z', duration: {duration: 4, unit: 'hour'}, period:{duration:1, unit:'day'}}},
		'night'       : {type: 'boundless', data: {from: '2013-05-06T00:00:00Z', duration: {duration: 8, unit: 'hour'}, period:{duration:1, unit:'day'}}},
	},

	initialize: function() {
		this.$el.html(this.template);

		this.model = {type: 'stack', data:{actions:{}}};

		// find elements
		this.$day_checkboxes = {};
		for (var day in this.domain_templates_weekdays) {
			this.$day_checkboxes[day] = this.$el.find('[name=weekday_'+day+']');
		}
		this.$time_checkboxes = {};
		for (var time in this.domain_templates_daytime) {
			this.$time_checkboxes[time] = this.$el.find('[name=time_'+time+']');
		}

		this.set_values(this.defaults);
		this.set_domain_model();
	},

	set_values: function(values) {
		// reset state
		for (var i in this.$day_checkboxes) {
			this.$day_checkboxes[i].prop('checked', false);
		};
		for (var i in this.$time_checkboxes) {
			this.$time_checkboxes[i].prop('checked', false);
		};

		// set state
		for (var i = 0; i < values.weekdays.length; i++) {
			this.$day_checkboxes[ values.weekdays[i] ].prop('checked', true);
		};
		for (var i = 0; i < values.hours.length; i++) {
			this.$time_checkboxes[ values.hours[i] ].prop('checked', true);
		};
	},

	set_domain_model: function() {
		var weekdays = this.get_selected_weekdays();
		var hours = this.get_selected_hours();

		// create times in day - as mask
		var time_actions = [];
		for (var i = hours.length - 1; i >= 0; i--) {
			time_actions.push({action: 'add', domain: this.domain_templates_daytime[ hours[i] ]});
		};

		// create weekdays
		var day_actions = [];
		for (var i = weekdays.length - 1; i >= 0; i--) {
			day_actions.push({action: 'add', domain: this.domain_templates_weekdays[ weekdays[i] ]});
		};

		this.model.data.actions = [
			{action: 'mask', domain: {type:'stack', data:{actions: time_actions }}},
			{action: 'add', domain: {type:'stack', data:{actions: day_actions }}},
			]; 
		
		this.trigger('change');
	},

	get_selected_hours: function() {
		var hours = [];
		for (var i in this.$time_checkboxes) {
			if (this.$time_checkboxes[i].prop('checked')) {
				hours.push(i);
			}
		};
		return hours;
	},

	get_selected_weekdays: function() {
		var weekdays = [];
		for (var i in this.$day_checkboxes) {
			if (this.$day_checkboxes[i].prop('checked')) {
				weekdays.push(i);
			}
		};
		return weekdays;
	},

});
});