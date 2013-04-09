

PersonalTimetabling.Models.ActivityOccurance = Backbone.Model.extend({

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
PersonalTimetabling.Models.FixedActivity = Backbone.Model.extend({

  constructor: function(values) {
    if (values && 'start' in values && 'end' in values) {
      values.occurance = new PT.Models.ActivityOccurance({start: values.start, end: values.end});
      delete values.start;
      delete values.end;
    }

    Backbone.Model.apply(this, arguments);
  },

  parse: function(data, options) {
    data.occurance = new PT.Models.ActivityOccurance(data.occurance, {parse:true});
    return data;
  },

  getOccurancesInRange: function(start, end) {
    if (!this.has("occurance"))
      return [];

    var fixedOccurance = this.get("occurance");
    
    if (fixedOccurance.inRange(start, end)) {
      return [fixedOccurance];
    }
    return [];
  }

});

PersonalTimetabling.Models.ActivityCollection = Backbone.Collection.extend({

  /*initialize: function() {
    // sample stuff
    // TODO only once
    if (this.length == 0) {
  this.create({
    name: "Lunch",
    description:"Lunch at Burger King",
    occurance: new PT.Models.ActivityOccurance({start: new Date("2013/03/24 11:15:00"), end: new Date("2013/03/24 13:15:00")})});
    this.create({
      name: "Lunch",
      description:"Lunch at Vegan bistro",
      occurance: new PT.Models.ActivityOccurance({start: new Date("2013/03/25 11:15:00"), end: new Date("2013/03/25 13:15:00")})});
      this.create({
        name: "Something",
        occurance: new PT.Models.ActivityOccurance({start: new Date("2013/03/28 19:35:00"), end: new Date("2013/03/28 21:15:00")})});
    }
    
  },*/

  model: PersonalTimetabling.Models.FixedActivity,

  localStorage: new Backbone.LocalStorage("activities"),

  withOccurancesInRange: function(start, end) {
    return this.models;
  }

});