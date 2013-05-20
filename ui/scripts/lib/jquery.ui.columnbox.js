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

    this.orientation = this.options.view.axis == 'x' ? 'y' : 'x';
    
    if(!this.options.group) {
      this.options.group = {
        start: this.options.start,
        duration: this.options.duration,
        update: _.bind(this._update, this),
        check: _.bind(this._check, this),
      };
      this.group_first = true;
    }
    if (!this.options.offset_in_group) {
      this.options.offset_in_group =  0;
    }
    
    // init position and size
    this._update();
  },

  _mouseStart: function(event) {
    if($(event.toElement).is('.' + this.options.resize_handle_class_prefix + 'front')) {
      this.mode = 'resize-front';
    } else if($(event.toElement).is('.' + this.options.resize_handle_class_prefix + 'back')) {
      this.mode = 'resize-back';
    }
    else {
      this.mode = 'move';
    }

    var element_offset = this.element.offset();
    this.mouse_start_element = {
      x: event.pageX - element_offset.left,
      y: event.pageY - element_offset.top,
    }

    this.mouse_start_offset_in_group = this.options.offset_in_group;

    this.element.addClass('ui-draggable-dragging');
  },    

  _mouseDrag: function(event) {        
    switch(this.mode) {
      case 'resize-front':
        this.front_to(this._get_date_of_coordinates(event.pageX, event.pageY));
        break;
      case 'resize-back':
        this.back_to(this._get_date_of_coordinates(event.pageX, event.pageY));
        break;
      case 'move': 
        this.move_this_to(this._get_date_of_coordinates(
          event.pageX - (this.orientation == 'x' ? this.mouse_start_element.x : 0), 
          event.pageY - (this.orientation == 'y' ? this.mouse_start_element.y : 0)
        ));
        break;
    }

    this._trigger("drag", event, {el: this.element, date_front:this.options.group.start, duration: this.options.group.duration, mouse:true});  
  },

  _mouseStop: function(event) {
    // start kinetic animation
    this.element.removeClass('ui-draggable-dragging');
    if (!this.options.master) {
      this._trigger("stop", event, {el:this.element});
    }
  },



  front_to: function(date) {
    // determine new size
    var new_duration = this.options.group.duration + this.options.group.start.diff(date, 'seconds');
    this.set_date(date, new_duration);      
  },

  back_to: function(date) {
    // determine new size
    var new_duration = date.diff(this.options.group.start, 'seconds');
    this.set_date(this.options.group.start, new_duration);
  },

  move_to: function(date) {
    this.set_date(date, this.options.group.duration);
  },

  move_this_to: function(date) {
    // add in group offset to date
    date.add('s', -this.mouse_start_offset_in_group);
    this.set_date(date, this.options.group.duration);
  },

  // sets date of whole group
  set_date: function(start, duration) {
    if (!this.options.group.check(start, duration)) {
     return false
    };

    this.options.group.start = start;
    this.options.group.duration = duration;

    // update only if first otherwise delegate update to the first
    if (this.group_first) {
      //console.log ('first group member updated', this.options.group.start.format(), this.options.group.duration);
      this._update();
    } else {
      //console.log ('not first group member updated', this.options.group.start.format(), this.options.group.duration);
      this.options.group.update();
    }
  },

  _update: function() {
    //console.log ('updated', this.group_first, this.options.group.start.format(), this.options.group.duration);
    var start_line = this.options.view.geometry.get_line_of_date(this.options.group.start.clone().add('s', this.options.offset_in_group));
    var end_date = this.options.group.start.clone().add('s', this.options.group.duration);
    var end_line = this.options.view.geometry.get_line_of_date(end_date);
    
    //find column in view
    var start_column = this.options.view.column_of_id(start_line.column_id);
    var end_column = this.options.view.column_of_id(end_line.column_id);

    if (start_column && end_column) {
      var size = this.options.view.box_offset_and_size_for_column(
        start_line.line,
        end_column.column_id > start_column.column_id ? start_column.lines_labels.length - start_line.line : end_line.line - start_line.line);

      // move box and resize
      this.element.css(size);
      this.element.css(this.options.view.columns_size_attr, this.options.view.drawing_column_width);
      this.element.css(this.options.view.columns_offset_attr, start_column.$column.position()[this.options.view.columns_offset_attr]);

      this._process_overflow();
    }

    //this._trigger("drag", event, {el: this.element, date_front:this.options.group.start, duration: this.options.group.duration, mouse:false});
  },

  _process_overflow: function(){
    var start_date = this.options.group.start.clone().add('s', this.options.offset_in_group);
    var start_line = this.options.view.geometry.get_line_of_date(start_date);
    var start_column_end = this.options.view.geometry.get_end_of_column(start_line.column_id);
    var end_date = this.options.group.start.clone().add('s', this.options.group.duration);

    var next_group_offset = start_column_end.diff(this.options.group.start, 'seconds');

    if (start_column_end.isAfter(end_date) || start_column_end.isSame(end_date)) {
      if (this.group_next_box)  {
        this._destroy_box(this.group_next_box);
        this.group_next_box.remove();
        this.group_next_box = null;
      }

      return;
    }

    if (this.group_next_box) {
      this.group_next_box.column_box('set_group_offset', next_group_offset);
    }else {
      // copy HTML of element including its own outer HTML
      this.group_next_box = $('<div/>').append(this.element.clone());
      this.group_next_box = $(this.group_next_box.html());

      this.element.after(this.group_next_box);
      var next_options = $.extend(
          {},
          this.options, 
          {offset_in_group: next_group_offset}
        );
      this.group_next_box.column_box(next_options);
      this.group_next_box.column_box('set_group', this.options.group);
      this._setup_box(this.group_next_box);
    }
  },

  // override to setup box created for overflow
  _setup_box: function(box) {},
  _destroy_box: function(box) {},

  set_group: function(group_object) {
    this.options.group = group_object;
  },

  set_group_offset: function(offset){
    this.options.offset_in_group = offset;
    this._update();
  },

  // override to implement validation of moving
  _check: function(start, duration) {
    return duration > 0;
  },

  _get_date_of_coordinates: function(x, y) {
    var plane_offset = this.options.view.get_plane_offset_to_document();
    var coordinates = {left: x - plane_offset.left, top:y - plane_offset.top};

    var column_line = this.options.view.get_column_line_by_offset(coordinates);
    
    // gets rounded date of line and then move element to pixel corresponding to date
    var mouse_date = this.options.view.geometry.get_date_of_line(column_line.column_id, column_line.line, this.options.step_minutes);    
    return mouse_date;
  },


});

});