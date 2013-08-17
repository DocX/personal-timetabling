// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment'),
    DomainTemplatesPanel = require('components/side_panels/domain_templates/domain_templates_index'),
    ActivitiesPanel = require('components/side_panels/activities/activities_index');
    
// View controller for calendar buttons
return Backbone.View.extend({

	template: 
		"<div >" +
			"<div class='btn-group '>" +
				"<button class='btn btn-inverse'><i class='icon-user icon-white'></i>  Demo</button>" +
				"<button class='btn dropdown-toggle btn-inverse' data-toggle='dropdown'>" +
					"<span class='caret'></span>" +
				"</button>" +
				"<ul class='dropdown-menu pull-right'>" +
					"<li><a tabindex='-1' href='#' data-role='activities'>Activities</a></li>" +
					"<li><a tabindex='-1' href='#' data-role='domain-template'>Domain templates</a></li>" +
					"<li class='divider'></li>" +
					"<li><a tabindex='-1' href='#' data-role='purge-all'>Purge demo database</a></li>" +
					"<li><a tabindex='-1' href='#' data-role='reset-all'>Reset all events to initial allocation</a></li>" +
					"<li class='divider'></li>" +
					"<li><a tabindex='-1' href='#pt-modal-about' data-toggle='modal' role='button'><i class=' icon-info-sign'></i> About</a></li>" +
				"</ul>" +
			"</div>" +
		"</div>",

	events: {
		"click a[data-role=purge-all]": 'purge_all',
		"click a[data-role=reset-all]": 'reset_all',
	},

	initialize: function() {
		this.$el.append(this.template);
		this.$el.addClass('rightcomponents');
		
		this.$el.find('[data-role=domain-template]').click(_.bind(function() {
			this.options.app.open_panel(DomainTemplatesPanel, {
				calendar_view: this.options.app.calendar_view.calendar,
				activities_view: this.options.app.calendar_view
			});
		}, this));

		this.$el.find('[data-role=activities]').click(_.bind(function() {
			this.options.app.open_panel(ActivitiesPanel, {
				calendar_view: this.options.app.calendar_view.calendar,
				activities_view: this.options.app.calendar_view
			});
		}, this));
	},

	purge_all: function() {
		$.ajax({
			url: '/reset/purge_all',
			type: 'post',
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			processData: false,
			data: "",
			success: _.bind(function(result) {
				window.location.reload();
			}, this),
			error: _.bind(function() {
				alert('failed to purge database');
			}, this)
		});

	},

	reset_all: function() {
		$.ajax({
			url: '/events/all/reset',
			type: 'post',
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			processData: false,
			data: "",
			success: _.bind(function(result) {
				window.location.reload();
			}, this),
			error: _.bind(function() {
				alert('failed to reset events allocations');
			}, this)
		});

	},

});
});