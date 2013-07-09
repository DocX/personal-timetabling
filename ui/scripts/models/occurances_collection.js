// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
    ActivityOccurance = require('models/activity_occurance');

return Backbone.Collection.extend({

  url: '/events/in_period',
  
  model: ActivityOccurance,
  
  fetchRange: function(start, end, remove) {
    return this.fetch({remove: remove == true, data: {from: start.toJSON(), to: end.toJSON()}});
  },
  
  inRange: function(start, end) {
    return this.filter(function(o) {return o.inRange(start,end);});
  }
  
});

});