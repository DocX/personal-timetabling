// (c) 2013 Lukas Dolezal

// linear deceleration easing
$.easing.lineardeceleration = function(t) {
            // 1 = -a/2 + a; 2 = -a + 2a; a = 2;
            return 2*t - (t*t);
         };

// kinetic draggable widget, extends ui.draggable by kinetic effect after drag end        
$.widget("pt.kinetic_draggable", $.ui.mouse, {
   _create: function() {
      this._drag_stack = [];
      
      this._mouseInit();
   },

   resetMouse: function(event) {
      if (this._animation != null)
         return;

      //The element's absolute position on the page minus margins
      this.offset = this.positionAbs = this.element.position();

      this.click = { //Where the click happened,
        x: event.pageX ,
        y: event.pageY
      };

      console.log("mouse reset", this.offset, this.click);
   },

   _mouseDrag: function(event) {
      console.log("mouse drag", event.pageX, event.pageY);
      // store last 5 events
      this._drag_stack.push({mouseX: event.pageX, mouseY: event.pageY, time: new Date().getTime()});
      if (this._drag_stack.length > 3)
      {
         this._drag_stack.shift();
      }

      var x_delta = event.pageX - this.click.x;
      var y_delta = event.pageY - this.click.y;

      // set offset to delta from click point
      var ui = {};
      ui.helper = this.element;
      if (this.options.axis == 'x') {
        ui.position = {left: this.offset.left + x_delta, top: this.offset.top};
        this.element.css({"left":ui.position.left+"px"});
      } else {
        ui.position = {left: this.offset.left , top: this.offset.top + y_delta};
        this.element.css({"top":ui.position.top+"px"});        
      }

      this._trigger("drag", event, ui);
      
   },

    _animation: null,

    animation : function(v, event) {
        if (Math.abs(v) == 0 || this._animation == null) {
            this._animation = null;
            return;
        }
        var offset = this.element.position();
        if (this.options.axis == 'x') {
          offset.left = offset.left + (v*0.01);
          this.element.css({"left":offset.left+"px"});
        } else {
          offset.top = offset.top + (v*0.01);
          this.element.css({"top":offset.top+"px"});          
        }
        
        this._trigger("drag", event, {position:offset});
        this._animation = setTimeout(_.bind(this.animation, this, (v < 0 ? -1 : 1) * Math.max(Math.abs(v) - 100,0), event), 10);
      },
    
   _mouseStop: function(event) {
      // start kinetic animation
      var speed = this._averageSpeed();
     
      this._animation = setTimeout(_.bind(this.animation, this, this.options.axis == 'x' ? speed.xv : speed.yv, event), 10);
   },

   _mouseStart: function(event) {
        //stop animation
        if (this._animation != null){
            clearTimeout(this._animation);
            this._animation = null;
        }

        this._drag_stack = [];
        
        this.resetMouse(event);
        console.log("mousedown");
    },

   _averageSpeed: function() {
      var avg = { xv: 0, yv: 0 };
      var time = 0;

      if (this._drag_stack.length == 0)
          return avg;
      
      for(var i = 0; i< this._drag_stack.length-1; i++) {
         avg.xv += this._drag_stack[i+1].mouseX - this._drag_stack[i].mouseX;
         avg.yv += this._drag_stack[i+1].mouseY - this._drag_stack[i].mouseY;
         time += this._drag_stack[i+1].time - this._drag_stack[i].time;
      }

      avg.xv = 1000 * avg.xv / time;
      avg.yv = 1000 * avg.yv / time;
      return avg;
   }

});
