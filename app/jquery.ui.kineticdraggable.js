// (c) 2013 Lukas Dolezal

// linear deceleration easing
$.easing.lineardeceleration = function(t) {
            // 1 = -a/2 + a; 2 = -a + 2a; a = 2;
            return 2*t - (t*t);
         };

// kinetic draggable widget, extends ui.draggable by kinetic effect after drag end        
$.widget("pt.kinetic_draggable", $.ui.draggable, {
   _create: function() {
      this._drag_stack = [];
      this.element.data("ui-draggable", this);
      this._super();
   },

   resetMouse: function(event) {
      if (this._animation != null || this.helper == null || !this.helper.is(".ui-draggable-dragging"))
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

    _animation: null,

    animation : function(xv, event) {
        if (Math.abs(xv) == 0 || this._animation == null) {
            this._animation = null;
            return;
        }
        var offset = this.element.offset();
        this.element.offset({left: offset.left + (xv*0.01)});
        this._trigger("drag", event, {position:this.element.offset()});
        this._animation = setTimeout(_.bind(this.animation, this, (xv < 0 ? -1 : 1) * Math.max(Math.abs(xv) - 100,0), event), 10);
      },
    
   _mouseUp: function(event) {
      var result = this._super(event);

      // start kinetic animation
      var speed = this._averageSpeed();
     
      this._animation = setTimeout(_.bind(this.animation, this, speed.xv, event), 10);
      
      return result;
   },

   _mouseDown: function(event) {
        //stop animation
        if (this._animation != null){
            clearTimeout(this._animation);
            this._animation = null;
        }

        this._drag_stack = [];
        
        this._super(event);
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
      avg.yv = 1000 * avg.xv / time;
      return avg;
   }

});
