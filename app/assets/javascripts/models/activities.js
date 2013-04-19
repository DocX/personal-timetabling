

PersonalTimetabling.Models.ActivityOccurance = Backbone.RelationalModel.extend({
  
  validate: function(attrs, options) {
    if (attrs.end < attrs.start) {
      return "can't end before it starts";
    }
  },

  // occurance is partialy or full in the given time range
  inRange: function(rangestart, rangeend) {
    var start = this.get("start");
    var end = this.get("end");
    
    return (start >= rangestart && start <= rangeend ) || (end <= rangeend && end >= rangestart) || (start < rangestart && end > rangeend)
  },

  parse: function(data, options) {
    data.start = new Date(data.start);
    data.end = new Date(data.end);
    return data;
  }

});


// High-level representation of activity
PersonalTimetabling.Models.Activity = Backbone.Model.extend({

  relations: [
    {
      type: Backbone.HasMany,
      key: 'occurances',
      relatedModel: 'PersonalTimetabling.Models.ActivityOccurance',
      collectionType: 'Backbone.LocalRelationalCollection',
      reverseRelation: {
        key: 'activity',
      },
      parse: true,
    }
  ],
  
  defaults: {
    name: "",
    description: "",
    type: 'Fixed', /* others: 'Fluent', 'Repeating', 'Longterm' */
    data: {},
  },

  getOccurancesInRange: function(start, end) {
    return this.get('occurances');
  }

}, {
  createFixed: function(name, description, start, end) {
    if (typeof name === 'object') {
        end = name.end;
        start = name.start;
        description = name.description,
        name = name.name;
    }
    
    var fixed = new PersonalTimetabling.Models.Activity({
      name: name,
      description: description,
      type: 'Fixed',
    });
    
    var occurance = new PersonalTimetabling.Models.ActivityOccurance({
      start: start,
      end: end, 
      activity: fixed
    });
    
    return fixed;
  },
});

Backbone.LocalRelationalCollection = Backbone.Collection.extend({

  localStorageBelongsTo: function(model) {
    return model.collection.relation.instance;
  },

});

PersonalTimetabling.Models.ActivityCollection = Backbone.Collection.extend({

  model: PersonalTimetabling.Models.Activity,

  withOccurancesInRange: function(start, end) {
    return this.models;
  }
});