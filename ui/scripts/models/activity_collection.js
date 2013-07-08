// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
    Activity = require('models/activity');

return Backbone.Collection.extend({

  url: '/activities',
  
  model: Activity,

  fetchInRange: function(start, end) {
  	return this.fetch();
  }
});

});