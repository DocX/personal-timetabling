// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
    BackboneRelational = require('backbone.relational'),
    Activity = require('models/activity'),
    Interval = require('models/interval'),
    moment = require('moment');

// AKA Event
var ActivityOccurance = Backbone.RelationalModel.extend({
  
  relations: [
    {
      type: Backbone.HasOne,
      key: 'activity',
      relatedModel: Activity,
      collectionType: Activity.ActivityRelationalCollection,
      reverseRelation: {
        key: 'events',
        type: Backbone.HasMany
      },
      keySource: 'activity_id',
      parse: true,
      autoFetch: {
        success: function(model) {
          model.get('events').each(function(o) {o.collection.trigger('related:activity:fetch')});
        }
      }
    }
  ],
  
  initialize: function() { 
    this.domain_intervals = new ActivityOccurance.OccuranceDomainCollection(null, {id: this.id});
  },

  defaults: function() { return {
    start: moment.utc(),
    duration: 3600,
    
  }},
  
  validate: function(attrs, options) {
    if(attrs.end) {
      attrs.duration = attrs.end.diff(attrs.start, 's');
      delete attrs.end;
    }
    
    if (attrs.duration <= 0) {
      return "can't have zero or less duration";
    }
  },

  get: function(attribute) {
    switch(attribute){
      case 'end':
        return this.end();
      default:
        return Backbone.RelationalModel.prototype.get.apply(this, arguments);
    }
  },

  set: function(attribute, value) {
    switch(attribute){
      case 'end':
        return this.end(value);
      case 'start':
        value = moment.utc(value).clone();
        break;
    }
    return Backbone.RelationalModel.prototype.set.call(this, attribute, value);
  },

  
  end: function (value) {
    if (value != undefined) {
      return this.set('duration', moment.utc(value).diff(this.get('start'), 'seconds'));
    }
    return this.get('start').clone().add('s', this.get('duration')); 
  },
  
  // occurance is partialy or full in the given time range
  inRange: function(rangestart, rangeend) {
    var start = this.get("start");
    var end = this.end();
    
    // occurance starts or ends in the range
    // or range starts or ends in the occurance
    return (start.isBefore(rangeend) && end.isAfter(rangestart)) || 
      (rangestart.isBefore(end) && rangeend.isAfter(start));
  },

  parse: function(data, options) {
    data.start = moment.utc(data.start);
    data.duration = Number(data.duration);
    if (data.end) {
      data.duration = moment.utc(data.end).diff(data.start,'seconds');
      delete data.end;
    }
    return data;
  },

  validDuration: function(duration){
    var max_duration = this.get('max_duration');
    return this.get('min_duration') <= duration && (max_duration == -1 || max_duration >= duration);
  },

  isFeasible: function(start, duration) {
    if (this.fixed === true) {
      return (start.isSame(this.get('start')) && duration == this.get('duration'));
    }
    if (typeof this.fixed == 'function') {
      return this.fixed(start, duration);
    }

    return this.domain_intervals.isFeasible(start, duration) && this.validDuration(duration);
  },
  
  urlRoot: '/events', 
},{
  OccuranceDomainCollection: Backbone.Collection.extend({

    url: '/events/<%= occurance_id %>/domain_intervals',

    initialize: function(models, options) {
      this.url = _.template(this.url, {occurance_id: options.id});
    },
    
    fetched: false,
    model: Interval,
    
    fetchRange: function(start, end) {
      var xhr = this.fetch({data: {from: start.toJSON(), to: end.toJSON()}});
      xhr.success(_.bind(function() {this.fetched = true;}, this));
      return xhr;
    },

    // determine if interval given by start date and duration in seconds
    // is inside any of intervals in this collection (which is mostly intervals for currently displayed window)
    isFeasible: function(start, duration) {
      return !this.fetched || this.any(function(i) {return i.isInInterval(start, duration);});
    }
    
  }),


  OccurancesRalationCollection: Backbone.Collection.extend({

    url: function(relatedModels) {
      return '/events/list/'+_.pluck(relatedModels,'id').join(';')
    },

  })


});

return ActivityOccurance;

});

