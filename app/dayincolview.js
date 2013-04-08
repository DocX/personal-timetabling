// (c) 2013 Lukas Dolezal

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.VerticalDayView = PersonalTimetabling.AbstractTaskView.extend({

    options: {
        initial_date: Date.today()
    },

    initialize: function() {

        this.$el.addClass("dayincolview");
        
        this.$bar_container_window = $("<div id='headers-window'/>").appendTo(this.$el);
        this.$bar_container = $("<div id='mover-headers' />").appendTo(this.$bar_container_window);
        this.$headers_el = $("<div id='grid-headers' />").appendTo(this.$bar_container);
        this.$bar_container_window.append($("<div id='headers-corkiee'><div class='something'/></div>"));
        
        this.$container_window = $("<div id='mover-window'/>").appendTo(this.$el);
        this.$container = $("<div id='mover' />").appendTo(this.$container_window);
        this.$grid_el = $("<div id='grid' />").appendTo(this.$container);
        
        //this.$container.kinetic_draggable({distance:10, drag:_.bind(this.on_drag, this, this.$bar_container), stop: _.bind(this.on_drag, this, this.$bar_container)});
        this.$bar_container.kinetic_draggable({distance:10, drag:_.bind(this.on_drag, this, this.$container), stop: _.bind(this.on_drag, this, this.$container)});

        // clone zoom and geometry from class instance
        this.zoom = new PersonalTimetabling.CalendarViews.VerticalDayView.ColumnViewParemeters();
        this.geometry = new PersonalTimetabling.CalendarViews.VerticalDayView.ColumnsGeometry();

        this.geometry.compute(this.zoom.zoom(999, this));

        this.grid_cols_first_date = this.geometry.align(new Date());

        this.render();
        
        this.displayDate(this.options.initial_date);
    },

    // current columns geometry configuration - abstracted from physical rendered units
    geometry: null,

    // current rendering zoom - translates abstracted columns to physical rendering units
    zoom: null,

    // array of grid column elements
    $grid_cols: [],

    // array of supercolumn elements
    $grid_supercols: [],

    // redraws whole view
    render: function(visible_date) {
        this.render_hours_labels();
        this.resize();        
        this.render_columns();
        this.render_headers();
        this.redraw(visible_date);
    },

    // refresh sizes of view
    resize: function() {
        var headers_height = this.$headers_el.innerHeight();
        this.$bar_container.height(headers_height);
        this.$container_window.height(this.$el.height()-headers_height);
    },

    // renders main grid columns
    render_columns: function() {
        console.log("render_columns");
      
        this.$grid_el.empty();

        this.$grid_cols = [];

        // create number of visible columns plus 2 for left and right overflow
        for( var i = 0; i < this.zoom.rendering_columns_count; i++) {            
            var gridcol = $("<div class='grid-col' />");
            
            this.$grid_cols.push({col: gridcol});
            this.$grid_el.append(gridcol);
        }
    },

    // render headers
    render_headers: function() {
        console.log("render_headers");
      
        this.$headers_el.empty();
        this.$grid_supercols = [];
        this.$grid_colsheaders = [];

        // compute maximum on screen visible headers
        // ie max supercol units in visible days pixels
        var headers_count = (this.zoom.rendering_columns_count / this.geometry.min_supercolumn_columns()) + 1;

        // create supercolumns elements
        var supercols_header_container = $("<div class='header-supercols-container'/>").appendTo(this.$headers_el);
        
        for (var i = 0; i< headers_count; i++) {
            var label_el = $("<span class='header-col-label'></span>");
            var header_el = $("<div class='header-col'></div>").append(label_el);

            supercols_header_container.append(header_el);
            this.$grid_supercols.push({col: header_el, label: label_el});
        }

        // create number of visible columns
        var cols_header_container = $("<div class='header-cols-container'/>").appendTo(this.$headers_el);
        for( var i = 0; i < this.zoom.rendering_columns_count; i++) {
            var label_el = $("<div class='grid-header-col'/>");
            
            this.$grid_colsheaders.push({$el: label_el});
            cols_header_container.append(label_el);
        }
    },

    weekdays: ["Mon", "Tue", "Wed", "Thu", "Fri", "Son", "Sun"],
    
    render_hours_labels: function() {
	//this.$hours_labels.empty();

        /*var lineformat = this.geometry.column_spec.lineformat;
        var sepformat = this.geometry.column_spec.sepformat;
        var sepparam = this.geometry.column_spec.sepparam;

        var previous_sep = '';
        
        // add 24 hours boxes
        for(var i = 0; i< this.geometry.column_spec.lines;i++) {
          var hour = (i * this.geometry.column_spec.linehours) % 24;
          hour = hour.pad(2) + ":00";
          
          var dayn = Math.floor((i * this.geometry.column_spec.linehours) / 24);
          var day = dayn;

          var weekday = this.weekdays[(dayn % 7)];
          var week = "week +" + Math.floor(dayn / 7);

          var curr_sep = sepparam.replace("%w", week).replace("%v", weekday).replace("%h", hour).replace("%d", day);
          var format = (previous_sep != curr_sep) ? sepformat : lineformat;
          previous_sep = curr_sep;
          
            $("<div class='hour-label' />")
            .html(format.replace("%w", week).replace("%v", weekday).replace("%h", hour).replace("%d", day))
            .appendTo(this.$hours_labels);
        }
	*/
	var newHeight = 50*this.geometry.column_spec.lines;
	if (this.$grid_el.height() != newHeight){
	  this.$grid_el.height(newHeight);
	}
        //this.$hours_labels.height(50*this.geometry.column_spec.lines);
	
    },
    
    render_incol_hours_labels: function(coldate, col) {
	col.find("[data-role=line-label]").remove();

        var lineformat = this.geometry.column_spec.lineformat;
        var sepformat = this.geometry.column_spec.sepformat;
        var sepparam = this.geometry.column_spec.sepparam;

        var previous_sep = '';
        
        // add 24 hours boxes
        for(var i = 0; i< this.geometry.column_spec.lines;i++) {
          
            $("<div class='line-label' data-role='line-label' />")
            .html(this.geometry.column_line_label(coldate, i))
            .appendTo(col);
        }
    },

    // UPDATE COLUMNS WIDTH AND ALIGNMENT
    update_columns_rendering: function() {
        // ensure alignment of first column

        var left_date = this.grid_cols_first_date;
        var left_pixels = 0;

        var even = true;
        // find columns difference from current left and new left
        if ('date' in this.$grid_cols[0]) {
          var columns = this.geometry.columns_of_range(left_date, this.$grid_cols[0].date);
          even = this.$grid_cols[0].col.is(".even-col");
          even = (columns % 2 == 0) ? even : !even;
        }

        //this.$grid_el.hide();
        
        for(var i = 0; i< this.$grid_cols.length; i++) {
            // column width is variable (ie column of month)
            var width = this.zoom.column_width;

            this.$grid_cols[i].width = width;
            this.$grid_cols[i].left = left_pixels;
            this.$grid_cols[i].date = left_date;
            this.$grid_cols[i].col.width(width);
            this.$grid_cols[i].col.css("left", left_pixels);
            this.$grid_cols[i].col.toggleClass("even-col", even);
            this.$grid_cols[i].col.height(this.geometry.column_spec.lines * 50);
            
	    this.render_incol_hours_labels(left_date, this.$grid_cols[i].col);
	    
            left_pixels += width;
            left_date = this.geometry.get_column_next(left_date);
            even = !even;
        }

        //this.$grid_el.show();
        
        this.zoom.rendering_width = left_pixels;
        this.grid_cols_last_date = left_date;
        this.$container.width(this.zoom.rendering_width);
        this.$bar_container.width(this.zoom.rendering_width);
        //console.log("update columns end ");
    },


    update_headers: function() {
        // compute start of first supercolumn.
        var left_date = this.geometry.align(this.grid_cols_first_date, this.geometry.supercolumn_groupby);

        // compute pixel offset of first supercolumn
        var left_pixels = -this.geometry.columns_of_range(left_date, this.grid_cols_first_date) * this.zoom.column_width;

        // supercolumns
        for(var i = 0; i < this.$grid_supercols.length; i++) {
            // get date of end of current supercolumn determined by left date
            var next_stop = this.geometry.next(left_date, this.geometry.supercolumn_groupby);

            // width of supercolumn = number of columns overlapped by supercolumn * column width
            var width = this.geometry.columns_of_range(left_date, next_stop) * this.zoom.column_width;
            this.$grid_supercols[i].col
                .css("left", left_pixels);
            this.$grid_supercols[i].label
                .text(this.header_label(left_date));

            if (Math.abs(this.$grid_supercols[i].col.width() - width) > 1) {
                this.$grid_supercols[i].col.width(width);
            }

            this.$grid_supercols[i].width = width;
            this.$grid_supercols[i].left = left_pixels;
            this.$grid_supercols[i].label_width =  this.$grid_supercols[i].label.innerWidth();

            left_date = next_stop;
            left_pixels += width;
        }

        // update cols headers
        for(var i = 0; i < this.$grid_colsheaders.length; i++) {
            this.$grid_colsheaders[i].$el
                .width(this.$grid_cols[i].width)
                .css("left", this.$grid_cols[i].left)
                .text(this.column_label(this.$grid_cols[i].date));
        }
        
        this.update_header_labels();
    },

    update_header_labels: function(container_offset) {
        if (container_offset == undefined) {
            container_offset = this.$container.offset();
        }
        
        for(var i = 0; i < this.$grid_supercols.length; i++) {
            // if left part of column is not visible, make label fixed
            if (this.$grid_supercols[i].left + container_offset.left < 60) {
                this.$grid_supercols[i].col.addClass("header-col-invisibleleft");
                this.$grid_supercol_affixed = i;
            }
            else {
                this.$grid_supercols[i].col.removeClass("header-col-invisibleleft");
            }

            if (this.$grid_supercols[i].left + container_offset.left < -this.$grid_supercols[i].width+this.$grid_supercols[i].label_width +60) {
                this.$grid_supercols[i].col.addClass("header-col-invisible");
            }
            else {
                this.$grid_supercols[i].col.removeClass("header-col-invisible");
            }
        }
        
    },


    // get label on column according current zoom
    column_label: function(col_date) {
        if(this.geometry.column_unit == 'HOUR') {
                return col_date.getHours().pad(2) + ":" + col_date.getMinutes().pad(2);
        } else if (this.geometry.column_unit == 'DAY') {
                return col_date.getDate() + ". " + (col_date.getMonth()+1) + ". " + col_date.getFullYear();
        } else if (this.geometry.column_unit == 'WEEK') {
                return "Week " + col_date.getWeekOfYear().text;
        } else {
            // month
            return (col_date.getMonth() + 1) + "/" + col_date.getFullYear();
        }                           

    },

    header_label: function(header_date) {
        switch(this.geometry.supercolumn_groupby) {
            case 'HOUR':
                return header_date.getHours().pad(2) + ":" + header_date.getMinutes().pad(2);
            case 'DAY':
                return header_date.getDate() + ". " + (header_date.getMonth()+1) + ". " + header_date.getFullYear();
            case 'WEEK':
                var w = header_date.getWeekOfYear();
                return "Week " + w.week  + "/" + w.year;
            case 'MONTH':
                return header_date.getMonthName() + " " + header_date.getFullYear();
            case 'YEAR':
                return header_date.getFullYear();
            default:
                return "";
        }
    },

    activity_template: _.template("<div data-type='activity-occurance' class='activity-occurance'><div class='name'><%= name %></div><div class='time'><span class='start'><%= start %></span><span class='end'><%= end %></span></div></div>"),
    
    // renders activities
    update_data_view: function() {

      this.$grid_el.find("[data-type='activity-occurance']").remove();
      
      var first_date = this.grid_cols_first_date;
      var last_date = this.grid_cols_last_date;

      var activities_to_show = this.collection.withOccurancesInRange(first_date, last_date);

      for(var i = 0; i < activities_to_show.length; i++) {
        var activity = activities_to_show[i];
        var activity_occurances = activity.getOccurancesInRange(first_date, last_date);

        for(var io = 0; io < activity_occurances.length; io++) {
          var occurance = activity_occurances[io];
          // determine column
          var start_col = this.geometry.get_containing_column_index(first_date, occurance.get("start"));
          var start_line = this.geometry.get_column_line(this.$grid_cols[start_col].date, occurance.get("start"));
          var end_line = this.geometry.get_column_line(this.$grid_cols[start_col].date, occurance.get("end"));

          this.$grid_cols[start_col].col.append(
            $(this.activity_template({name: activity.get("name"), start:  occurance.get("start"), end:  occurance.get("end")}))
              .css("top", (start_line*50) + "px")
              .height((end_line - start_line) * 50)
          );

        }
      }

    },
    

    // rederaws columns in order to make given column index prepended by edge columns
    redraw: function(column_to_center_around) {
        //console.log("redraw start");
        //this.$container.hide();
      
        var column_index = 0;
        if (column_to_center_around !== false) {
          if (typeof column_to_center_around == 'undefined') {
            column_to_center_around = -this.$container.offset().left / this.zoom.column_width;
          }

          // set new first col date
          column_index = Math.floor(column_to_center_around);

          if (column_index > 0) {
            if (column_index < this.$grid_cols.length && 'date' in this.$grid_cols[column_index]) {
              this.grid_cols_first_date = this.$grid_cols[column_index].date;
            } else {
              // compute
              var i = column_index;
              while(i-- > 0)
                this.grid_cols_first_date = this.geometry.get_column_next(this.grid_cols_first_date);
            }
          } else {
            var i = column_index * this.geometry.column_units;
            while(i++ < 0)
              this.grid_cols_first_date = this.geometry.align(this.grid_cols_first_date.addMinutes(-1));
          }
        }
        
        // prepend by edge cols
        var i = this.zoom.edge_columns* this.geometry.column_units;
        while(i-- >0)
          this.grid_cols_first_date = this.geometry.align(this.grid_cols_first_date.addMinutes(-1));

        // update columns labels
        this.update_columns_rendering();
        this.update_headers();
        this.update_data_view();
        
        // set view offset to given column to center around partition
        if (column_to_center_around === false) {
          column_to_center_around = 0;
        }
        this.set_movers_offset(
          -(this.zoom.edge_columns + column_to_center_around - column_index) * this.zoom.column_width
        );

        //this.$container.show();
        //console.log("redraw end");
    },

    scroll: function(pixels) {

        //console.log("scroll ", pixels);
        var container_offset = this.$container.offset();
        this.$container.offset({left: container_offset.left + pixels});
        this.$bar_container.offset({left: container_offset.left + pixels});

        this.scroll_masquerade(this.$container.offset());
    },

    set_movers_offset: function(left){
        this.$container.css("left", left);
        this.$bar_container.css("left", left);
        //console.log("movers ", left);
    },

    // redraw columns in order to currently leftmost visible column will be prepended with edge columns (ie align columns roll to center of view)
    scroll_masquerade: function(current_offset) {

        var grid_left = current_offset.left;
        
        if (grid_left > 0) {
            this.redraw(-grid_left / this.zoom.column_width);
            return true;
        } else if (grid_left <  -this.zoom.column_width * 2* this.zoom.edge_columns) {
            this.redraw(-grid_left / this.zoom.column_width);
            return true;
        } else {
            // update headers text alginments
            this.update_header_labels(current_offset);
        }

        if (this.$grid_supercol_affixed !== undefined) {
            this.$grid_supercols[this.$grid_supercol_affixed].label.css("left", -current_offset.left - this.$grid_supercols[this.$grid_supercol_affixed].left);
        }
        
        return false;
        
    },

    on_drag: function(other, e, ui) {
        // drag also other elements
        other.offset({left:ui.position.left});

        
        if (this.scroll_masquerade(ui.position)) {
            // reset mouse position in drag
            if (typeof ui.helper !== "undefined" && typeof ui.helper.kinetic_draggable === "function") {
              ui.helper.kinetic_draggable("resetMouse", e);
            }
        }

        this.zoom_date = null;
    },

    zoom_date: null,
    
    // z is 0 to 1000
    setZoom: function(z) {
      if (this.zoom_date === null) {
        this.zoom_date = this.getAbstractMiddleDate();
      }
      
      var level = this.zoom.zoom(z, this);
      this.geometry.compute(level);

      this.render();
      this.displayDate(this.zoom_date);
    },

    // move view center to given date
    displayDate: function(date) {
      this.setAbstractMiddleDate(date);
    },

    getAbstractMiddleDate: function() {
      var middle_column = (-this.$container.offset().left + (this.$el.width() / 2)) / this.zoom.column_width;
      
      var column = Math.floor(middle_column);
      
      return this.$grid_cols[column]
        .date
        .addMinutes((middle_column-column)*PersonalTimetabling.CalendarViews.VerticalDayView.ColumnsGeometry.max_unit_days[this.geometry.column_unit]*this.geometry.column_units*1440);
    },

    setAbstractMiddleDate: function(date) {
      // determine offset in columns from current middle column
      var current_middle = this.getAbstractMiddleDate();

      var column_diff = this.geometry.columns_of_range(current_middle, date);      
      
      this.scroll(-column_diff * this.zoom.column_width);
    }
});


// Abstract representation of view column logic.
PersonalTimetabling.CalendarViews.VerticalDayView.ColumnsGeometry = Backbone.Model.extend({

  constructor: function() {
    this.compute(0);

    Backbone.Model.apply(this, arguments);
  },

  // determine geometry by given minutes/pixel zoom
  compute: function(level) {
    var level_spec = this.constructor.LEVELS[level];
    
    this.column_units = level_spec.count;
    this.column_unit = level_spec.unit;
    this.supercolumn_groupby = level_spec.supercolumn;
    this.column_spec = level_spec;
  },
  
  // align given date to the nearest earlier eadge of larger unit
  align: function(date, unit) {
    if (unit == undefined)
      unit = this.column_unit;
    
    switch(unit) {
      case 'DAY':
        // go to noon of next day and then get midnight to resolve +- 1 hour fluctuation in day length
        // in DST
        return date.getMidnight();
        break;
      case 'WEEK':
        return date.getMidnight().addDays(- date.getDayStarting(1));
        break;
      case 'MONTH':
        next_stop = new Date(date);
        next_stop.setDate(1);
        next_stop.setHours(0,0,0);
        return next_stop;
        break;
      case 'YEAR':
        return new Date(date.getFullYear(), 0,0,0,0,0);
      default:
        return date;
        break;
    }
  },
  
  // get the nearest latter edge of larger time unit
  next: function(date, unit){
    if (unit === undefined)
      unit = this.column_unit;
    
    switch(unit) {
      case 'DAY':
        // go to noon of next day and then get midnight to resolve +- 1 hour fluctuation in day length
        // in DST
        return date.getMidnight().addDays(1);
        break;
      case 'WEEK':
        return date.getMidnight().addDays(- date.getDayStarting(1) + 7).getMidnight();
        break;
      case 'MONTH':
        next_stop = new Date(date);
        next_stop.setDate(1);
        next_stop.setMonth(next_stop.getMonth() + 1);
        next_stop.setHours(0,0,0);
        return next_stop;
        break;
      case 'YEAR':
        return new Date(date.getFullYear() + 1, 0,0,0,0,0);
      default:
        break;
    }
  },
  
  unit_minutes_limit: function(unit) {
    switch(unit) {
      case 'HOUR':
        return 60;
        break;
      case 'DAY':
        // go to noon of next day and then get midnight to resolve +- 1 hour fluctuation in day length
        // in DST
        // 25 hours a day
        return 1500;
        break;
      case 'WEEK':
        // better bound is only 1 day in week can have 25 hours
        // but it is for compatibility with cross unit computations (ie WEEK / DAY returns 7 not 6,76)
        return 7*1500;
        break;
      case 'MONTH':
        // similar to week
        return 31*1500;
        break;
      case 'YEAR':
        return 31*12*1500;
      default:
        break;
    }
    
  },
  
  get_column_next:function (date) {
    var column_date = date;
    var i = this.column_units;
    while(i-- > 0) {column_date = this.next(column_date, this.column_unit);}
    return column_date;
  },
  
  // returns number of column containing needle_date right of first_column_date
  get_containing_column_index: function(first_column_date, needle_date) {
    var next_column = this.get_column_next(first_column_date);
    var column_index = 0;
    while(next_column < needle_date) {
      column_index++;
      next_column = this.get_column_next(next_column);
    }
    return column_index;
  },
  
  // determine minimal supercolumn width in number of columns
  min_supercolumn_columns: function() {
    // smallest allowed column is day.

    var col_days = this.constructor.max_unit_days[this.column_unit] * this.column_units;
    var supercol_days = this.constructor.min_unit_days[this.supercolumn_groupby];

    return supercol_days / col_days;
  },

  // retrieve number of columns corresponding to range given by two dates
  columns_of_range: function(left, right) {
    var days = right.diff('DAY', left);

    var column_days = this.constructor.max_unit_days[this.column_unit] * this.column_units;

    return days / column_days;
  },

  // determine line (with precision to decimals) of given date in column starting in column_start_date
  get_column_line: function(column_start_date, date) {
    var hours_diff = date.diff_partial('HOUR', column_start_date);
    return hours_diff / this.column_spec.linehours;
  },
  
  column_line_label: function(column_start_date, lineno) {
    var line_normalized_day = this.column_spec.linehours * lineno / 24;
    var day_floor = Math.floor(line_normalized_day);
    
    var line_date = column_start_date.addDays(day_floor).addMinutes((line_normalized_day - day_floor) * 24 * 60);
    
    switch (this.column_unit) {
      case "WEEK":
	return line_normalized_day > day_floor ? "" : ( this.constructor.weekdays[line_date.getDayStarting(1)] + " " + line_date.getDate() + ". " + (line_date.getMonth()+1) + ".");
      case "DAY":
	return lineno.pad(2) + "h";
      case "MONTH":
	return column_start_date.getMonth() == line_date.getMonth() ? this.constructor.weekdays[line_date.getDayStarting(1)] + " " + line_date.getDate() + ". " + (line_date.getMonth()+1) + "." : "-";
    }
  }
}, {
  weekdays: ["Mon", "Tue", "Wed", "Thu", "Fri", "Son", "Sun"],
  
  max_unit_days: {
    'DAY': 1,
    'WEEK': 7,
    'MONTH': 31,
    'YEAR': 366,
  },
  
  min_unit_days: {
    'DAY': 1,
    'WEEK': 7,
    'MONTH': 28,
    'YEAR': 356
  },
  
  // levels of details from the farest
  LEVELS: [
  {count: 1, unit: 'MONTH', supercolumn: 'YEAR', linehours: 24, lines:31, lineformat: "%d.", sepformat: "%d.", sepparam: "%d"},
  {count: 1, unit: 'WEEK', supercolumn: 'MONTH', linehours: 24, lines:7, lineformat: "%h", sepformat: "%v<br>%h", sepparam: "%d"},
  {count: 1, unit: 'WEEK', supercolumn: 'MONTH', linehours: 12, lines:14, lineformat: "%h", sepformat: "%v<br>%h", sepparam: "%d"},
  {count: 1, unit: 'WEEK', supercolumn: 'MONTH', linehours: 8, lines:21, lineformat: "%h", sepformat: "%v<br>%h", sepparam: "%d"},
  {count: 1, unit: 'DAY', supercolumn: 'WEEK', linehours: 2, lines:12, lineformat: "%h",  sepformat: "%h", sepparam: ""},
  {count: 1, unit: 'DAY', supercolumn: 'WEEK', linehours: 1, lines:24, lineformat: "%h",  sepformat: "%h", sepparam: ""},
  ],  
});


PersonalTimetabling.CalendarViews.VerticalDayView.ColumnViewParemeters = Backbone.Model.extend({

  MIN_COL_WIDTH: 150,
  
  // current column width in pixels
  column_width: 150,

  // current count of columns are rendered (not the same as visible due to overflow hidding)
  rendering_columns_count: 0,

  // current rendering width.
  rendering_width: 0,

  // number of columns appended on each side for dragging seamless effect
  edge_columns: 4,
  
  recompute: function(timeline) {
    
    var visible_width = timeline.$el.width();
    this.rendering_columns_count = Math.ceil(visible_width / this.column_width) + 2*this.edge_columns;
    this.rendering_width = this.rendering_columns_count * this.column_width;
    
  },

  // zooms view parameters and returns zoom level for geometry
  zoom: function(zoom, timeline) {    
      var level = Math.floor(PT.CalendarViews.VerticalDayView.ColumnsGeometry.LEVELS.length * zoom/ 1000);
      console.log("level", level);
      
      this.column_width = /*zoom % this.MIN_COL_WIDTH + */this.MIN_COL_WIDTH;
      this.recompute(timeline);

      return level;
  }
});

// represents view geometry. translates view columns/lines to/from dates and handles column labels
PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnView = function(view, model) {
  
  var global_geometry =  {
    column_max_lines : 24,
    supercolum_max_cols: 7,
  };
  
  /* construcotr(view, model) { */
    this.view = view;
    this.model = model;
  /* } */
  
  // returns view mode's global geometry, immutable by the time
  // geometry = {column_max_lines: number, supercolum_max_cols: number}
  function get_global_geometry() {
    return this.constructor.global_geometry;
  }
  
  // returns number column specification to the right, resp. to the left of from_id column
  // column specifications = [{id: id, title: "", lines_labels: ["", "", ...]}, ...]
  function get_columns(number, from_id) {
    // id is column's top edge Date
    
  }
  
  // return supercolumns grouping columns between given bounds.
  // supercolumns are specified as array of borders coordinates in columns unit
  // supercolumns = [{end_column_id: id, column_part: part, label: ""}, ...]
  function get_super_columns(column_start_id, column_end_id) {
    
  }
  
  function get_activities(column_start_id, column_end_id) {
    
  }
  
  function get_date_of_line(column_id, line) {
    
  }
  
  // returns line coordinates = {column_id: id, line: number}
  function get_line_of_date(date) {
    
  }
};