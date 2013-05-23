// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var moment = require('moment'),
    TimeColumnGeometryBase = require('views/columns_view.geometry_base');


return TimeColumnGeometryBase.extend({
  line_seconds: 86400 * 7,

  get_name: function() {
    return 'weeks';
  },

  constructor: function(view, model) {
    this.base(view, model);
    
  }, 
  
  global_geometry:  {
    column_max_lines : 7,
    supercolum_min_cols: 4,
  },

  // returns view mode's global geometry, immutable by the time
  // geometry = {column_max_lines: number, supercolum_max_cols: number}
  get_global_geometry: function() {
    return this.global_geometry;
  },
  
  get_line_of_date: function(date) {
    // get monday of the week in which date is
    var week_start;
    if (date.day() == 0) {
      week_start = date.clone().day(-7).startOf('week').day(1);
    } else {
      week_start = date.clone().startOf('week').day(1);
    }
    
    return {column_id: week_start.valueOf(), line: date.diff(week_start, 'hours', true) / 24};
  },
  
  next_column: function(date) { return moment.utc(date).add('w',1).valueOf(); },
  
  prev_column: function(date) { return moment.utc(date).add('w',-1).valueOf(); },
  
  column_spec: function(date_id) {
    var date = moment.utc(date_id);
    
    var lines = [];
    var line_date = date.clone();
    for(var i = 0; i<7; i++) {
      var sun_or_sat = ((line_date.day() +6) % 7) >= 5;
      lines.push({label: line_date.format("ddd D.M."), style: sun_or_sat ? 'shaded' : ''});
      line_date.add('d',1);
    }
    var week = date.isoWeek();
    return {
        id: date_id,
        title: (week + "/" + date.day(4).year()),
        lines_labels: lines
      };
  },
  
  get_super_columns: function(column_start_id, columns) {
    // get left edge of column on the left edge of first column
    var column_date = moment.utc(column_start_id);
    var left_date = column_date.clone().startOf('month');
    
    var end_date = column_date.clone().add('w',columns);
    var supercols = [];
    
    while(end_date.isAfter(left_date)) {
      // get months first day week - should be sure before left_date
      var week;
      if (left_date.day() == 0) {
        week = left_date.clone().day(-7).day(1);
      } else {
        week = left_date.clone().day(1);
      }
    
      supercols.push({
        supercol_start_column: week.valueOf(),
        start_part: left_date.diff(week, 'weeks', true),
        label: left_date.format("MMM YYYY")});
      
      left_date.add('month', 1);
    }
    
    return supercols;
  },
  
  
  
  get_date_of_line: function(column_id, line, round_to_minutes) {
    var date = moment.utc(column_id);
    
    if (!round_to_minutes)
      round_to_minutes = 1;
    
    date.add('minutes', Math.round(line * 1440 / round_to_minutes) * round_to_minutes);
    return date;
  }
});

});