// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Base = require('base'),
    moment = require('moment');    

// represents view geometry. translates view columns/lines to/from dates and handles column labels
return Base.extend( {

  constructor: function(view, model) {
    this.view = view;
    this.model = model;
  },

  // returns view mode's global geometry, immutable by the time
  // geometry = {column_max_lines: number, supercolum_max_cols: number}
  get_global_geometry: function() {  },
  
  // returns number column specification to the right from position corresponding to from_id + offset columns
  // column specifications = [{id: id, title: "", lines_labels: ["", "", ...]}, ...]
  get_columns: function(number, from_id, offset) {
    // id is column's top edge Date
    
    // go to first date
    var date = this.get_offset_column(from_id, offset);
    
    var columns = [];
    while(number-- > 0) {
      columns.push(this.column_spec(date));
      date = this.next_column(date);
    }
    return columns;
  },
    
  // gets column which is number of columns apart from given column
  get_offset_column: function(column, offset) {
    var date = column;
    var direction = offset < 0 ? -1 : 1;
    offset = offset * direction;
    while(offset>0) {
      offset -= 1;
      date = direction > 0 ? this.next_column(date) : this.prev_column(date);
    }
    return date;
  },
  
  // returns array of line start and end for each column that given interval covers
  get_lines_for_interval: function(from, to) {
    var start_line = this.get_line_of_date(from);
    var end_line = this.get_line_of_date(to);

    var columns = [];
    var current_start = start_line;
    while (current_start.column_id != end_line.column_id) {
      // get end of current_start's column
      var current_column_end = {column_id: current_start.column_id, line: this.column_spec(current_start.column_id).lines_labels.length};
      columns.push([current_start, current_column_end]);
      current_start = {column_id: this.next_column(current_start.column_id), line: 0};
    }
    columns.push([current_start, end_line]);

    return columns;
  },

  // returns date of the right edge of given column
  get_end_of_column: function(column_id){
    return this.get_date_of_line(this.next_column(column_id), 0);
  },

  /* abstract protected methods */

  // return supercolumns grouping columns between given bounds.
  // supercolumns are specified as array of their starts in column_id and part of that column
  // supercolumns = [{supercol_start_column: id, start_part: part, label: ""}, ...]
  get_super_columns: function(column_start_id, columns) { throw 'Not implemented'; },
  
  get_date_of_line: function(column_id, line) { throw 'Not implemented'; },
  
  // returns line coordinates = {column_id: id, line: number}
  get_line_of_date: function(date) { throw 'Not implemented';  },
  
  // get date representation of center of given column
  get_center_date: function(column_id) { throw 'Not implemented'; },

  // should return column_id of the next column to given  
  next_column: function(col_id) {throw 'Not implemented'; },
      
  // should return column_id of the previous column to given                                                                                  
  prev_column: function(col_id) {throw 'Not implemented'; },
  
  // should return specification for given column
  column_spec: function(col_id) {throw 'Not implemented'; },
});

});
