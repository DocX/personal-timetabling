// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    PanelBase = require('components/side_panels/panel_base'),
    DomainTemplate = require('models/domain_template'),
    DomainTemplatesCollection = require('models/domain_templates_collection'),
    NestedDomainForm = require('components/domain_template_forms/nested_domain_form');
    
return PanelBase.extend({

	template: 
		"<div id='domain_box'>" +
			"<p><strong>New domain template</strong> <a class='' style='float:right' data-role='cancel_btn'>Cancel</a></p>" +

			"<label>Name</label>" +
			"<input name='domain_name' type='text' class='fill-width' />" +
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

		// initial stack domain
		if (this.model == null) {
			this.model = new DomainTemplate();
			this.model.set('domain_attributes', {type: 'stack', data: { actions: [] } });			
		} 

		this.$el.find('[name=domain_name]').val(this.model.get('name'));

		this.domain_form = new NestedDomainForm({
			el: this.$el.find('.domain_stack_form'),
			model: this.model.get('domain_attributes')
		});

		this.refresh_preview();

		this.listenTo(this.domain_form, 'change', this.refresh_preview);
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
		//this.model.set('data', this.domain_model.data);

		
		if (!this.model.get('domain_attributes'))
			return true;
		console.log("refresh_preview domain");

      	// gets display date range
      	var range = this.options.calendar_view.showing_dates();

		this.preview_xhr && this.preview_xhr.abort();
      	this.preview_xhr = this.model.fetchPreviewIntervals(range.start, range.end);
      	if (this.preview_xhr == false) {
      		return;
      	}

      	this.preview_xhr.success(_.bind(function() {
			this.remove_preview_intervals_display();
			this.handling_boxes = this.options.calendar_view.display_intervals(
				this.model.preview_intervals_collection.models,
				function(box) {box.addClass('domain-highlight')}
				);
		}, this)
		);
	},

	save: function() {	
		this.model.set('name', this.$el.find('[name=domain_name]').val());

		this.model.save()
		.success(_.bind(this.remove, this))
		.error(function() { alert('error saving domain')});
	},

	remove: function() {
		// remove currently displayed intervals
		this.remove_preview_intervals_display();

		PanelBase.prototype.remove.apply(this);
	},

});

});