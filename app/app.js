// (c) 2013 Lukas Dolezal

var PersonalTimetabling = {};
var PT = PersonalTimetabling;

PersonalTimetabling.CalendarView = Backbone.View.extend({
   
   options: {
      // zoom means how much days are visible in one screen
      zoom: 0.5,
      cols_overflow: 5
   },
   
   initialize: function() {
      
      this.$container = $("<div id='mover' />").appendTo(this.$el);
      this.$grid_el = $("<div id='grid' />").appendTo(this.$container);
      this.$headers_el = $("<div id='grid-headers' />").appendTo(this.$container);
      this.$tasks_el = $("<div id='tasks' />").appendTo(this.$container);
      
      this.$container.kinetic_draggable({axis: 'x', drag:_.bind(this.gridDrag, this), stop: _.bind(this.gridDrag, this)});
      
      var now = new Date();
      this.first_col_date = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0,0,0);
      
     
   },
   
   render: function() {
      this.resize();      
      this.renderGrid();
      this.renderHeaders();
   },
   
   resize: function() {
      var headers_h = this.$headers_el.outerHeight();
      this.$grid_el.height(this.$el.height() - headers_h).css("top", headers_h);
      this.$tasks_el.height(this.$el.height() - headers_h).css("top", headers_h);
      this.$container.height(this.$el.height());
   },
   
   _set_sizes: function() {
      this._col_minutes = 60;
      this._col_num = Math.ceil((this.options.zoom  * 24 * 60) / this._col_minutes); 
      this._col_width = this.$el.width() / this._col_num;
   },
   
   renderGrid: function() {
   
      this._set_sizes();
   
      this.$grid_el.empty();
   
      // create number of visible columns plus 2 for left and right overflow
      for( var i = 0; i < this._col_num + 2*this.options.cols_overflow; i++) {
         var col_date = this.first_col_date.addMinutes(this._col_minutes * i);
         this.$grid_el.append($("<div class='grid-col grid-col-" + (i % 2 == 0 ? "even" : "odd") + "' />")
            .width(this._col_width)
            .css("left", this._col_width * i)
            .text(col_date.toString())           
         );
      }
      
      this.$container.width((this._col_num + 2*this.options.cols_overflow) * this._col_width);
      this.$container.css("left", -this.options.cols_overflow * this._col_width);
      
   },
   
   renderHeaders: function() {
      this.$headers_el.empty();
      
      var currentStart = this.first_col_date;
      var currentLeft = 0;
      var even = false;
      
      while(currentLeft < this.$headers_el.width()) {
         var nextDay = currentStart.getNoon().addDays(1);
         var minutes = nextDay.diffMinutes(currentStart);
         var width = this._col_width * (minutes / this._col_minutes);
         width = Math.min(width, this.$headers_el.width());
         
         
         this.$headers_el.append(
            $("<div class='header-col' />")
            .addClass(even ? "header-col-even" : "header-col-odd")
            .width(width)
            .css("left", currentLeft)
            .text(currentStart.getDate() + " " + currentStart.getMonth() + " " + currentStart.getFullYear())
         );
         
         currentLeft += width;
         currentStart = nextDay;
         even = !even;
      }
   },
   
   
   _prepend_cols: function(number){    
      this.first_col_date = this.first_col_date.addMinutes(-number * this._col_minutes);
      
      var even = this.$grid_el.children().first().hasClass("grid-col-even") ? 1 : 0;
      // even = 1  last = 1 => even = 0
      // even = 1  last = 0 => even = 1
      // even = 0  last = 1 => even = 1
      // even = 0  last = 0 => even = 0
      even = (((number - 1) % 2) == 1) ^ (even == 1) ? 1 : 0;
      
      // move current columns
      var move_distance = number * this._col_width;
      var current_cols = this.$grid_el.children();
      var remove_thresold = current_cols.length - number;
      current_cols.each(function(i,el) {
         if(i >= remove_thresold)
         {
            $(el).remove();
         }
         else
         {
            $(el).css("left", "+=" + move_distance);
         }
      });
      
      // create number of visible columns plus 2 for left and right overflow
      for( var i = number-1; i >= 0 ; i--) {
         var col_date = this.first_col_date.addMinutes(this._col_minutes * i);
         this.$grid_el.prepend($("<div class='grid-col grid-col-" + (i % 2 == even ? "even" : "odd") + "' />")
            .width(this._col_width)
            .css("left", this._col_width * i)
            .text(col_date.toString())
         );
      }
      
   },
   
   _append_cols: function(number){ 
      this.first_col_date = this.first_col_date.addMinutes(number * this._col_minutes);
      
      var even = this.$grid_el.children().last().hasClass("grid-col-even") ? 1 : 0;
      
      // move current columns
      var move_distance = number * this._col_width;
      var current_cols = this.$grid_el.children();

      current_cols.each(function(i,el) {
         if(i < number)
         {
            $(el).remove();
         }
         else
         {
            $(el).css("left", "-=" + move_distance);
         }
      });
      
      var before_cols =  (current_cols.length-number);
      // create number of visible columns plus 2 for left and right overflow
      for( var i = 0; i < number ; i++) {
         var col_date = this.first_col_date.addMinutes(this._col_minutes * (i  + before_cols));
         this.$grid_el.append($("<div class='grid-col grid-col-" + (i % 2 == even ? "even" : "odd") + "' />")
            .width(this._col_width)
            .css("left", this._col_width * (i+before_cols))
            .text(col_date.toString())
         );
      }
      
   },
   
   _rearanging: false,
   
   gridDrag: function(e, ui) {
   
      var grid_left = ui.position.left;
      if (grid_left > 0) {
         if (this._rearanging === true)
            return;
      
         this._rearanging = true;
      
         //rearange
         // ceil number of cols to multiply of cols_overflow
         var number = Math.ceil(grid_left / (this._col_width * this.options.cols_overflow)) * this.options.cols_overflow;

         this._prepend_cols(number);
         
         // move to seamless position
         this.$container.css("left", (-this.options.cols_overflow * this._col_width) + (grid_left % (this.options.cols_overflow * this._col_width)));
         

         this.$container.kinetic_draggable("resetMouse", e);
         this._rearanging = false;
         
         this.renderHeaders();
      } else if (grid_left < -this._col_width*2*this.options.cols_overflow ) {
         if (this._rearanging === true)
            return;
      
         this._rearanging = true;
         
         var overflow = -(grid_left + this._col_width*2*this.options.cols_overflow);
         
         //rearange
         var number = Math.ceil(Math.ceil(overflow / this._col_width) / this.options.cols_overflow) * this.options.cols_overflow;
         this._append_cols(number);
         
         // move to seamless position
         this.$container.css("left", (-this.options.cols_overflow * this._col_width) - (overflow % (this.options.cols_overflow * this._col_width)));
         

         this.$container.kinetic_draggable("resetMouse", e);
         this._rearanging = false;
                  this.renderHeaders();
      } 
   
      
   }
   
});

PersonalTimetabling.App = Backbone.View.extend({
   
   initialize: function() {
      this.calendar_view = new PersonalTimetabling.CalendarView({el: $("#calendarview")});
      this.$topbar = $("#topbar");
      
      $(window).resize(function(that) { return function() {that.resize();} } (this));
      this.render();
   },
   
   render: function() {
      this.resize();
      this.calendar_view.render();  
   },
   
   resize: function() {
      var window_h = $(window).height();
      var topbar_h = this.$topbar.outerHeight();
      this.calendar_view.$el.height(window_h - topbar_h);
      this.calendar_view.resize();
   }
   
   
});

$(document).ready(function() {
   PT.AppInstance = new PT.App(); 
   PT.AppInstance.render();  
});

$.easing.lineardeceleration = function(t) {
            // 1 = -a/2 + a; 2 = -a + 2a; a = 2;
            return 2*t - (t*t);
         };

$.widget("pt.kinetic_draggable", $.ui.draggable, {
   _create: function() {
      this._drag_stack = [];
      this.element.data("ui-draggable", this);
      this._super();
      

   },
   
   resetMouse: function(event) {
      if (this.helper == null || !this.helper.is(".ui-draggable-dragging"))
         return;
   
      //Cache the margins of the original element
		this._cacheMargins();
   
      //Store the helper's css position
		this.cssPosition = this.helper.css("position");
		this.scrollParent = this.helper.scrollParent();
   
      //The element's absolute position on the page minus margins
		this.offset = this.positionAbs = this.element.offset();
		this.offset = {
			top: this.offset.top - this.margins.top,
			left: this.offset.left - this.margins.left
		};

		$.extend(this.offset, {
			click: { //Where the click happened, relative to the element
				left: event.pageX - this.offset.left,
				top: event.pageY - this.offset.top
			},
			parent: this._getParentOffset(),
			relative: this._getRelativeOffset() //This is a relative to absolute position minus the actual position calculation - only used for relative positioned helper
		});

		//Generate the original position
		this.originalPosition = this.position = this._generatePosition(event);
		this.originalPageX = event.pageX;
		this.originalPageY = event.pageY;
   },
   
   _mouseDrag: function(event) {
      // store last 5 events
      this._drag_stack.push({mouseX: event.pageX, mouseY: event.pageY, time: new Date().getTime()});
      if (this._drag_stack.length > 3)
      {
         this._drag_stack.shift();
      }
      
      return this._super(event);
   },
   
   _mouseUp: function(event) {
      var result = this._super(event);
      
      // start kinetic animation
      var speed = this._averageSpeed();
      // speed = pixels / s;
      // keep speed and decelerate constantly by 1px/s^2
      // time = speed / deceleration
      // destination = speed * time / 2
      var timex = Math.abs(speed.xv) / 200;
      /*
      this.element.animate({
         left: '+='+(Math.max(Math.min((Math.abs(speed.xv) * timex / 2),0), 500) * (speed.xv < 0 ? -1 : 1))
      }, {
         duration: timex*1000,
         step: _.bind(function(now,fx) {
            var $el = $(fx.elem);
            var _now = Math.round($el.offset().left);
            this._trigger("drag", event, {position:$el.offset()});
            
            var __now = Math.round($el.offset().left);
            if (__now != _now) {
               //fx.now = __now;
               fx.end += fx.now - _now;
               fx.start += fx.now - _now;
            }
            
         }, this),
         easing: 'lineardeceleration'
         });
      */
      return result;
   },
   
   _averageSpeed: function() {
      var avg = { xv: 0, yv: 0 };
      var time = 0;
      
      for(var i = 0; i< this._drag_stack.length-1; i++) {
         avg.xv += this._drag_stack[i+1].mouseX - this._drag_stack[i].mouseX;
         avg.yv += this._drag_stack[i+1].mouseY - this._drag_stack[i].mouseY;
         time += this._drag_stack[i+1].time - this._drag_stack[i].time;
      }
      
      avg.xv = 1000 * avg.xv / time;
      avg.yv = 1000 * avg.xv / time;
      return avg;
   }
   
});







