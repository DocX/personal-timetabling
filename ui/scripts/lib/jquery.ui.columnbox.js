// (c) 2013 Lukas Dolezal
"use strict";

define(['jquery', 'jquery-ui'], function($) {

// box moving and resizing in column view
$.widget("pt.column_box", $.ui.mouse, {
  options: {
    // x is that box is moved inside column in x axis
    step_minutes: 15,
    view: null,
    resize_handle_class_prefix: 'columnbox-handle-'
  },
  
   _create: function() {
      this._mouseInit();
      
      this.options.orientation = this.options.view.axis == 'x' ? 'y' : 'x';
      
      // determine column
      var start_coord = this.options.view.geometry.get_line_of_date(this.options.start);
      var end_coord = this.options.view.geometry.get_line_of_date(this.options.start.clone().add('seconds', this.options.duration));

      // find column for start
      var column = this.options.view.column_of_id(start_coord.column_id); 
      if (column == null)
        return;
      
      this.column = column;
      this.el_start = this.options.start;
      this.el_duration = this.options.duration;
      
      // set sizes to fit column
      this.element.css(this.options.view.columns_size_attr, this.options.view.drawing_column_width);
      this.element.css(this.options.view.columns_offset_attr, column.$column.position()[this.options.view.columns_offset_attr]);

      this.options.view.set_box_offset_and_size_for_column(this.element, start_coord.line, end_coord.line - start_coord.line);

      // init sizes
   },

   _mouseDrag: function(event) {     
      var x_delta = event.pageX - this.click.x;
      var y_delta = event.pageY - this.click.y;
      
      // get date of current lines
      var column = this.column;
      var el_start = this.el_start;
      var el_duration = this.el_duration;
      var handle_position = this._getDateOfHandle(column, x_delta, y_delta);

      if (handle_position == false)
        return false;
      
      if (this.mode == 'resize-front') {
        // determine new size
        el_duration += el_start.diff(handle_position[0], 'seconds');
        el_start = handle_position[0];
        if (el_duration <= 0 || !this._check(el_start, el_duration))
          return false;
        
        var size = this.options.view.box_offset_and_size_for_column(
          handle_position[2].line,
          this.options.view.geometry.get_line_of_date(el_start.clone().add(el_duration,'s')).line - handle_position[2].line);
        // move box and resize
        this.element.css(size);
      }
      else if (this.mode == 'resize-back') {
        // determine new size
        el_duration = handle_position[0].diff(el_start, 'seconds');
        if (el_duration <= 0 || !this._check(el_start, el_duration))
          return false;
        
        var start_line = this.options.view.geometry.get_line_of_date(el_start).line;
        var size = this.options.view.box_offset_and_size_for_column(
          start_line,
          handle_position[2].line - start_line);
        // move box and resize
        this.element.css(size);
      }
      else if (this.mode == 'move') {
        // process move from columns
        var column_move = this._getMouseColumn(x_delta, y_delta);
        handle_position = this._getDateOfHandle(column_move[0], x_delta, y_delta);
        el_start = handle_position[0];

        if (!this._check(el_start, el_duration)) { return false };

        // move box
        this.element.css(handle_position[1]);  
        // process potential inter-column move
        this._processInterColumnMove(column_move);
      }

      this.el_start = el_start;      
      this.el_duration = el_duration;
      this._trigger("drag", event, {el: this.element, date_front:el_start, duration: el_duration});      
   },

   _check: function() {},
   
   _getDateOfHandle: function(column, x_delta, y_delta) {
      var new_position = $.extend({},this.start_position);
      new_position.left += x_delta;
      new_position.top += y_delta;

      var handle_line_in_col = this.options.view.get_box_offset_in_column(new_position);
      // if handle is out of column
      if (handle_line_in_col < 0)
        return false;
      
      // gets rounded date of line and then move element to pixel corresponding to date
      var mouse_date = this.options.view.geometry.get_date_of_line(column.column_id, handle_line_in_col, this.options.step_minutes);
      var date_column = this.options.view.geometry.get_line_of_date(mouse_date);
      var date_offset = this.options.view.box_offset_and_size_for_column(date_column.line, 0);
      delete date_offset.height;
      delete date_offset.width;
      
      return [mouse_date, date_offset, date_column];
   },
   
   _getMouseColumn: function(x_delta, y_delta) {
      var columns_delta = 
        // |    box       |
        // |        ^mouse|
        // |              |   ^mousenow => +1
        this.options.orientation == 'x' ?
        (this.element_click.y + y_delta) :
        (this.element_click.x + x_delta);
        //console.log('column_delta', columns_delta, this.options.view.drawing_column_width);
      var columns_delta = 
        Math.floor(columns_delta / this.options.view.drawing_column_width);
      
      if (columns_delta - this.current_columns_delta != 0) {        
        var column = this.options.view.column_of_id(this.column.column_id, columns_delta - this.current_columns_delta);
        return [column, columns_delta]
      }
      return [this.column,0];
   },

   _processInterColumnMove: function(move_column) {

      if (move_column[0] != null && move_column[0] != this.column) {         
        this.column = move_column[0];
        
        this.current_columns_delta = move_column[1];
        
        this.element.css(this.options.view.columns_offset_attr, this.column.$column.position()
          [this.options.view.columns_offset_attr] );
      }
   },
    
   _mouseStop: function(event) {
      // start kinetic animation
     this.element.removeClass('ui-draggable-dragging');
     this._trigger("stop", event, {el:this.element});
   },

   _mouseStart: function(event) {
        if($(event.toElement).is('.' + this.options.resize_handle_class_prefix + 'front')) {
          this.mode = 'resize-front';
          this.start_position = this.element.position();
        } else if($(event.toElement).is('.' + this.options.resize_handle_class_prefix + 'back')) {
          this.mode = 'resize-back';
          this.start_position = this.element.position();
          this.start_position[this.options.view.in_column_offset_attr] += 
          this.element[this.options.view.in_column_size_attr].call(this.element);
        }
        else {
          this.mode = 'move';
          this.start_position = this.element.position();
        }

        
        
        this.current_columns_delta = 0;
        var element_offset = this.element.offset();
        this.element_click = {
          x: event.pageX - element_offset.left,
          y: event.pageY - element_offset.top,
        }
        //console.log('offset click', this.element_click);
        
        this.click = { //Where the click started
          x: event.pageX ,
          y: event.pageY
        };
        
        this.element.addClass('ui-draggable-dragging');
    }
});


});