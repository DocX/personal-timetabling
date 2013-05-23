// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var moment = require('moment'),
    TimeColumnGeometryBase = require('views/columns_view.geometry_base');


return TimeColumnGeometryBase.extend({
  line_seconds: 86400,

  get_name: function() {
    return 'months';
  },

  constructor: function(view, model) {
    this.base(view, model);
    
  }, 
  
  global_geometry:  {
    column_max_lines : 31,
    supercolum_min_cols: 3,
  },

  // returns view mode's global geometry, immutable by the time
  // geometry = {column_max_lines: number, supercolum_max_cols: number}
  get_global_geometry: function() {
    return this.global_geometry;
  },
  
  get_line_of_date: function(date) {
    // first day of the month
    var start_month = date.clone().startOf('month');
    return {column_id: start_month.valueOf(), line: date.diff(start_month, 'days', true)};
  },
  
  next_column: function(date) { return moment.utc(date).add('month',1).valueOf(); },
  
  prev_column: function(date) { return moment.utc(date).add('month',-1).valueOf(); },
  
  column_spec: function(date_id) {
    var date = moment.utc(date_id);
    
    var lines = [];
    var line_date = date.clone();
    var month_days = date.daysInMonth();

    for(var i = 0; i<month_days; i++) {
      var sun_or_sat = ((line_date.day() +6) % 7) >= 5;

      lines.push({label: line_date.format("D."), style: sun_or_sat ? 'shaded' : ''});
      line_date.add('d',1);
    }
    return {
        id: date_id,
        title: date.format('MM/YYYY'),
        lines_labels: lines
      };
  },
  
  get_super_columns: function(column_start_id, columns) {
    // get left edge of column on the left edge of first column
    var column_date = moment.utc(column_start_id);
    var left_date = column_date.clone().startOf('month');
    left_date.add('months',  - ((left_date.month()) % 3))
    
    var end_date = column_date.clone().add('months',columns);
    var supercols = [];
    
    while(end_date.isAfter(left_date)) {
      // get years first day week - should be sure before left_date
    
      supercols.push({
        supercol_start_column: left_date.valueOf(),
        start_part: 0,
        label: 'Q'+(Math.floor((left_date.month()) / 3) + 1)+'/'+left_date.format("YYYY")});
      
      left_date.add('months', 3);
    }
    
    return supercols;
  },
  
  
  
  get_date_of_line: function(column_id, line, round_to_minutes) {
    var date = moment.utc(column_id);
    
    if (!round_to_minutes)
      round_to_minutes = 60;
    
    date.add('minutes', Math.round(line * 1440 / round_to_minutes) * round_to_minutes);
    return date;
  }
});

});