// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    PanelBase = require('components/panel_base'),
    DomainTemplate = require('models/domain_template'),
    DomainTemplatesCollection = require('models/domain_templates_collection'),
    DomainStackForm = require('components/domain_stack_form');
    
return PanelBase.extend({

	template: 
		"<div id='domain_box'>" +
			"<p><strong>New domain template</strong> <a class='btn' data-role='cancel_btn'>Cancel</a></p>" +

			"<label>Name</label>" +
			"<input name='domain_name' type='text'/>" +
			"<label>Intervals actions:</label>" +
			"<div class='domain_stack_form'></div>" +
			"<div>" +
				"<lable>Save above domain template</label>" +
				"<a href='#' class='btn btn-primary' data-role='save-domain'>Save domain template</a>" +
			"</div>" +
		"</div>",

	events: {
		'click a[data-role=save-domain]': 'save',
		'click a[data-role=cancel_btn]': 'remove',
	},

	initialize: function() {

		this.$el.append(this.template);

		this.listenTo(this.options.calendar_view, 'columns_updated', this.refresh_preview);

		this.domain_form = new DomainStackForm({
			el: this.$el.find('.domain_stack_form')
		});

		this.model = this.domain_form.model;

		this.listenTo(this.model, 'change', this.refresh_preview);
	},

	remove_preview_intervals_display: function() {
		if (!this.handling_boxes)
			return;

		for (var i = this.handling_boxes.length - 1; i >= 0; i--) {
			this.handling_boxes[i].remove();
		};		
	},

	preview_xhr: null,

	refresh_preview: function() {
		console.log("refresh_preview domain");
		if (!this.model.get('domain_stack'))
			return true;

      	// gets display date range
      	var range = this.options.calendar_view.showing_dates();

		this.preview_xhr && this.preview_xhr.abort();
      	this.preview_xhr = this.model.fetchIntervals(range.start, range.end);

      	this.preview_xhr.success(_.bind(function() {
			this.remove_preview_intervals_display();
			this.handling_boxes = this.options.calendar_view.display_intervals(
				this.model.intervals_collection.models,
				function(box) {box.addClass('domain-highlight')}
				);
		}, this)
		);
	},

	save: function() {
		var domain_template = {};
		domain_template.name = this.$el.find('[name=domain_name]').val();

		if (domain_template.name == '')
			return false;

		domain_template.domain_stack_attributes = this.model.get('domain_stack');

		$.ajax({
      		url:'/domain_templates',
      		type:'post',
      		dataType:'json',
      		data: JSON.stringify({'domain_template': domain_template}),
      		contentType: "application/json; charset=utf-8",
      		success: _.bind(function(intervals) {
      			alert('saving ok');
      			this.remove();
      		}, this),
      		error: function() {
      			alert('saving error');
      		}
      	});

	},

	remove: function() {
		// remove currently displayed intervals
		this.remove_preview_intervals_display();

		PanelBase.prototype.remove.apply(this);
	},

});

});