// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var moment = require('moment'),
    TimeColumnGeometryBase = require('views/columns_view.geometry_base');

return TimeColumnGeometryBase.extend({
  constructor: function(view, model,  hours_lower, hours_upper) {
    this.base(view, model);
    
  }, 
  
  lines_prototype: function() {
    var lines = [], count = 24, line_hour = 0;
    while(count-- > 0) {
      lines.push(Math.floor(line_hour).pad(2) + "h");
      line_hour += 1;
    }
    return lines;
  }(),
  
  global_geometry:  {
    column_max_lines : 24,
    supercolum_min_cols: 7,
  },

  // returns view mode's global geometry, immutable by the time
  // geometry = {column_max_lines: number, supercolum_max_cols: number}
  get_global_geometry: function() {
    return this.global_geometry;
  },
  
  next_column: function(date) { return moment.utc(date).add('d',1).valueOf(); },
  
  prev_column: function(date) { return moment.utc(date).add('d',-1).valueOf(); },
  
  column_spec: function(date_id) {
    var date = moment.utc(date_id).startOf('day');
    var sun_or_sat = ((date.day() +6) % 7) >= 5;
    return {
        id: date_id,
        title: date.format("ddd D.[&nbsp;]M.[&nbsp;]YYYY"),
        lines_labels: this.lines_prototype.map(function(el) {return {label: el, style: sun_or_sat ? 'shaded' : ''};})
      };
  },
  
  get_super_columns: function(column_start_id, columns) {
    // get left edge of column on the left edge of first column
    column_start_id = moment.utc(column_start_id);
    var left_date = column_start_id.clone().startOf('week').day(1);
    
    var end_date = column_start_id.clone().add('d', columns);
    var supercols = [];
    
    while(end_date.isAfter(left_date)) {
      var week = left_date.clone().day(1);
      supercols.push({
        supercol_start_column: left_date.valueOf(),
        start_part: 0,
        label: "Week " + week.isoWeek()  + "/" + week.year()});
      
      left_date = left_date.add('w',1);
    }
    
    return supercols;
  },
  
  get_line_of_date: function(date) {
    return {column_id: date.clone().startOf('day').valueOf(), line: date.hour() + (date.minute()/60)};
  },
  
  get_date_of_line: function(column_id, line, round_to_minutes) {
    var date = moment.utc(column_id);
    
    if (!round_to_minutes)
      round_to_minutes = 1;
    
    var minutes_since_zero = Math.round((line * 60) / round_to_minutes) * round_to_minutes;
    
    date.hours(Math.floor(minutes_since_zero / 60))
    date.minutes(minutes_since_zero % 60);
    return date;
  }
});



});