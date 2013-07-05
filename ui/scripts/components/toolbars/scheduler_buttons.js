// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment');
    
// View controller for calendar buttons
return Backbone.View.extend({

	template:
		"<div class='rightcomponents' >" +
			"<div class='btn-group'>" +
      			"<button class='btn btn-inverse active' data-role='mode-running'>Running</button>" +
        		"<button class='btn btn-inverse' data-role='mode-planning'>Planning</button>" +
      		"</div>" + 
		"</div>",

	initialize: function() {
		this.$el.append(this.template);
		this.$el.addClass('rightcomponents');

		$("button[data-role=mode-running]").click(_.bind(this.set_mode, this, 'running', true));
		$("button[data-role=mode-planning]").click(_.bind(this.set_mode, this, 'planning', true));
		this.listenTo(this.options.app, 'change:scheduler_mode', this.set_mode);
	},

	set_mode: function(mode, set_scheduler) {
		switch(mode) {
			case 'running':
				$("button[data-role=mode-running]").addClass('active');
				$("button[data-role=mode-planning]").removeClass('active');
				break;
			case 'planning':
				$("button[data-role=mode-planning]").addClass('active');
				$("button[data-role=mode-running]").removeClass('active');
				break;
		}

		if (set_scheduler == true) {
			this.options.scheduler.set_mode(mode);
		}
		
	},


});

});