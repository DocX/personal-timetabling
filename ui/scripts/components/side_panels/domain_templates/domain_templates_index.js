// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    PanelBase = require('components/side_panels/panel_base'),
    DomainTemplatesCollection = require('models/domain_templates_collection'),
    DomainTemplateEditor = require('components/side_panels/domain_templates/domain_template_editor');
    
return PanelBase.extend({

	template: 
		"<div id='domain_box'>" +
			"<p><strong>Domain templates</strong> <a class='' style='float:right' data-role='cancel_btn'>Cancel</a></p>" +

			"<div>" +
				"<a href='#' class='btn btn-primary' data-role='new-domain'>Create new domain</a>" +
			"</div>" +

			"<div>" +
				"<p>Listing domain templates:</p>" +
				"<ul data-role='domains'>" +
					"<li>Loading...</li>" +
				"</ul>" +
			"</div>" +
		"</div>" +
		"<div id='subpanel-view'>"+
		"</div>",

	domain_item_template: _.template(
		"<li>" +
			"<a href='#' data-role='edit-domain' data-id='<%= id %>'><%= name %></a> " +
			"<a href='#' data-role='remove-domain' data-id='<%= id %>'>Remove</a>" +
		"</li>"
		),

	events: {
		'click a[data-role=new-domain]': 'create_new',
		'click a[data-role=cancel_btn]': 'remove',
		'click a[data-role=edit-domain]': 'edit_domain',
		'click a[data-role=remove-domain]': 'remove_domain',
	},

	initialize: function() {

		this.$el.append(this.template);

		this.$index_view = this.$el.find('#domain_box');
		this.$panel_view = this.$el.find('#subpanel-view');
		this.$domains_list = this.$el.find('[data-role=domains]');

		this.domains_collection = new DomainTemplatesCollection();
		this.listenTo(this.domains_collection, 'add destroy sync', this.load_from_domains);
		this.domains_collection.fetch();
	},

	remove: function() {
		PanelBase.prototype.remove.apply(this);
	},

	load_from_domains: function() {
		this.$domains_list.empty();
		this.domains_collection.forEach(function(d) {
			this.$domains_list.append(this.domain_item_template(d.attributes));
		}, this);
	},

	create_new: function() {
		this.$index_view.hide();

		var panel_el = $('<div/>');
		this.$panel_view.append(panel_el);
		this.subpanel = new DomainTemplateEditor({el:panel_el, calendar_view: this.options.calendar_view});
		this.domains_collection.add(this.subpanel.model);
		this.listenTo(this.subpanel, 'removed', this.subpanel_removed);

		this.$panel_view.show();
	},

	remove_domain: function(b) {
		var domain_id = $(b.target).data('id');

		this.domains_collection.get(domain_id).destroy({wait: true})	
		.error(function(xhr) {
			var d = JSON.parse(xhr.responseText);
			if (d.error == 'is_referenced') {
				alert('Domain template is referenced by other domain templates. ');
			} else {
				alert('error destroying domain template');
			}
			
		});
	},

	edit_domain: function(b) {
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