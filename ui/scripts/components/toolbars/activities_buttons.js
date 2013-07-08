// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment'),
    NewActivityPanel = require('components/side_panels/activities/activities_new');
    
// View controller for calendar buttons
return Backbone.View.extend({

	template:
		"<div class='rightcomponents' >" +
			"<div class='btn-group'>" +
				"<a class='btn btn-inverse' data-role='add_activity'><i class='icon-plus icon-white'></i> Add activity</a>" +
			"</div>" +
		"</div>",

	initialize: function() {
		this.$el.append(this.template);
		this.$el.addClass('rightcomponents');

		$("a[data-role=add_activity]").click(_.bind(function(){
			//open sidebar
			this.options.app.open_panel(NewActivityPanel, {activities_view: this.options.app.calendar_view});
	    }, this));
	}


});

});