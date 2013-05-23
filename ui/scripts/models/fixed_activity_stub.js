// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
		BackboneRelational = require('backbone.relational'),
		Activity = require('models/activity'),
		ActivityOccurance = require('models/activity_occurance');

// Stub representation for creating fixed activity in UI
var FixedActivityStub = Activity.extend({
	
	defaults: function() {
		return {
			definition: {
				repeating: false,
				type: 'fixed',
				from: null,
				to: null,
			},
			repeating: false
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

	initialize: function() {
		// create first fixed occurance

	var first_occurance = new ActivityOccurance({min_duration: 60, max_duration:-1});
	this.get('occurances').add(first_occurance);

	first_occurance.on('change', this.set_repeating, this);

	this.on('change:repeating', this.set_repeating, this);

	},

	// retrives first occurance model instnace which acts as reference for fixed activity
	first_occurance: function() {
		return this.attributes.occurances.models[0];
	},

	// when repeating value is changed, setup repeated occurances
	set_repeating: function() {

		// remove all repeated occurances
		if (this.attributes.repeating == false) {
			for (var i = this.attributes.occurances.length - 1; i >= 1; i--) {
				this.attributes.occurances.remove(this.attributes.occurances.at(i));
			};
		}
		else {
			// generate repeating
			var occurance_date = this.first_occurance().get('start').clone()
				.add(this.attributes.repeating.period_unit, this.attributes.repeating.period_duration);

			var count = this.attributes.repeating.until_type == 'date' ? 
				this.attributes.repeating.until_date.diff(this.first_occurance().get('start'), this.attributes.repeating.period_unit) :
				(Number(this.attributes.repeating.until_repeats) - 1);

			// go thgrou count and update existing, add new or delete remaining

			var repeating_occurance;
			var occurances = this.attributes.occurances;
			var first_occurance = this.first_occurance();
			var plus_one_if_last = (occurances.models.length > 1) ? +1 : 0 ;

			var add_or_update = function(add_new, i) {
				if (add_new) {
					// add new
					var repeating_occurance = first_occurance.clone();
					repeating_occurance.set('start', occurance_date)
					repeating_occurance.fixed = true;

					// if was last, add just before it, else add as last
					occurances.add(repeating_occurance, {at: i});					
				} else {
					// update
					occurances.at(i).set('start', occurance_date);
					occurances.at(i).set('duration', first_occurance.get('duration'));
				}
			}

			// turn of listening on the last for changes during reorganization
			if(occurances.length > 1) {
				this.stopListening(occurances.last());
			}

			// proccess all except last (1..count == 0..count - 1)
			var i;
			for (i = 1; i < count; i++) {
				add_or_update (occurances.models.length - plus_one_if_last <= i, i);

				//move date to next period
				occurance_date.add(this.attributes.repeating.period_unit, this.attributes.repeating.period_duration);
			};

			// delete remaining
			for(; i < occurances.models.length - plus_one_if_last; i++) {
				occurances.at(i).destroy();
			} 

			// process last
			if (count +1  == plus_one_if_last) {
				occurances.at(1).destroy();
			} else {
				add_or_update(occurances.models.length < count+1, count);
			}

			// set last check
			if (occurances.length > 1) {
				occurances.last().fixed = _.bind(this.last_occurance_check, this);	
				this.listenTo(occurances.last(), 'change', this.last_moved);
			}
			
		}

		this.attributes.definition.repeating = this.attributes.repeating;
		this.attributes.definition.from = this.first_occurance().get('start');
		this.attributes.definition.to = this.first_occurance().get('end');
	},

	last_occurance_check: function(start, duration) {
		// compute diff of periods from first occurance start
		var from_first = start.diff(this.first_occurance().get('start'), this.attributes.repeating.period_unit, true);

		if (from_first < 1 ) {
			return false;
		}

		return (from_first % this.attributes.repeating.period_duration) < 0.00001 && duration ==  this.first_occurance().get('duration');
	},


	last_moved: function() {
		var from_first = this.attributes.occurances.last().get('start').diff(
			this.first_occurance().get('start'), 
			this.attributes.repeating.period_unit, true);

		// since last isallowed only to period jumps, just round diff to new period
		this.attributes.repeating.until_type = 'repeats';
		this.attributes.repeating.until_repeats = Math.floor(from_first / this.attributes.repeating.period_duration) + 1;

		//this.set_repeating();
		this.trigger('change:repeating', this, this.attributes.repeating);
	}

});

return FixedActivityStub;

});