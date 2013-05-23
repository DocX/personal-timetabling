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
	for (var i = this.attributes.occurances.length - 1; i >= 1; i--) {
		this.attributes.occurances.remove(this.attributes.occurances.at(i));
	};

  	if (this.attributes.repeating != false ) {
  		// generate repeating
  		var occurance_date = this.first_occurance().get('start').clone()
  			.add(this.attributes.repeating.period_unit, this.attributes.repeating.period_duration);

  		var end_date = this.attributes.repeating.until_type == 'date' ? 
  			this.attributes.repeating.until_date :
  			this.first_occurance().get('start').clone().add(this.attributes.repeating.period_unit, this.attributes.repeating.period_duration * (Number(this.attributes.repeating.until_repeats) - 1));

  		while(occurance_date.isBefore(end_date) || occurance_date.isSame(end_date)) {
  			var repeating_occurance = this.first_occurance().clone();
  			repeating_occurance.set('start', occurance_date)

  			this.attributes.occurances.add(repeating_occurance);

  			occurance_date.add(this.attributes.repeating.period_unit, this.attributes.repeating.period_duration);
  		};
  	}

  	this.attributes.definition.repeating = this.attributes.repeating;
  	this.attributes.definition.from = this.first_occurance().get('start');
  	this.attributes.definition.to = this.first_occurance().get('end');
  },

});

return FixedActivityStub;

});