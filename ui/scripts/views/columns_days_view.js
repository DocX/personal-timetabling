// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment'),
    ColumnsView = require('views/columns_view'),
    DayColumnGeometry = require('views/columns_days_view.day_geometry'),
    WeekColumnGeometry = require('views/columns_days_view.week_geometry'),
    MonthColumnGeometry = require('views/columns_days_view.month_geometry');  

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
var ColumnsDaysView;
return ColumnsDaysView = ColumnsView.extend({
  options: {
      initial_date: moment.utc()
  },
      
  initialize: function() {
    ColumnsView.prototype.initialize.apply(this);
      
    this._set_geometry( new DayColumnGeometry(this,null) );

    this._set_zoom(0, false, true);
    this.drawing_columns_list = [{column_id: this.geometry.get_line_of_date(this.options.initial_date).column_id}];

    this.render();
    this.display_date(this.options.initial_date);
  },
 
  // set zoom
  set_zoom: function(e, ui) {
    this._set_zoom(ui.value, true, true);
  },
 
  set_geometry: function(geometry) {
    var date = this.date_in_center();
    this._set_geometry(geometry);
    this.drawing_columns_list[0].column_id = this.geometry.get_line_of_date(date).column_id;
    this.render();
    this.display_date(date);

    this.trigger('geometry_changed');
  },
  
  // move view center to given date
  display_date: function(date) {
    var date_pos = this.geometry.get_line_of_date(date);
    var column_index = Math.floor(this.drawing_columns/2);
    this.update_columns(this.geometry.get_columns(
      this.drawing_columns,
      date_pos.column_id,
      -column_index)
    );
    
    // wanted date is in column column_index and in part determined by date_pos.line
    var part = date_pos.line / this.drawing_columns_list[column_index].lines_labels.length;
    
    // scroll to center
    var offset = -((column_index + part) * this.drawing_column_width) + (this.drawing_view_width/2);
    this.scrollTo(offset);
  },
  
  date_in_center: function() {
    var current_center_column = (-this.container_offset()  + (this.drawing_view_width/2)) / this.drawing_column_width;
    var column_index = Math.ceil(current_center_column);
    
    var center =  this.geometry.get_date_of_line(
      this.drawing_columns_list[column_index].column_id,
      (current_center_column - column_index) * this.drawing_columns_list[column_index].lines_labels.length
    );
    
    console.log("date in center", center);
    return center;
  },
  

  set_column_type: function(type) {
    var change = false;

    switch(type) {
      case 'days':
        if (!(this.geometry instanceof DayColumnGeometry)) {
          this.set_geometry(new DayColumnGeometry(this, null));
          change = true;
        }
        this.column_step_minutes = 15;      
        break;
      case 'weeks':
        if (!(this.geometry instanceof WeekColumnGeometry)) {
          this.set_geometry(new WeekColumnGeometry(this, null));
          change = true;
        }        
        this.column_step_minutes = 60;
        break;
      case 'months':
        if (!(this.geometry instanceof MonthColumnGeometry)) {
          this.set_geometry(new MonthColumnGeometry(this, null));
          change = true;
        }        
        this.column_step_minutes = 720;
        break;
    }

    this._set_zoom(0, false, false);    
  },

  _set_zoom: function(zoom, redraw, dont_update_slider) {
    
    
    this.zoom = zoom;
    
    if (redraw) {
      this.resize();
    }
    
    if (!dont_update_slider) {
      this.trigger('zoom_changed', this.zoom);
    }
  },
  
  resize: function() {
    var max_lines = this.geometry.get_global_geometry().column_max_lines;
    
    // set height of lines
    // minimum is to be fit to window height or 50px
    var max_height = 500;
    var min_height = Math.min(max_height, Math.max(25, this.container_window_lines_size() / max_lines));
    
    
    // compute height as linear function of zoom between min and max height
    this.drawing_column_line_height = min_height + ((max_height - min_height) * (this.zoom / 1000));              

    ColumnsView.prototype.resize.apply(this);      
  },
  
  // returns hash with the range of rendered columns time as 'start' and 'end' date attributes
  showing_dates: function() {
    var first_col_id = this.drawing_columns_list.first().column_id;
    var last_col_id =  this.drawing_columns_list.last().column_id;
    var start_date = this.geometry.get_date_of_line(first_col_id,0);
    var end_date = this.geometry.get_date_of_line(last_col_id,     this.drawing_columns_list.last().lines_labels.length);

    return {start: start_date, end:end_date};
  },
  
  
  /* INTERVALS HANDLING */
  
  clear_intervals: function(selector) {
    // just clear overlay div
    this.$grid_overlay_el.find(selector).remove();
  },
  
  
  add_interval_box: function(box) {
    this.$grid_overlay_el.append(box);
  },
  
  render_interval: function(from, to, box_setup_func) {
    // get splits to columns intervals for given interval
    var columns_for_interval = this.geometry.get_lines_for_interval(from, to);

    // walk throu column intervals and make boxes
    var boxes = [];
    for (var i = 0; i < columns_for_interval.length; i++) {
      
      var start = columns_for_interval[i][0];
      var end = columns_for_interval[i][1];

      var box = $("<div/>");
      this.set_box_offset_and_size_for_column(box, start.line, end.line - start.line);
      
      // set column position and size for box
      var column = this.drawing_columns_list.find(function(c) {return c.column_id == start.column_id});
      if (column == undefined)
        continue;

      box.css(this.columns_size_attr, this.drawing_column_width);
      box.css(this.columns_offset_attr, column.$column.position()[this.columns_offset_attr]);
      box.css('position', 'absolute');

      box_setup_func && box_setup_func(box);
      boxes.push(box);

      this.$grid_overlay_el.append(box);
    };

    return boxes;
  },

  // creates DOM elements on appropriate places for given interval
  // and returns object for handling them
  display_interval: function(from, to, box_setup_func) {
    var boxes = this.render_interval(from, to, box_setup_func);

    var handle = new ColumnsDaysView.IntervalDisplayHandle({
      'boxes':boxes, 
      'interval_start': from,
      'interval_end': to, 
      'box_setup_func': box_setup_func,
      'view': this
    });

    return handle;
  },

  // renders all intervals in given array. 
  // returns array with element for each interval, which is array of DOMs objects of that interval
  display_intervals: function(intervals, box_setup_func) {
    var intervals_boxes = [];
    for (var i = intervals.length - 1; i >= 0; i--) {
        var boxes = this.display_interval(
          intervals[i].get('start'),
          intervals[i].get('end'),
          box_setup_func
        );  
        intervals_boxes.push(boxes);
    };
    return intervals_boxes;
  }
}, {

  // simple handle wrapping internal behaviour of interval display
  IntervalDisplayHandle: Backbone.Model.extend({
    
    default: {
      boxes: null,
      interval_start:null,
      interval_end:null,
      box_setup_func:null,
      view: null,
    },

    initialize: function() {
      this.listenTo(this.attributes.view, 'columns_updated', this.render);
    },
    
    render: function() {
      this.attributes.boxes = this.attributes.view.render_interval(
        this.attributes.interval_start, 
        this.attributes.interval_end, 
        this.attributes.box_setup_func
        );
    },

    remove: function() {
      if (this.attributes.boxes == null) 
        return;

      for (var i =  this.attributes.boxes.length - 1; i >= 0; i--) {
          this.attributes.boxes[i].remove();
      };

      this.attributes.boxes = null;

      this.attributes.view = null;

      this.stopListening();

    }
  })
});

});