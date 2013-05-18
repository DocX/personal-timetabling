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
    description: "",
    definition: null,
  },

  getOccurancesInRange: function(start, end, callback) {
    var suc = _.bind(function() {
      //TODO really gen only occurances in given range
      callback(this.get('occurances'));
    }, this);

    var req = this.fetchRelated('occurances', {success: suc});
    if (req.length == 0) {
      suc();
    }
  }

}, {
  // Alias for creating activity with definition containing domain with one fixed interval
  fixed: function(attributes) {
    var values = _.extend({
      name: '',
      description: '',
      start: moment.utc(),
      end: moment.utc(),
    }, attributes);

    return new Activity({
      name: values.name, 
      description: values.description,
      definition: {
        type: 'fixed',
        from: values.start.toJSON(),
        to: values.end.toJSON(),
        repeating: false
      }
    });
  },

  floating: function(attributes) {
    var values = _.extend({
      name: '',
      description: '',
      start: moment.utc(),
      duration_min: 60,
      duration_max: 120,
      domain_template_id: 0,
      period: {duration: 0, unit: 'days'}
    }, attributes);

    return new Activity({
      name: values.name,
      description: values.description,
      definition: {
      type: 'floating',
        domain_template_id: values.domain_template_id,
        from: values.start.toJSON(),
        period: {
          duration: values.period.duration,
          unit: values.period.unit
        },
        duration_min: values.duration_min,
        duration_max: values.duration_max,
      }
    });
  },

  ActivityRalationalCollection: Backbone.Collection.extend({

    url: function(relatedModels) {
      return '/activities/list/'+_.pluck(relatedModels,'id').join(';')
    },

  }),


});

return Activity;

});