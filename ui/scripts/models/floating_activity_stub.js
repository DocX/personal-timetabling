// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
		BackboneRelational = require('backbone.relational'),
		FixedActivityStub = require('models/fixed_activity_stub'),
		Interval = require('models/interval'),
		ActivityOccurance = require('models/activity_occurance');

// Stub representation for creating fixed activity in UI
var FloatingActivityStub = FixedActivityStub.extend({
	
	defaults: function() {
		return {
			definition: {
				repeating: false,
				type: 'floating',
				from: null,
				to: null,
				duration_min: 1,
				duration_max: 1,
				domain_template: {}
			},
			// stub repeating
			repeating: false,
			// duration of domain range. is added to start when moving with first occurance
			range_duration: 86400 * 7,
			// +- interval for occurences duration. is subtracted from and added to first occurence duration when moved
			duration_interval: 0,
			// set of day hours
			hours: ['morning', 'noon', 'afternoon'],
			// set of weekdays
			weekdays: [1,2,3,4,5],
			// if set, use stored domain as domain template
			domain_template_id: null,
		}
	},


	initialize: function() {
		FixedActivityStub.prototype.initialize.apply(this);
		this.on('change:range_duration', this.set_repeating, this);
		this.on('change:duration_interval', this.set_repeating, this);
		this.on('change:hours', this.set_domain, this);
		this.on('change:weekdays', this.set_domain, this);
	},

	// updates definition Hash from stub values
	set_definition: function() {
		this.attributes.definition.repeating = this.attributes.repeating;
		this.attributes.definition.from = this.first_occurance().get('start');
		this.attributes.definition.to = this.attributes.definition.from.clone().add(this.attributes.range_duration, 's');

		this.attributes.definition.duration_min = this.first_occurance().get('duration') - this.attributes.duration_interval;
		this.attributes.definition.duration_max = this.first_occurance().get('duration') + this.attributes.duration_interval;
	},

	// returns array of intervals for each period occurence of its range (to-from + period offset)
	get_ranges_intervals: function() {
		var intervals = [];
		for (var i = this.attributes.occurances.models.length - 1; i >= 0; i--) {
			var model_start = this.attributes.occurances.models[i].get('start');

			intervals.push(
				new Interval({
					start: model_start,
					end: model_start.clone().add(this.attributes.range_duration, 's')
				})
			);
			
		};

		return intervals;
	},

	// returns domain definition for predefined values of hours and weekdays
	set_domain: function() {
		// parse hours and weekdays sets to domain template definition
		if (this.attributes.domain_template_id) {
			this.attributes.definition.domain_template = {type: 'database', data: {id: this.attributes.domain_template_id}};
			return;
		} else {

			// create weekdays
			var weekdays_actions = [];
			for (var i = this.attributes.weekdays.length - 1; i >= 0; i--) {
				weekdays_actions.push({action: 'add', domain: this.domain_templates_weekdays[this.attributes.weekdays[i]]});
			};

			// create times in day
			var time_actions = [];
			for (var i = this.attributes.hours.length - 1; i >= 0; i--) {
				time_actions.push({action: 'add', domain: this.domain_templates_daytime[this.attributes.hours[i]]});
			};

			this.attributes.definition.domain_template = {type: 'stack', data:{actions : [
				{action: 'mask', domain: {type: 'stack', data:{actions:time_actions}} },
				{action: 'add', domain: {type: 'stack', data:{actions:weekdays_actions}}}
				]}};
		}

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

});

return FloatingActivityStub;

});