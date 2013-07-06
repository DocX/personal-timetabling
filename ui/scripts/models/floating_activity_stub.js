// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
		BackboneRelational = require('backbone.relational'),
		FixedActivityStub = require('models/fixed_activity_stub'),
		Interval = require('models/interval'),
		Activity = require('models/activity'),
		ActivityOccurance = require('models/activity_occurance');

// Stub representation for creating fixed activity in UI
var FloatingActivityStub = FixedActivityStub.extend({
	
	defaults: function() {
		var today = moment.utc().startOf('day');
		return {
			repeating: false,

			// start of first window
			from: today,
			// end of first window
			to: today.clone().add(1,'d'),

			// duration range for events
			duration_min: 3600,
			duration_max: 3600,

			// template of domain for events
			domain_template: {},
			name: 'Floating activity'
		}
	},


	initialize: function() {
		this.activity = new Activity();

		this.on('change:from', this.set_repeating, this);
		this.on('change:to', this.set_repeating, this);

	},

	get_activity_prototype: function() {
		return new Activity({
			name: this.get('name'),
			definition: {
				repeating: this.get('repeating'),
				type: 'floating',
				from: this.get('from'),
				to: this.get('to'),
				duration_min: this.get('duration_min'),
				duration_max: this.get('duration_max'),
				domain_template: this.get('domain_template'),
			}
		});
	},

	set_repeating: function() {

	},

	// returns array of intervals for each period occurence of its range (to-from + period offset)
	get_ranges_intervals: function() {
		var repeating_intervals = this.get_repeating_intervals();

		var ranges = [];
		for (var i = 0; i < repeating_intervals.length; i++) {
			ranges.push(new Interval({
				start: repeating_intervals[i].start,
				end: repeating_intervals[i].end
				}));
		};

		return ranges;
	},	

});

return FloatingActivityStub;

});