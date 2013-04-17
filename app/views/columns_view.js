// (c) 2013 Lukas Dolezal


// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.VerticalDayView = PersonalTimetabling.TasksViewBase.extend({

    options: {
        initial_date: Date.today()
    },

    layout_template:
      "<div  class='dayincolview'>" +
        "<div id='headers-window'>" +
          "<div id='mover-headers'>" +
            "<div id='grid-headers'>" +
              "<div class='header-supercols-container'></div>" +
              "<div class='header-cols-container'></div>" +
            "</div>" +
          "</div>" +
        "</div>" +
        "<div id='mover-window'>"+
          "<div id='mover'>" +
            "<div id='grid'></div>" +
          "</div>" +
        "</div>" +
      "</div>",
    
    activity_template: _.template(
      "<div data-type='activity-occurance' class='activity-occurance'>"+
        "<div class='name'>" +
          "<%= name %>" +
        "</div>" +
        "<div class='time'>" +
          "<span class='start'><%= start %></span>" +
          "<span class='end'><%= end %></span>" +
        "</div>" +
      "</div>"),
      
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
            "<button class='btn btn-inverse' data-role='scroll-left'><i class='icon-white icon-chevron-left'></i></button>" +
            "<button class='btn btn-inverse' data-role='scroll-right'><i class='icon-white icon-chevron-right'></i></button>" +
          "</div>" +
        "</div>",
    
    initialize: function() {

        // create HTML
        this.$el.append(this.layout_template);
        this.$buttons = $(this.buttons_template);
        $("#content-panel-place").empty().append(this.$buttons);
        
        
        // store jQuery references
        this.$bar_container = this.$el.find("#mover-headers");
        this.$supercols_el = this.$el.find(".header-supercols-container");
        this.$headers_el =  this.$el.find(".header-cols-container");        
        this.$container_window = this.$el.find("#mover-window");
        this.$container = this.$el.find("#mover");
        this.$grid_el = this.$el.find("#grid");
        
        this.$zoom_slider = this.$buttons.find("[data-role=zoom-slider]")
          .slider({min: 0, max: 899, change:_.bind(this.set_zoom, this), slide:_.bind(this.set_zoom, this), value:600});

        this.$buttons.find("[data-role=scroll-left]")
          .click(_.bind(this.move_left, this));
        this.$buttons.find("[data-role=scroll-right]")
          .click(_.bind(this.move_right, this));
         this.$buttons.find("[data-role=zoom-to]")
          .click(_.partial(function(view) {view._set_zoom($(this).attr("data-zoom"), true)}, this));
        
        // create Bindings and other stuff
        //this.$container.kinetic_draggable({distance:10, drag:_.bind(this.on_drag, this, this.$bar_container), stop: _.bind(this.on_drag, this, this.$bar_container)});
        this.$bar_container.kinetic_draggable({
          distance: 10,
          drag: _.bind(this.on_drag, this, this.$container),
          stop: _.bind(this.on_drag, this, this.$container)
        });

        this._set_geometry( new PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnGeometry(this,null) );
        
        // setup view parametersdate
        this.drawing_column_width = 200;
        this.drawing_columns_overlap = 6;
        this._set_zoom(600, false, true);

        this.drawing_columns_list = [{column_id: this.geometry.get_line_of_date(this.options.initial_date).column_id}];
        
        //this.render();
        //this.display_date(this.options.initial_date);
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
      var current_center_column = (-this.$container.offset().left  + (this.drawing_view_width/2)) / this.drawing_column_width;
      var column_index = Math.ceil(current_center_column);
      
      var center =  this.geometry.get_date_of_line(
        this.drawing_columns_list[column_index].column_id,
        (current_center_column - column_index) * this.drawing_columns_list[column_index].lines_labels.length
      );
      
      console.log("date in center", center);
      return center;
    },
    
    move_left: function() {
      this.scroll(this.drawing_column_width);
    },

    move_right: function() {
      this.scroll(-this.drawing_column_width);
    },
    
    
    /** (private) section **/
    
    _set_geometry: function(geometry) {
      this.geometry = geometry;
      if (this.geometry instanceof PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnGeometry) {
      } else if (this.geometry instanceof PersonalTimetabling.CalendarViews.VerticalDayView.WeekColumnGeometry) {
      }
    },
    
    _set_zoom: function(zoom, redraw, dont_update_slider) {
      if (zoom >= 600) {
        if (!(this.geometry instanceof PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnGeometry)) {
          this.set_geometry(new PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnGeometry(this, null));
        }
      }
      else if (zoom >= 300) {
        if (!(this.geometry instanceof PersonalTimetabling.CalendarViews.VerticalDayView.WeekColumnGeometry)) {
          this.set_geometry(new PersonalTimetabling.CalendarViews.VerticalDayView.WeekColumnGeometry(this, null));
        }        
      }
      
      var max_lines = this.geometry.get_global_geometry().column_max_lines;
      
      // set height of lines
      // minimum is to be fit to window height or 50px
      var min_height = Math.min(200, Math.max(25, this.$container_window.height() / max_lines));
      var max_height = 200;
      
      // compute height as linear function of zoom between min and max height
      this.drawing_column_line_height = min_height + ((max_height - min_height) * ((zoom % 300) / 300));
      
      if (redraw) {
        this.resize();
      }
      
      if (!dont_update_slider) {
        this.$zoom_slider.slider("value", zoom);
      }
    },
    
    // redraws whole view
    render: function() {
        this.resize();
    },

    // refresh sizes of view
    resize: function() {        
        // set view parameters
        this.drawing_view_width = this.$el.width();
        this.drawing_column_width = this.drawing_view_width / Math.floor(this.drawing_view_width / 200);
        this.drawing_columns = Math.ceil(this.drawing_view_width / this.drawing_column_width) + this.drawing_columns_overlap;
        this.$container.width(this.drawing_columns*this.drawing_column_width);
        this.$bar_container.width(this.drawing_columns*this.drawing_column_width);

        this.render_columns();
        this.render_headers();
        this.update_columns();
    },

    // renders main grid columns
    render_columns: function() {
        console.log("render_columns");
      
        this.$grid_el.empty();
        this.$headers_el.empty();

        // keep first column id
        var first_id = this.drawing_columns_list[0].column_id;
        this.drawing_columns_list = [];

        var max_lines = this.geometry.get_global_geometry().column_max_lines;
        
        var line_proto = $('<div class="line-label" />').css("height", this.drawing_column_line_height + "px");
        
        // create number of visible columns plus 2 for left and right overflow
        for( var i = 0; i < this.drawing_columns; i++) {            
            var gridcol = $("<div class='grid-col' />");
            gridcol.width(this.drawing_column_width);
            gridcol.css("left", i*this.drawing_column_width);

            var label_el = $("<div class='grid-header-col'/>");
            label_el.width(this.drawing_column_width);
            label_el.css("left", i*this.drawing_column_width);
            
            // render lines
            var lines = []; var lines_visible = 0;
            for (var li=0; li< max_lines; li++) {
              var line = line_proto.clone();
              
              gridcol.append(line);
              lines.push(line.get(0));
            }
            
            this.drawing_columns_list.push({$column: gridcol, $label: label_el, line_label_els: lines});
            this.$grid_el.append(gridcol);
            this.$headers_el.append(label_el);
        }
        
        this.drawing_columns_list[0].column_id = first_id;
        this.$grid_el.height(max_lines * this.drawing_column_line_height);
        
        // move to overlaping column
        this.set_movers_offset(-this.drawing_column_width * this.drawing_columns_overlap / 2)
    },

    // render headers
    render_headers: function() {
        console.log("render_headers");
      
        this.$supercols_el.empty();
        this.drawing_columns_supercols = [];
        
        var headers_count = Math.floor(this.drawing_columns / this.geometry.get_global_geometry().supercolum_min_cols) + 1;
        for (var i = 0; i< headers_count; i++) {
            var label_el = $("<span class='header-col-label'></span>");
            var header_el = $("<div class='header-col'></div>")
              .append(label_el);

            this.$supercols_el.append(header_el);
            this.drawing_columns_supercols.push({$el: header_el, label_el: label_el.get(0)});
        }
        
    },

    // updates columns content
    update_columns: function(columns_specs, first_even) {
        if (typeof columns_specs === 'undefined') {
          columns_specs = this.geometry.get_columns(this.drawing_columns, this.drawing_columns_list[0].column_id, 0);
        }
        if (typeof first_even === 'undefined') {
          first_even = true;
        }
      
        // update grid columns
        this.update_grid_columns(columns_specs,first_even);
        
        // update supercolumns
        this.update_header_columns();
        
        this.update_data_view();
    },
    
    update_grid_columns: function(columns_specs, first_even) {
        var width = this.drawing_column_width;
        var max_height = 0;
        
        for(var i = 0; i< this.drawing_columns_list.length; i++) {
          
            this.drawing_columns_list[i].column_id = columns_specs[i].id;
            this.drawing_columns_list[i].$column.toggleClass("even-col", first_even);
            
            var visible_lines = columns_specs[i].lines_labels.length;
            var col_height =  visible_lines * this.drawing_column_line_height;
            if (col_height > max_height)
              max_height = col_height;
            this.drawing_columns_list[i].$column.css({"height":col_height+"px"});
            this.drawing_columns_list[i].$label.get(0).innerHTML = (columns_specs[i].title);
            
            //update line labels
            this.update_column_lines(this.drawing_columns_list[i], columns_specs[i].lines_labels);
            
            first_even = !first_even;
        }

    },
    
    update_column_lines: function(column, line_labels) {
      for(var i in line_labels) {
        $(column.line_label_els[i]).removeClass("delimiter-right").removeClass("delimiter-left").removeClass("delimiter-top");
        column.line_label_els[i].innerHTML = line_labels[i];
      }
      
      column.lines_labels = line_labels;
    },

    // updates supercolumns rendering
    update_header_columns: function() {
      // get supercolumns breaks (ie first super header always starts at first column)
      var supercolumns = this.geometry.get_super_columns(
        this.drawing_columns_list[0].column_id,
        this.drawing_columns);
      
      var i_column = 0;
      var previous_column_i = 0;
      var previous_left_pixels = 0;
      // Start with second supercolumn (first is before or at most at the same time as first column)
      for(var i = 1; i<supercolumns.length && i < this.drawing_columns_supercols.length; i++) {
        //get column of this supercol start
        while(i_column < this.drawing_columns_list.length &&
          this.drawing_columns_list[i_column].column_id != supercolumns[i].supercol_start_column) {
          i_column++;
        }
        if (i_column >= this.drawing_columns_list.length)
          break;
        
        var left_pixels = (
          (i_column - previous_column_i) 
          + supercolumns[i].start_part
        ) * this.drawing_column_width;
        
        var width_pixels = left_pixels - previous_left_pixels;
        this.drawing_columns_supercols[i-1].$el.css("width", (width_pixels) + "px");
        previous_left_pixels = left_pixels;
        
        this.drawing_columns_supercols[i].$el.css("left", (left_pixels) + "px");
        this.drawing_columns_supercols[i-1].label_el.innerHTML = supercolumns[i-1].label;
        
        // process lines for marking them delimiters
        this._setup_delimiter_column(i_column, supercolumns[i].start_part);
      }
      
      // set width last supercol (wchich breaks at the right edge of view)
      var width_pixels = this.drawing_column_width*this.drawing_columns - previous_left_pixels;
      this.drawing_columns_supercols[i-1].$el.css("width", (width_pixels) + "px");
      this.drawing_columns_supercols[i-1].label_el.innerHTML = supercolumns[i-1].label;
      
      // set unused columns to width:0
      for(;i<this.drawing_columns_supercols.length;i++){
        this.drawing_columns_supercols[i].$el.css("width",0);
        this.drawing_columns_supercols[i].$el.css("left",0);
      }
    },

    _setup_delimiter_column: function(column, delimiting_part) {
      var column_lines = this.drawing_columns_list[column].line_label_els;
      var prev_right = false;
      for(var cli = 0; cli < column_lines.length; cli++) {
        var line_column_part = cli / column_lines.length;
        if (line_column_part < delimiting_part) {
          $(column_lines[cli]).addClass("delimiter-right");
          prev_right = true;
        } else {
          if (prev_right) {
            $(column_lines[cli]).addClass("delimiter-top");
            prev_right = false;
          }
          $(column_lines[cli]).addClass("delimiter-left");
        }
      }      
    },
    
    // renders activities
    update_data_view: function() {

      this.$grid_el.find("[data-type='activity-occurance']").remove();
      
      var first_date = this.drawing_columns_list.first().column_id;
      var last_date =  this.drawing_columns_list.last().column_id;

      var activities_to_show = this.collection.withOccurancesInRange(
        this.geometry.get_date_of_line(first_date,0), 
        this.geometry.get_date_of_line(last_date, this.drawing_columns_list.last().lines_labels.length)
      );

      for(var i = 0; i < activities_to_show.length; i++) {
        var activity = activities_to_show[i];
        var activity_occurances = activity.getOccurancesInRange(first_date, last_date);

        for(var io = 0; io < activity_occurances.length; io++) {
          var occurance = activity_occurances[io];
          // determine column
          var start_coord = this.geometry.get_line_of_date(occurance.get("start"));
          var end_coord = this.geometry.get_line_of_date(occurance.get("end"));

          // find column for start
          var column = this.drawing_columns_list.find(function(k) {return k.column_id == start_coord.column_id;}); 
          
          column.$column.append(
            $(this.activity_template({
              name: activity.get("name"), 
              start:  occurance.get("start"), 
              end:  occurance.get("end")
            }))
              .css("top", (start_coord.line*this.drawing_column_line_height) + "px")
              // TODO support over column activities
              .css("height", (end_coord.line - start_coord.line) * this.drawing_column_line_height + "px")
          );

        }
      }
    },
    
    shift_cols: function(number) {
      var columns_spec = this.geometry.get_columns(this.drawing_columns, this.drawing_columns_list[0].column_id, number);
      var first_even = ('$column' in this.drawing_columns_list[0] ? this.drawing_columns_list[0].$column.is(".even-col") : false);
      // if shift is even, first columns keeps even
      first_even = (number % 2 == 0) ? first_even : !first_even;
      this.update_columns(columns_spec, first_even);
    },
    
    // rederaws columns in order to make currently display columns on view centered in all columns
    center_columns: function() {      
      //compute how much columns prepend/append
      var center_column = this.drawing_columns / 2;
      var current_center_column = (-this.$container.offset().left  + (this.drawing_view_width/2)) / this.drawing_column_width;
      var columns_distance = current_center_column - center_column;
      var columns_to_shift = 0;
      
      console.log("current center ", current_center_column, " distance ", columns_distance); 
      
      if (current_center_column < 0) {
        // append columns to the right - needs to be shifted to the left
        columns_to_shift = -Math.ceil(-columns_distance);
      } else {
        columns_to_shift = Math.ceil(columns_distance);
      }
      
      this.shift_cols(columns_to_shift);
            
      this.set_movers_offset(
        -( (current_center_column-columns_to_shift) * this.drawing_column_width - (this.drawing_view_width/2))
      );
      
      
      
      //this.$container.show();
      //console.log("redraw end");
    },

    scroll: function(pixels) {

        //console.log("scroll ", pixels);
        var container_offset = this.$container.offset();
        this.$container.offset({left: container_offset.left + pixels});
        this.$bar_container.offset({left: container_offset.left + pixels});

        this.on_move(this.$container.offset());
    },
    
    scrollTo: function(offset){
        this.$container.css("left", offset + "px");
        this.$bar_container.css("left", offset + "px");

        this.on_move(this.$container.offset());
    },

    set_movers_offset: function(left){
        this.$container.css("left", left);
        this.$bar_container.css("left", left);
        this.update_supercol_label_position(left);        
    },

    // redraw columns in order to currently leftmost visible column will be prepended with edge columns (ie align columns roll to center of view)
    on_move: function(current_offset) {

        var grid_left = current_offset.left;
      
        // check if drawed columns reached screen edge from inside
        if (grid_left > 0) {
          // make virtual column which would be on the left edge non-virtual - ie prepend it
          this.center_columns();
          
          return true;
        } else if (grid_left <  -this.drawing_column_width * this.drawing_columns_overlap) {
          // make virtual column which would be on the right edge non-virtual - ie append it
          this.center_columns();
          
          return true;
        }
        this.update_supercol_label_position(grid_left);        
        
        return false;
        
    },
    
    update_supercol_label_position: function(left) {
      // affix superheader col
      /*
       *  |                  |           " left of screen (100 form lom)
       *  |                  |           "
       *  |                  ^ left of col rel to l-of-mover (50px)
       *  ^ left of mover = (-100px)
       * 
       */
        if (this.drawing_columns_supercols) {
        for(var i = 0; i < this.drawing_columns_supercols.length;i++) {
          var header_dom = this.drawing_columns_supercols[i].$el.get(0);
          this.drawing_columns_supercols[i].label_el.style.left =
          Math.max(0, Math.min(header_dom.offsetWidth - this.drawing_columns_supercols[i].label_el.offsetWidth, -header_dom.offsetLeft - left)) + "px";
        }
        }
    },

    // handle drag event
    on_drag: function(other, e, ui) {
      // drag also other elements
      other.css({"left":ui.position.left+"px"});
      
      if (this.on_move(ui.position)) {
        // reset mouse position in drag
        if (typeof ui.helper !== "undefined" && typeof ui.helper.kinetic_draggable === "function") {
          ui.helper.kinetic_draggable("resetMouse", e);
        }
      }
    }
});

