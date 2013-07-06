// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
		BackboneRelational = require('backbone.relational'),
		Activity = require('models/activity'),
		ActivityOccurance = require('models/activity_occurance');

// Stub representation for creating fixed activity in UI
var FixedActivityStub = Backbone.Model.extend({

	defaults: function() {
		return {
			repeating: false,
			from: null,
			to: null,
			name: '',
		}
	},

	repeating_default: function() {
		return {
			period_duration: 1,
			period_unit: 'days',
			until_repeats: 3,
			until_date: null,
			until_type: 'repeats',
		};
	},

	get_activity_prototype: function() {
		return new Activity({
			name: this.get('name'),
			definition: {
				repeating: this.get('repeating'),
				type: 'fixed',
				from: this.get('from'),
				to: this.get('to')
			}
		});
	},

	initialize: function() {
		// create first fixed occurance
		this.activity = new Activity();

		var first_occurance = new ActivityOccurance({min_duration: 60, max_duration:-1});
		this.activity.get('events').add(first_occurance);
		this.set('from', first_occurance.get('start'));
		this.set('to', first_occurance.get('end'));

		this.on('change', this.set_repeating, this);
		this.listenTo(first_occurance, 'change', this.update_from_event);
	},

	update_from_event: function(e) {
		this.stopListening(this);
		if (this.attributes.repeating == false) {
			this.set({'from': e.get('start'), 'to': e.get('end')});
		}
		this.on('change', this.set_repeating, this);
	},

	// when repeating value is changed, setup repeated occurances
	set_repeating: function() {

		var repeats = this.get_repeating_intervals();
		var events = this.activity.get('events');

		this.stopListening(events.at(0), 'change');

		for (var i = 0; i < events.length && i < repeats.length; i++) {
			events.at(i).set('start', repeats[i].start);
			events.at(i).set('end', repeats[i].end);
			events.at(i).fixed = this.attributes.repeating != false;
		};

		// add new
		for(var i = events.length; i< repeats.length; i++) {
			var repeating_occurance = new ActivityOccurance({
				min_duration: 60, 
				max_duration:-1,
				start: repeats[i].start,
				end: repeats[i].end,
			});
			repeating_occurance.fixed = this.attributes.repeating != false;

			events.add(repeating_occurance, {at: i});			
		}

		// or remove remaining 
		var remove_count = events.length - repeats.length
		for(var i = repeats.length; i < repeats.length + remove_count ; i++) {
			events.last().destroy();
		}

		this.listenTo(events.at(0), 'change', this.update_from_event);
	},

	get_repeating_intervals: function() {
		// remove all repeated occurances
		if (this.attributes.repeating == false) {
			return [{start: this.get('from'), end:this.get('to')}];
		}
		

		var intervals = [];		
		
		// generate repeating
		var occurance_date = this.get('from').clone();
		var duration = this.get('to').diff(this.get('from'), 'seconds');

		// prepare week days for weekly repeating
		var week_days = [];
		if (this.attributes.repeating.period_unit == 'weeks') {
			week_days = this.attributes.repeating.period_unit_options.weekdays.sort();
			if (week_days.length == 0) {
				week_days = [0];
			}

			//skip to first weekday
			var weekday = occurance_date.day();
			var w;
			for (w = 0;  w < week_days.length;  w++) {
				if (week_days[ w] >= weekday) {
					break;
				}
			}

			// if w > length, move to next period (this week no day left)
			if (w >= week_days.length) {
				// next period
				occurance_date.day(week_days[0]);
				occurance_date.add(this.attributes.repeating.period_unit, this.attributes.repeating.period_duration);
			} else {
				// set next week day
				occurance_date.day(week_days[w]);
			}
		}

		// create occurrences until end condition met
		var until_date;
		if (this.attributes.repeating.until_type == 'date' ) {
			until_date = this.attributes.repeating.until.clone().startOf('day').add(1,'d');
		}

		for (var i = 0; true; i++) {
			// check if is not last
			if (this.attributes.repeating.until_type == 'date' && until_date.isBefore(occurance_date)) {
				break;
			} else if (this.attributes.repeating.until_type == 'repeats' && i >= this.attributes.repeating.until) {
				break;
			}

			intervals.push({start: occurance_date.clone(), end: occurance_date.clone().add(duration, 's')});

			//move date to next period
			if (this.attributes.repeating.period_unit == 'weeks') {
				// get next weekday from weekdays
				var weekday = occurance_date.day();
				var w;
				for (w = 0;  w < week_days.length;  w++) {
					if (week_days[ w] > weekday) {
						break;
					}
				}

				// if w > length, move to next period (this week no day left)
				if (w >= week_days.length) {
					// next period
					occurance_date.day(week_days[0]);
					occurance_date.add(this.attributes.repeating.period_unit, this.attributes.repeating.period_duration);
				} else {
					// set next week day
					occurance_date.day(week_days[w]);
				}
			} else {
				// simply next period
				occurance_date.add(this.attributes.repeating.period_unit, this.attributes.repeating.period_duration);
			}
		};

		return intervals;
	},

});

return FixedActivityStub;

});