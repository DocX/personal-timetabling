// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
    BackboneRelational = require('backbone.relational');

// High-level representation of activity
var Activity = Backbone.RelationalModel.extend({

  urlRoot: '/activities', 
  
  defaults: {
    name: "",
    definition: null,
    link_events: false,
    link_comparator: null
  },

}, {

  ActivityRalationalCollection: Backbone.Collection.extend({

    url: function(relatedModels) {
      return '/activities/list/'+_.pluck(relatedModels,'id').join(';')
    },

  }),


});

return Activity;

});