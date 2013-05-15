

PersonalTimetabling.Models.ActivityOccurance = Backbone.RelationalModel.extend({
  
  relations: [
    {
      type: Backbone.HasOne,
      key: 'activity',
      relatedModel: 'PersonalTimetabling.Models.Activity',
      collectionType: 'PersonalTimetabling.Models.ActivityRelationalCollection',
      reverseRelation: {
        key: 'occurances',
        type: Backbone.HasMany
      },
      keySource: 'activity_id',
      parse: true,
      autoFetch: true
    }
  ],
  
  initialize: function() { 
    this.domain_intervals = new PersonalTimetabling.Models.OccuranceDomainCollection(null, {id: this.id});
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
  
  end: function () {
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

    return this.get('min_duration') <= duration && this.get('max_duration') >= duration;
  },
  
  urlRoot: '/occurances', 
});


// High-level representation of activity
PersonalTimetabling.Models.Activity = Backbone.RelationalModel.extend({

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

    return new PersonalTimetabling.Models.Activity({
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

    return new PersonalTimetabling.Models.Activity({
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

});

PersonalTimetabling.Models.OccurancesRalationCollection = Backbone.Collection.extend({

  url: function(relatedModels) {
    return '/occurances/list/'+_.pluck(relatedModels,'id').join(';')
  },

});

PersonalTimetabling.Models.ActivityRalationalCollection = Backbone.Collection.extend({

  url: function(relatedModels) {
    return '/activities/list/'+_.pluck(relatedModels,'id').join(';')
  },

});


PersonalTimetabling.Models.OccurancesCollection = Backbone.Collection.extend({

  url: '/occurances/in_range',
  
  model: PersonalTimetabling.Models.ActivityOccurance,
  
  fetchRange: function(start, end) {
    return this.fetch({remove: false, data: {start: start.toJSON(), end: end.toJSON()}});
  },
  
  inRange: function(start, end) {
    return this.filter(function(o) {return o.inRange(start,end);});
  }
  
});

PersonalTimetabling.Models.ActivityCollection = Backbone.Collection.extend({

  url: '/activities',
  
  model: PersonalTimetabling.Models.Activity,

  withOccurancesInRange: function(start, end) {
    return this.models;
  }
});

PersonalTimetabling.Models.OccuranceDomainCollection = Backbone.Collection.extend({

  url: '/occurances/<%= occurance_id %>/domain',

  initialize: function(models, options) {
    this.url = _.template(this.url, {occurance_id: options.id});
  },
  
  model: PersonalTimetabling.Models.Interval,
  
  fetchRange: function(start, end) {
    return this.fetch({data: {start: start.toJSON(), end: end.toJSON()}});
  },

  // determine if interval given by start date and duration in seconds
  // is inside any of intervals in this collection (which is mostly intervals for currently displayed window)
  isFeasible: function(start, duration) {
    return this.any(function(i) {return i.isInInterval(start, duration);});
  }
  
});
