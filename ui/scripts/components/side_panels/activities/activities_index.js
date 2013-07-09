// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    PanelBase = require('components/side_panels/panel_base'),
    ActivityCollection = require('models/activity_collection'),
    NewActivityForm = require('components/side_panels/activities/activities_new');
    
return PanelBase.extend({

	template: 
		"<div id='activities-box'>" +
			"<p><strong>Activities</strong> <a class='' style='float:right' data-role='cancel_btn'>Cancel</a></p>" +

			"<div>" +
				"<a href='#' class='btn btn-primary' data-role='new-activity'>Create new activity</a>" +
			"</div>" +

			"<div>" +
				"<p>Listing activities with events in visible range:</p>" +
				"<p><span data-role='range-from'></span> - <span data-role='range-to'></span></p>" +
				"<ul data-role='activities'>" +
					"<li>Loading...</li>" +
				"</ul>" +
			"</div>" +
		"</div>" +
		"<div id='subpanel-view'>"+
		"</div>",

	activity_item_template: _.template(
		"<li>" +
			"<a href='#' data-role='edit-activity' data-id='<%= id %>'><%= name %></a> " +
			"[<a href='#' data-role='remove-activity' data-id='<%= id %>' title='Remove'>X</a>]" +
		"</li>"
		),

	events: {
		'click a[data-role=new-activity]': 'create_new',
		'click a[data-role=cancel_btn]': 'remove',
		'click a[data-role=edit-activity]': 'edit_item',
		'click a[data-role=remove-activity]': 'remove_item',
	},

	initialize: function() {
		this.$el.append(this.template);

		this.$index_view = this.$el.find('#activities-box');
		this.$panel_view = this.$el.find('#subpanel-view');
		this.$list = this.$el.find('[data-role=activities]');

		this.activity_collection = new ActivityCollection();
		this.listenTo(this.activity_collection, 'add destroy sync', this.load_from_collection);
		this.listenTo(this.options.calendar_view, 'columns_updated', this.update_range)
		this.update_range();

		this.options.activities_view.clear_selection();

	},

	update_range: function() {
		var range = this.options.calendar_view.showing_dates();
		this.activity_collection.fetchInRange(range.start, range.end);
		this.$el.find('[data-role=range-from]').text(range.start.format('LL'));
		this.$el.find('[data-role=range-to]').text(range.end.format('LL'));
	},

	remove: function() {
		PanelBase.prototype.remove.apply(this);
	},

	load_from_collection: function() {
		this.$list.empty();
		this.activity_collection.forEach(function(d) {
			this.$list.append(this.activity_item_template(d.attributes));
		}, this);
	},

	create_new: function() {
		this.$index_view.hide();

		var panel_el = $('<div/>');
		this.$panel_view.append(panel_el);
		this.subpanel = new NewActivityForm({
			el:panel_el, 
			calendar_view: this.options.calendar_view,
			activities_view: this.options.activities_view
		});
		this.activity_collection.add(this.subpanel.model);
		this.listenTo(this.subpanel, 'removed', this.subpanel_removed);

		this.$panel_view.show();
	},

	remove_item: function(b) {
		var item_id = $(b.target).data('id');

		this.activity_collection.get(item_id).destroy({wait: true})	
		.success(_.bind(function() {
			this.options.activities_view.reload_activities(true);
		}, this))
		.error(function(xhr) {
			var d = JSON.parse(xhr.responseText);
			alert('error destroying domain template');			
		});
	},

	edit_item: function(b) {
		var domain_id = $(b.target).data('id');
		this.domains_collection.get(domain_id).fetch()
		.success(_.bind(this.show_edit,this, domain_id));

	},

	show_edit: function(domain_id) {

		this.$index_view.hide();

		var panel_el = $('<div/>');
		this.$panel_view.append(panel_el);
		this.subpanel = new DomainTemplateEditor({
			el:panel_el,
			calendar_view: this.options.calendar_view,
			model: this.domains_collection.get(domain_id)
		});
		this.listenTo(this.subpanel, 'removed', this.subpanel_removed);

		this.$panel_view.show();
	},

	subpanel_removed: function() {
		this.$panel_view.hide();
		this.$index_view.show();
	}

});

});