// (c) 2013 Lukas Dolezal
"use strict";

// Panel base provides some basic functions for handling
// panel-like view in application
PersonalTimetabling.Views.PanelBase = Backbone.View.extend({


	remove: function() {
		this.trigger('removed');
		// and remove view
		Backbone.View.prototype.remove.apply(this);
	},

});