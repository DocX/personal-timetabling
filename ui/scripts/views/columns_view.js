// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryKineticDraggable = require('lib/jquery.ui.kineticdraggable');

return Backbone.View.extend({
    
    layout_template:
      "<div class='columns-view horizontal-columns'>" +
        "<div class='headers-window window'>" +
          "<div class='headers-mover mover'>" +
            "<div class='headers mover-content'>" +
              "<div class='headers-supercols'></div>" +
              "<div class='headers-cols'></div>" +
            "</div>" +
          "</div>" +
        "</div>" +
        "<div class='grid-window window'>"+
          "<div class='grid-mover mover'>" +
            "<div class='grid mover-content'>" +
              "<div class='grid-columns'></div>" +
              "<div class='grid-overlay'></div>" +
            "</div>" +
          "</div>" +
        "</div>" +
      "</div>",
    
    
    
    initialize: function() {

        this.reset_html();

        // setup view parametersdate
        this.drawing_column_width = 200;
        this.drawing_columns_overlap = 10;
    },

    reset_html: function() {
        // create HTML
        this.$wrapper = $(this.layout_template);
        this.$el.empty();
        this.$el.append(this.$wrapper);
        
        // store jQuery references
        this.$bar_container = this.$el.find(".columns-view .headers-mover");
        this.$supercols_el = this.$el.find(".columns-view .headers-supercols");
        this.$headers_el =  this.$el.find(".columns-view .headers-cols");        
        this.$container_window = this.$el.find(".columns-view .grid-window");
        this.$container = this.$el.find(".columns-view .grid-mover");
        this.$grid_el = this.$el.find(".columns-view .grid-columns");
        this.$grid_overlay_el = this.$el.find(".columns-view .grid-overlay");
                
        this.$bar_container.kinetic_draggable({
          axis: this.axis,
          drag: _.bind(this.on_drag, this, this.$container),
          stop: _.bind(this.on_drag, this, this.$container)
        });      
    },
    
    move_left: function() {
      this.scroll(this.drawing_column_width);
    },

    move_right: function() {
      this.scroll(-this.drawing_column_width);
    },
    
    
    /** (private) section **/
    
    axis: 'y',
    
    set_axis: function(axis) {
      switch(axis) {
        case 'y':
          this.axis = 'y';
          this.reset_html();
          this.$wrapper.addClass('horizontal-columns');
          this.resize();
          break;
        case 'x':
          this.axis = 'x';
          this.reset_html();
          this.$wrapper.removeClass('horizontal-columns');
          this.resize();
          break;
      }
    },
    
    container_window_lines_size: function() {
      return this.axis == 'y' ? this.$container_window.width() : this.$container_window.height();
    },
    
    container_offset: function() {
      return this.axis == 'y' ? this.$container.offset().top : this.$container.offset().left;
    },
    
    column_min_size: function() {
      return this.axis == 'y' ? 50 : 100;
    },
    
    get_box_offset_in_column: function(position) {
      var offset = this.axis == 'y' ? position.left : position.top;
      return offset / this.drawing_column_line_height;
    },
    
    box_offset_and_size_for_column: function(offset, size) {
      var setting = {};
      setting[this.axis == 'y' ? 'left' : "top"] = offset*this.drawing_column_line_height;
      setting[this.axis == 'y' ? 'width' : "height"] = size * this.drawing_column_line_height;
      return setting;
    },
    
    // offset: count of linesfrom the earl edge of column
    // size: floating number of lines size
    set_box_offset_and_size_for_column: function($box, offset, size) {
      $box.css(this.box_offset_and_size_for_column(offset,size));
    },
    
    _set_geometry: function(geometry) {
      this.geometry = geometry;
    },
    
    column_of_id: function(column_id, offset) {
      if (offset == undefined)
        offset = 0;
      
      for(var i = 0; i < this.drawing_columns_list.length; i++) {
        if (this.drawing_columns_list[i].column_id == column_id)
          break;
      }
      
      i = i + offset;
      return i < this.drawing_columns_list.length && i >= 0 ?
        this.drawing_columns_list[i] : null;
      
    },
    
    // redraws whole view
    render: function() {
        this.resize();
    },

    // refresh sizes of view
    resize: function() {        
        if (this.axis == 'y') {
          this.in_column_size_attr = 'width';
          this.in_column_offset_attr = 'left';
          this.columns_offset_attr = 'top';
          this.columns_size_attr = 'height';
        } else {
          this.in_column_size_attr = 'height';
          this.in_column_offset_attr = 'top';
          this.columns_offset_attr = 'left';
          this.columns_size_attr = 'width';
        }
        // set view parameters
        this.drawing_view_width = this.$el[this.columns_size_attr].call(this.$el);
        this.drawing_column_width = this.drawing_view_width / Math.floor(this.drawing_view_width / this.column_min_size());
        this.drawing_columns = Math.ceil(this.drawing_view_width / this.drawing_column_width) + this.drawing_columns_overlap;
        
        this.$container[this.columns_size_attr].call(this.$container, this.drawing_columns*this.drawing_column_width);
        this.$bar_container[this.columns_size_attr].call(this.$bar_container, this.drawing_columns*this.drawing_column_width);
        
        this.render_columns();
        this.render_headers();
        this.update_columns();
    },

    // renders main grid columns
    render_columns: function() {
        console.log("render_columns");
      
        this.$grid_el.empty();
        this.$grid_overlay_el.empty();
        this.$headers_el.empty();

        // keep first column id
        var first_id = this.drawing_columns_list[0].column_id;
        this.drawing_columns_list = [];

        var max_lines = this.geometry.get_global_geometry().column_max_lines;
        
        var line_proto = $('<div class="line-label" />').css(this.in_column_size_attr, this.drawing_column_line_height + "px");
        
        // create number of visible columns plus 2 for left and right overflow
        for( var i = 0; i < this.drawing_columns; i++) {            
            var gridcol = $("<div class='grid-col' />");
            gridcol.css(this.columns_size_attr, this.drawing_column_width);
            gridcol.css(this.columns_offset_attr, i*this.drawing_column_width);

            var label_el = $("<div class='headers-col'/>");
            label_el.css(this.columns_size_attr, this.drawing_column_width);
            label_el.css(this.columns_offset_attr, i*this.drawing_column_width);
            
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
        // set container size to fit in column cells
        this.$container.css(this.in_column_size_attr,max_lines * this.drawing_column_line_height);
        
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
            var label_el = $("<span class='headers-col-label'></span>");
            var header_el = $("<div class='headers-col'></div>")
              .append(label_el);

            this.$supercols_el.append(header_el);
            this.drawing_columns_supercols.push({$el: header_el, label_el: label_el.get(0)});
        }
        
    },

    // updates entire view columns
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
        
        this.$grid_overlay_el.empty();
        this.trigger("columns_updated");
    },
    
    // updates columns in the grid (not headers)
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
            this.drawing_columns_list[i].$column.css(this.in_column_size_attr,col_height+"px");
            this.drawing_columns_list[i].$label.get(0).innerHTML = (columns_specs[i].title);
            
            //update line labels
            this.update_column_lines(this.drawing_columns_list[i], columns_specs[i].lines_labels);
            
            first_even = !first_even;
        }

    },
    
    update_column_lines: function(column, line_labels) {
      for(var i in line_labels) {
        var $label = $(column.line_label_els[i]);
        $label.removeClass("delimiter-right delimiter-left delimiter-top shaded-line");
        column.line_label_els[i].innerHTML = line_labels[i].label;
        if (line_labels[i].style == 'shaded') {
          $label.addClass('shaded-line') ;
        }
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
        this.drawing_columns_supercols[i-1].$el.css(this.columns_size_attr, (width_pixels) + "px");
        previous_left_pixels = left_pixels;
        
        this.drawing_columns_supercols[i].$el.css(this.columns_offset_attr, (left_pixels) + "px");
        this.drawing_columns_supercols[i-1].label_el.innerHTML = supercolumns[i-1].label;
        
        // process lines for marking them delimiters
        this._setup_delimiter_column(i_column, supercolumns[i].start_part);
      }
      
      // set width last supercol (wchich breaks at the right edge of view)
      var width_pixels = this.drawing_column_width*this.drawing_columns - previous_left_pixels;
      this.drawing_columns_supercols[i-1].$el.css(this.columns_size_attr, (width_pixels) + "px");
      this.drawing_columns_supercols[i-1].label_el.innerHTML = supercolumns[i-1].label;
      
      // set unused columns to width:0
      for(;i<this.drawing_columns_supercols.length;i++){
        this.drawing_columns_supercols[i].$el.css(this.columns_size_attr,0);
        this.drawing_columns_supercols[i].$el.css(this.columns_offset_attr,0);
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
    
    shift_cols: function(number) {
      var columns_spec = this.geometry.get_columns(this.drawing_columns, this.drawing_columns_list[0].column_id, number);
      var first_even = ('$column' in this.drawing_columns_list[0] ? this.drawing_columns_list[0].$column.is(".even-col") : false);
      // if shift is even, first columns keeps even
      first_even = (number % 2 == 0) ? first_even : !first_even;
      this.update_columns(columns_spec, first_even);
    },
    
    get_view_center_column: function() {
      return (-this.$container.position()[this.columns_offset_attr]  + (this.drawing_view_width/2)) / this.drawing_column_width;
    },
    
    // rederaws columns in order to make currently display columns on view centered in all columns
    center_columns: function() {      
      //compute how much columns prepend/append
      var center_column = this.drawing_columns / 2;
      var current_center_column = this.get_view_center_column();
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
    },

    scroll: function(pixels) {

        //console.log("scroll ", pixels);
        var container_offset = this.$container.position();
        this.$container.css(this.columns_offset_attr, container_offset[this.columns_offset_attr] + pixels);
        this.$bar_container.css(this.columns_offset_attr, container_offset[this.columns_offset_attr] + pixels);

        this.on_move(this.$container.position());
    },
    
    scrollTo: function(offset){
        this.$container.css(this.columns_offset_attr, offset + "px");
        this.$bar_container.css(this.columns_offset_attr, offset + "px");

        this.on_move(this.$container.position());
    },

    set_movers_offset: function(left){
        this.$container.css(this.columns_offset_attr, left);
        this.$bar_container.css(this.columns_offset_attr, left);
        this.update_supercol_label_position(left);        
    },

    // redraw columns in order to currently leftmost visible column will be prepended with edge columns (ie align columns roll to center of view)
    on_move: function(current_offset) {

        var grid_offset = current_offset[this.columns_offset_attr];
      
        // check if drawed columns reached screen edge from inside
        if (grid_offset > 0) {
          // make virtual column which would be on the left edge non-virtual - ie prepend it
          this.center_columns();
          
          return true;
        } else if (grid_offset <  -this.drawing_column_width * this.drawing_columns_overlap) {
          // make virtual column which would be on the right edge non-virtual - ie append it
          this.center_columns();
          
          return true;
        }
        this.update_supercol_label_position(grid_offset);        
        
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
          var sizeDOMAttr = this.axis =='y'? 'offsetHeight' : 'offsetWidth';
          var offsetDOMAttr = this.axis =='y'? 'offsetTop' : 'offsetLeft';
          
          for(var i = 0; i < this.drawing_columns_supercols.length;i++) {
            var header_dom = this.drawing_columns_supercols[i].$el.get(0);
            // always get width of label, because it is rotated by css transform.
            var label_size = this.drawing_columns_supercols[i].label_el.offsetWidth;
            
            this.drawing_columns_supercols[i].label_el.style[this.columns_offset_attr] =
            Math.max(0, Math.min(
                      header_dom[sizeDOMAttr] - label_size,
                      -header_dom[offsetDOMAttr] - left)
                    ) + "px";
          }
        }
    },

    // handle drag event
    on_drag: function(other, e, ui) {
      // drag also other elements
      other.css(this.columns_offset_attr,ui.position[this.columns_offset_attr]+"px");
      
      if (this.on_move(ui.position)) {
        // reset mouse position in drag
        if (typeof ui.helper !== "undefined" && typeof ui.helper.kinetic_draggable === "function") {
          ui.helper.kinetic_draggable("resetMouse", e);
        }
      }
    }
});

});