// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    Activity = require('models/activity'),
    PanelBase = require('components/side_panels/panel_base'),
    DomainTemplatesCollection = require('models/domain_templates_collection'),
    FixedActivityForm = require('components/side_panels/activities/fixed_activity_form'),
    FloatingActivityForm = require('components/side_panels/activities/floating_activity_form');
    
return PanelBase.extend({

	template: 
		"<div>" +
			"<p><strong>New activity</strong> <a href='#' class='' data-role='activity_cancel_btn' style='float:right'>Cancel</a></p>" +

			"<label>Activity name</label>" +
			"<input type='text' name='activity_name' placeholder='Lunch, fitness, ...' class='fill-width' />" +

			"<p class='activity_type_select'>" +
				"<a href='#' class='btn fill-width' data-set-type='fixed'>I know exactly when</a>" +
				"<a href='#' class='btn fill-width' data-set-type='floating'>I know how long</a>" +
			"</p>" +

			"<div class='activity_form'></div>" +

			"<p class='actions'>" +
				"<a data-role='activity_create' class='btn btn-primary fill-width'>Create activity</a>" +
			"</p>" +

		"</div>",


	events: {
		'click a[data-role=activity_cancel_btn]': 'remove',
		'click a[data-set-type=fixed]': 'set_type_fixed',
		'click a[data-set-type=floating]': 'set_type_floating',
		'click a[data-role=activity_create]': 'save',
		'change input[name=activity_name]': 'change_name',
	},

	initialize: function() {
		this.$el.append(this.template);
		this.$form_box = this.$el.find('.activity_form');
		this.$actions = this.$el.find('.actions');
		this.$activity_name_input = this.$el.find('input[name=activity_name]');
		this.$activity_type_select = this.$el.find('.activity_type_select');

		this.$actions.hide();
	},

	clear_selected_form: function() {
		if (this.activity_form) {
			this.activity_form.remove();
			this.$form_box.empty();
		}
		if(this.activity_view_handle) {
			this.activity_view_handle.remove();
		}
	},

	set_form: function(form_class){
		this.clear_selected_form();

		this.activity_form = new form_class({
			el: this.$form_box,
			activities_view: this.options.activities_view
		});

		this.set_activity_view_handle(this.activity_form.activity);
		this.change_name();

		this.$activity_type_select.hide();

		this.$actions.show();

	},

	set_type_fixed: function(){
		this.set_form(FixedActivityForm);
	},

	set_type_floating: function(){
		this.set_form(FloatingActivityForm);
	},

	set_activity_view_handle: function(activity) {
		if(this.activity_view_handle) {
			this.activity_view_handle.remove();
		}
		this.activity_view_handle = this.options.activities_view.display_activity(activity.activity, 'new-creating');
	},

	change_name: function() {
		if (this.activity_form) {
			this.activity_form.activity.set('name', this.$activity_name_input.val());
		}
	},

	save: function() {
		
		var ap = this.activity_form.activity.get_activity_prototype();
		ap.save()
		.success(_.bind(function() {
			this.options.activities_view.reload_activities(); 
			this.trigger('new:activity', ap);
			this.remove()
		}, this))
		.error(function() {alert('Error')});
	},


	remove: function() {

		this.clear_selected_form();

		PanelBase.prototype.remove.apply(this, arguments);
	}

});

});