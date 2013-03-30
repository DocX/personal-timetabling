// (c) 2013 Lukas Dolezal

// Time domain represents structure for storing and quering to time intervals
PersonalTimetabling.Models.TimeDomain = Backbone.Model.extend({

    constructor: function() {
        this.layers = [];

        Backbone.Model.apply(this, arguments);
    },
    
});

// Repeatable interval. Represents time interval information that can be periodicaly repeated
// There is 2 fundamental problems to repeated intervals (when we want to describe intervals in DAYS, MONTHS etc units):
// - Actual (in ammount of seconds) length of interval can vary depending on time where
//   it is placed (DAY is 23 or 25 hours in DST switch days, MONTH is always variable, etc)
// - Actual distance of period can vary, similary to previous problem.
PersonalTimetabling.Models.RepeatableInterval = Backbone.Model.extend({

    defaults: {
      // number of periods between two instances of series
      each_period: 0,

      // period unit
      period_unit: 0,

      // repeat until this date
      repeat_end_date: null,

      // interval start of first occurance
      first_interval_start: null,

      // interval end of first occurance
      first_interval_end: null,
    },
    
    getAll: function() {
        // todo get all intervals
    },

    getNearest: function(date) {
        // get nearest interval to given date
    },

    contains: function(date) {
        // true false
    },

    
    
}, {
    // "enum" of available repeating period uint
    PERIOD_UNITS = {
        // pure time period
        SECONDS:0,
        // day is period from 0:00 of one day to 0:00 the next day time as seen by user. ie it can vary in actual duration
        DAY: 1,
        // month is period from 1. day 0:00 to 1.day 0:00 of next month.
        MONTH: 5,
        // year is period from 1.1. 0:00 one year to 1.1. 0:00 the next year
        YEAR: 6
    },
});