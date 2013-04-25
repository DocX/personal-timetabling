

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
  
  defaults: function() { return {
    start: moment.utc(),
    duration: 3600
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
  
  urlRoot: '/occurances', 
});


// High-level representation of activity
PersonalTimetabling.Models.Activity = Backbone.RelationalModel.extend({

  urlRoot: '/activities', 
  
  defaults: {
    name: "",
    description: "",
    type: 'Fixed', /* others: 'Fluent', 'Repeating', 'Longterm' */
    data: {},
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
  createFixed: function(name, description, start, duration) {
    if (typeof name === 'object') {
        duration = name.duration;
        if (moment.isMoment(name.end))
          duration = name.end;
        start = name.start;
        description = name.description,
        name = name.name;
    }
    
    // duration is containing end date
    if (moment.isMoment(duration))
    {
      duration = duration.diff(start, 's');
    }
    

    var fixed = new PersonalTimetabling.Models.Activity({
      name: name,
      description: description,
      type: 'Fixed',
    });

    var occurance = new PersonalTimetabling.Models.ActivityOccurance({
      start: start,
      duration: duration, 
      activity: fixed
    });
    
    return fixed;
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
    return this.fetch({data: {start: start.toJSON(), end: end.toJSON()}});
  },
  
  inRange: function(start, end) {
    return this.filter(function(o) {return o.inRange(start,end);});
  }
  
})

PersonalTimetabling.Models.ActivityCollection = Backbone.Collection.extend({

  url: '/activities',
  
  model: PersonalTimetabling.Models.Activity,

  withOccurancesInRange: function(start, end) {
    return this.models;
  }
});