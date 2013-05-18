// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
    ActivityOccurance = require('models/activity_occurance');

return Backbone.Collection.extend({

  url: '/occurances/in_range',
  
  model: ActivityOccurance,
  
  fetchRange: function(start, end) {
    return this.fetch({remove: false, data: {start: start.toJSON(), end: end.toJSON()}});
  },
  
  inRange: function(start, end) {
    return this.filter(function(o) {return o.inRange(start,end);});
  }
  
});

});