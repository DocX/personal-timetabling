// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment');
    
// View controller for calendar buttons
return Backbone.View.extend({

	template:
		"<span class='label label-important' data-role='ajax'>communicating...</span> " +
		"<span class='label label-info' data-role='scheduling'>scheduling...</span>",

	initialize :function() {
		this.$el.append(this.template);
		this.$el.addClass('rightcomponents');
		
		$(document).on('ajaxStart', _.bind(this.ajax_state,this, true));
    	$(document).on('ajaxStop', _.bind(this.ajax_state, this,false));

    	this.listenTo(this.options.app, 'start:scheduling', _.partial(this.scheduling_state,true));
    	this.listenTo(this.options.app, 'done:scheduling', _.partial(this.scheduling_state,false));

    	this.ajax_state(false);
    	this.scheduling_state(false);
	},

	ajax_state: function(state) {
		this.$el.find('[data-role=ajax]').toggle(state);
	},

	scheduling_state: function(state) {
		this.$el.find('[data-role=scheduling]').toggle(state);
	}

});

});