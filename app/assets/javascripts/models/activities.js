

PersonalTimetabling.Models.ActivityOccurance = Backbone.RelationalModel.extend({
  
  defaults: function() { return {
    start: new Date(),
    duration: 3600
  }},
  
  validate: function(attrs, options) {
    if(attrs.end) {
      attrs.duration = attrs.start.secondsUntil(attrs.end);
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
   return this.get('start').clone().addSeconds(this.get('duration')); 
  },
  
  // occurance is partialy or full in the given time range
  inRange: function(rangestart, rangeend) {
    var start = this.get("start");
    var end = this.end();
    
    return (start >= rangestart && start <= rangeend ) || (end <= rangeend && end >= rangestart) || (start < rangestart && end > rangeend)
  },

  parse: function(data, options) {
    data.start = new Date(data.start);
    data.duration = Number(data.duration);
    if (data.end) {
      data.duration = data.start.secondsUntil(data.end);
    }
    return data;
  },
  
  urlRoot: '/occurances', 
});


// High-level representation of activity
PersonalTimetabling.Models.Activity = Backbone.RelationalModel.extend({

  relations: [
    {
      type: Backbone.HasMany,
      key: 'occurances',
      relatedModel: 'PersonalTimetabling.Models.ActivityOccurance',
      collectionType: 'PersonalTimetabling.Models.OccurancesCollection',
      reverseRelation: {
        key: 'activity',
      },
      keySource: 'occurance_ids',
      keyDestination: 'occurances',
      parse: true,
    }
  ],
  
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
        if (name.end instanceof Date)
          duration = name.end;
        start = name.start;
        description = name.description,
        name = name.name;
    }
    
    if (duration instanceof Date)
    {
      duration = start.secondsUntil(duration);
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

PersonalTimetabling.Models.OccurancesCollection = Backbone.Collection.extend({

  url: function(relatedModels) {
    return '/occurances/list/'+_.pluck(relatedModels,'id').join(';')
  },

});

PersonalTimetabling.Models.ActivityCollection = Backbone.Collection.extend({

  url: '/activities',
  
  model: PersonalTimetabling.Models.Activity,

  withOccurancesInRange: function(start, end) {
    return this.models;
  }
});