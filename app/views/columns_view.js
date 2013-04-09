// (c) 2013 Lukas Dolezal


// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.VerticalDayView = PersonalTimetabling.TasksViewBase.extend({

    options: {
        initial_date: Date.today()
    },

    initialize: function() {

        this.$el.addClass("dayincolview");

        // create HTML
        this.$el.append(new Jaml.Template(function() {
          div({id:'headers-window'},
            div({id:'mover-headers'},
              div({id:'grid-headers'},
                div({cls:'header-supercols-container'}),
                div({cls:'header-cols-container'})
              )
            )
          );
        }).render());
        
        this.$el.append(new Jaml.Template(function() {
          div({id:'mover-window'},
            div({id:'mover'},
              div({id:'grid'})
            )
          );
        }).render());
        
        // store jQuery references
        this.$bar_container = this.$el.find("#mover-headers");
        this.$supercols_el = this.$el.find(".header-supercols-container");
        this.$headers_el =  this.$el.find(".header-cols-container");        
        this.$container_window = this.$el.find("#mover-window");
        this.$container = this.$el.find("#mover");
        this.$grid_el = this.$el.find("#grid");
        
        // create Bindings and other stuff
        //this.$container.kinetic_draggable({distance:10, drag:_.bind(this.on_drag, this, this.$bar_container), stop: _.bind(this.on_drag, this, this.$bar_container)});
        this.$bar_container.kinetic_draggable({
          distance: 10,
          drag: _.bind(this.on_drag, this, this.$container),
          stop: _.bind(this.on_drag, this, this.$container)
        });

        this.geometry = new PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnGeometry(this,null);
        
        // setup view parametersdate
        this.drawing_column_width = 200;
        this.drawing_columns_overlap = 6;
        this.drawing_column_line_height = 50;

        this.drawing_columns_list = [{column_id: this.geometry.get_line_of_date(this.options.initial_date).column_id}];
        
        //this.render();
        //this.display_date(this.options.initial_date);
    },

    // set zoom
    set_zoom: function(z) {
      if (z > 500) {
        if (!(this.geometry instanceof PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnGeometry)) {
          this.set_geometry(new PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnGeometry(this, null));
        }
      }
      else {
        if (!(this.geometry instanceof PersonalTimetabling.CalendarViews.VerticalDayView.WeekColumnGeometry)) {
          this.set_geometry(new PersonalTimetabling.CalendarViews.VerticalDayView.WeekColumnGeometry(this, null));
        }        
      }
    },

    set_geometry: function(geometry) {
      var date = this.date_in_center();
      this.geometry = geometry;
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
    
    
    /** (private) section **/
    
    // redraws whole view
    render: function() {
        this.resize();        
        this.render_columns();
        this.render_headers();
        this.update_columns();
    },

    // refresh sizes of view
    resize: function() {
        var headers_height = this.$bar_container.find("#grid-headers").innerHeight();
        this.$bar_container.height(headers_height);
        this.$container_window.height(this.$el.height()-headers_height);
        
        // set view parameters
        this.drawing_view_width = this.$el.width();
        this.drawing_columns = Math.ceil(this.drawing_view_width / this.drawing_column_width) + this.drawing_columns_overlap;
        this.$container.width(this.drawing_columns*this.drawing_column_width);
        this.$bar_container.width(this.drawing_columns*this.drawing_column_width);
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
        
        var line_proto = $('<div class="line-label" />');
        
        // create number of visible columns plus 2 for left and right overflow
        for( var i = 0; i < this.drawing_columns; i++) {            
            var gridcol = $("<div class='grid-col' />");
            gridcol.width(this.drawing_column_width);
            gridcol.css("left", i*this.drawing_column_width);

            var label_el = $("<div class='grid-header-col'/>");
            label_el.width(this.drawing_column_width);
            label_el.css("left", i*this.drawing_column_width);
            
            // render lines
            var lines = [];
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
            var col_height = columns_specs[i].lines_labels.length * this.drawing_column_line_height;
            if (col_height > max_height)
              max_height = col_height;
            this.drawing_columns_list[i].$column.css({"height":col_height+"px"});
            this.drawing_columns_list[i].$label.get(0).innerHTML = (columns_specs[i].title);
            
            //update line labels
            this.update_column_lines(this.drawing_columns_list[i], columns_specs[i].lines_labels);
            
            first_even = !first_even;
        }
        
        this.$grid_el.height(max_height);

    },
    
    update_column_lines: function(column, line_labels) {
      for(var i in line_labels) {
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
      for(var i = 1; i<supercolumns.length; i++) {
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
        this.drawing_columns_supercols[i].label_el.innerHTML = supercolumns[i].label;
      }
      
      // set width last supercol (wchich breaks at the right edge of view)
      var width_pixels = this.drawing_column_width*this.drawing_columns - previous_left_pixels;
      this.drawing_columns_supercols[i-1].$el.css("width", (width_pixels) + "px");        
      
      // set unused columns to width:0
      for(;i<this.drawing_columns_supercols.length;i++){
        this.drawing_columns_supercols[i].$el.css("width",0);
        this.drawing_columns_supercols[i].$el.css("left",0);
      }
    },

    activity_template: _.template("<div data-type='activity-occurance' class='activity-occurance'><div class='name'><%= name %></div><div class='time'><span class='start'><%= start %></span><span class='end'><%= end %></span></div></div>"),
    
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
        //console.log("movers ", left);
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

        // update fixed supercol label
        this.update_supercol_label_position();
        
        return false;
        
    },
    
    update_supercol_label_position: function() {
      if (this.$grid_supercol_affixed !== undefined) {
        this.$grid_supercols[this.$grid_supercol_affixed].label.css("left", -current_offset.left - this.$grid_supercols[this.$grid_supercol_affixed].left);
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

