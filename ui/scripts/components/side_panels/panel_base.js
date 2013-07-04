// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone');    

// Panel base provides some basic functions for handling
// panel-like view in application
return Backbone.View.extend({
	remove: function() {
		this.trigger('removed');
		// and remove view
		Backbone.View.prototype.remove.apply(this);
	},

});

});