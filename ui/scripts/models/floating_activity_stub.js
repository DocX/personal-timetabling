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
				duration_min: 3600,
				duration_max: 3600,
				domain_template: {}
			},
			// stub repeating
			repeating: false,
			// duration of domain range. is added to start when moving with first occurance
			range_duration: 86400 * 7,
			duration_min: 3600,
			duration_max:3600,
			// hash containing domain template definition
			domain_template: {},
		}
	},


	initialize: function() {
		FixedActivityStub.prototype.initialize.apply(this);
		this.on('change:range_duration', this.set_repeating, this);
		this.on('change:duration_max', this.set_repeating, this);
		this.on('change:domain_template', this.set_definition, this);
	},

	// updates definition Hash from stub values
	set_definition: function() {
		this.attributes.definition.repeating = this.attributes.repeating;
		this.attributes.definition.from = this.first_occurance().get('start');
		this.attributes.definition.to = this.attributes.definition.from.clone().add(this.attributes.range_duration, 's');

		this.attributes.definition.duration_min = this.attributes.duration_min;
		this.attributes.definition.duration_max = this.first_occurance().get('duration');

		this.attributes.definition.domain_template = this.attributes.domain_template; 
	},

	// returns array of intervals for each period occurence of its range (to-from + period offset)
	get_ranges_intervals: function() {
		var intervals = [];
		for (var i = this.get('events').models.length - 1; i >= 0; i--) {
			var model_start = this.get('events').models[i].get('start');

			intervals.push(
				new Interval({
					start: model_start,
					end: model_start.clone().add(this.attributes.range_duration, 's')
				})
			);
			
		};

		return intervals;
	},	

});

return FloatingActivityStub;

});