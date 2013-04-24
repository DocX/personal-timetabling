// (c) 2013 Lukas Dolezal

// box moving and resizing
$.widget("pt.column_box", $.ui.mouse, {
  options: {
    // x is that box is moved inside column in x axis
    orientation: 'x',
    step_minutes: 15,
    view: null,
    resize_handle_class_prefix: 'columnbox-handle-'
  },
  
   _create: function() {
      this._mouseInit();
   },

   _mouseDrag: function(event) {
      var x_delta = event.pageX - this.click.x;
      var y_delta = event.pageY - this.click.y;
      
      // get date of current lines
      var column = this.element.data('column');
      var el_start = this.element.data('start_date');
      var el_duration = this.element.data('duration');
      var handle_position = this._getDateOfHandle(column, x_delta, y_delta);

      if (this.mode == 'resize-front') {
        // determine new size
        el_duration += handle_position[0].secondsUntil(el_start);
        el_start = handle_position[0];
        if (el_duration <= 0)
          return false;
        
        var size = this.options.view.box_offset_and_size_for_column(
          handle_position[2].line,
          this.options.view.geometry.get_line_of_date(el_start.clone().addSeconds(el_duration)).line - handle_position[2].line);
        // move box and resize
        this.element.css(size);
      }
      else if (this.mode == 'resize-back') {
        // determine new size
        el_duration = handle_position[0].secondsSince(el_start);
        if (el_duration <= 0)
          return false;
        
        var start_line = this.options.view.geometry.get_line_of_date(el_start).line;
        var size = this.options.view.box_offset_and_size_for_column(
          start_line,
          handle_position[2].line - start_line);
        // move box and resize
        this.element.css(size);
      }
      else if (this.mode == 'move') {
        // move box
        this.element.css(handle_position[1]);  
        // process potential inter-column move
        this._processInterColumnMove(column, x_delta, y_delta);
        
        el_start = handle_position[0];
      }

      this.element.data('start_date', el_start);        
      this.element.data('duration', el_duration);
      this._trigger("drag", event, {el: this.element, date_front:el_start, duration: el_duration});      
   },
   
   _getDateOfHandle: function(column, x_delta, y_delta) {
      var new_position = $.extend({},this.start_position);
      new_position.left += x_delta;
      new_position.top += y_delta;

      // gets rounded date of line and then move element to pixel corresponding to date
      var mouse_date = this.options.view.geometry.get_date_of_line(column.column_id, this.options.view.get_box_offset_in_column(new_position), this.options.step_minutes);
      var date_column = this.options.view.geometry.get_line_of_date(mouse_date);
      var date_offset = this.options.view.box_offset_and_size_for_column(date_column.line, 0);
      delete date_offset.height;
      delete date_offset.width;
      
      return [mouse_date, date_offset, date_column];
   },
   
   _processInterColumnMove: function(column, x_delta, y_delta) {
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
        column = this.options.view.column_of_id(column.column_id, columns_delta - this.current_columns_delta);
        if (column != null) {         
          this.element.data('column',column);
          
          this.current_columns_delta = columns_delta;
          
          this.element.css(this.options.view.columns_offset_attr, column.$column.position()[this.options.view.columns_offset_attr] )
        }
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
        console.log('offset click', this.element_click);
        
        this.click = { //Where the click started
          x: event.pageX ,
          y: event.pageY
        };
        
        this.element.addClass('ui-draggable-dragging');
    }
});
