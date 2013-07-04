// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment');
    
// View controller for calendar buttons
return Backbone.View.extend({

	template:
		"<span class='label label-important'>communicating...</span>",

	initialize :function() {
		this.$el.append(this.template);
		this.$el.addClass('rightcomponents');
		
		$(document).on('ajaxStart', _.bind(this.ajax_state,this, true));
    	$(document).on('ajaxStop', _.bind(this.ajax_state, this,false));

    	this.ajax_state(false);
	},

	ajax_state: function(state) {
		this.$el.toggle(state);
	}

});

});