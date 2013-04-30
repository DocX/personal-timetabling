// (c) 2013 Lukas Dolezal

// simple implementation of ui.mouse widget firing mouse events.
$.widget("pt.mouse_events", $.ui.mouse, {
  _create: function() {
    this._mouseInit();
  },
  
  _mouseStart: function(e) {
    this._trigger("onstart", e);      
  },

  _mouseEnd: function(e) {
    this._trigger("onend", e);      
  },

});