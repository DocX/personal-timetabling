// (c) 2013 Lukas Dolezal
"use strict";

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.ColumnsDaysView = PersonalTimetabling.CalendarViews.ColumnsView.extend({
  options: {
      initial_date: Date.today()
  },

  buttons_template: 
    "<div id='dayincolview-buttons' class='rightcomponents'>" +
      "<div class='btn-group'>" +
        "<a class='btn dropdown-toggle btn-inverse' data-toggle='dropdown'>" +
          "Ka-Zoom-A" +
          "<span class='caret'></span>" +
        "</a>" +
        
        "<ul class='dropdown-menu'>" +
          "<li><div data-role='zoom-slider' ></div></li>" +
          "<li class='divider'></li>" +
          "<li><a href='#' data-role='zoom-to' data-zoom='600'>Days</a></li>" +
          "<li><a href='#' data-role='zoom-to' data-zoom='300'>Weeks</a></li>" +
          "<li><a href='#' data-role='zoom-to' data-zoom='0'>Months</a></li>" +
        "</ul>" +
        "</div>" +

        "<div class='btn-group'>" +
          "<button class='btn btn-inverse active' data-role='mode-horizontal'><i class='icon-white icon-pt-horizontal'></i>H</button>" +
          "<button class='btn btn-inverse' data-role='mode-vertical' ><i class='icon-white icon-pt-vertical'></i>V</button>" +
        "</div>" +        
        
        "<div class='btn-group'>" +
          "<button class='btn btn-inverse' data-role='scroll-left'><i class='icon-white icon-chevron-left'></i></button>" +
          "<button class='btn btn-inverse' data-role='scroll-right'><i class='icon-white icon-chevron-right'></i></button>" +
        "</div>" +
      "</div>",
      
    initialize: function() {
       PersonalTimetabling.CalendarViews.ColumnsView.prototype.initialize.apply(this);
      
      this.$buttons = $(this.buttons_template);
      $("#content-panel-place").empty().append(this.$buttons);

      this.$zoom_slider = this.$buttons.find("[data-role=zoom-slider]")
        .slider({min: 0, max: 899, change:_.bind(this.set_zoom, this), slide:_.bind(this.set_zoom, this), value:600});

      this.$buttons.find("[data-role=scroll-left]")
        .click(_.bind(this.move_left, this));
      this.$buttons.find("[data-role=scroll-right]")
        .click(_.bind(this.move_right, this));
      this.$buttons.find("[data-role=zoom-to]")
        .click(_.partial(function(view) {view._set_zoom($(this).attr("data-zoom"), true)}, this));
      var mode_vertical_btn = this.$buttons.find("[data-role=mode-vertical]");
      var mode_horizontal_btn = this.$buttons.find("[data-role=mode-horizontal]");
      mode_vertical_btn.click(_.bind(function() {
          mode_vertical_btn.addClass('active');
          mode_horizontal_btn.removeClass('active');
          this.set_axis('x');
      }, this));  
      mode_horizontal_btn.click(_.bind(function() {
          mode_vertical_btn.removeClass('active');
          mode_horizontal_btn.addClass('active');
          this.set_axis('y');
      }, this));  
        
      this._set_geometry( new PersonalTimetabling.CalendarViews.ColumnsView.DayColumnGeometry(this,null) );

      this._set_zoom(600, false, true);
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
    
    _set_zoom: function(zoom, redraw, dont_update_slider) {
      if (zoom >= 600) {
        if (!(this.geometry instanceof PersonalTimetabling.CalendarViews.ColumnsView.DayColumnGeometry)) {
          this.set_geometry(new PersonalTimetabling.CalendarViews.ColumnsView.DayColumnGeometry(this, null));
        }
        this.column_step_minutes = 15;
      }
      else if (zoom >= 300) {
        if (!(this.geometry instanceof PersonalTimetabling.CalendarViews.ColumnsView.WeekColumnGeometry)) {
          this.set_geometry(new PersonalTimetabling.CalendarViews.ColumnsView.WeekColumnGeometry(this, null));
        }        
        this.column_step_minutes = 60;
      }
      
      this.zoom = zoom;
      
      if (redraw) {
        this.resize();
      }
      
      if (!dont_update_slider) {
        this.$zoom_slider.slider("value", zoom);
      }
    },
    
    resize: function() {
      var max_lines = this.geometry.get_global_geometry().column_max_lines;
      
      // set height of lines
      // minimum is to be fit to window height or 50px
      var max_height = 500;
      var min_height = Math.min(max_height, Math.max(25, this.container_window_lines_size() / max_lines));
      
      
      // compute height as linear function of zoom between min and max height
      this.drawing_column_line_height = min_height + ((max_height - min_height) * ((this.zoom % 300) / 300));              

      PersonalTimetabling.CalendarViews.ColumnsView.prototype.resize.apply(this);      
    }
    
});