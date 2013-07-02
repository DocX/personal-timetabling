// (c) 2013 Lukas Dolezal
"use strict";

/*

Nested domain form. Enables traversing and editing of nested time domains

*/

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    DomainTemplate = require('models/domain_template'),
    DomainTemplatesCollection = require('models/domain_templates_collection'),
    DomainStackFormPart = require('components/domain_stack_form_part'),
    DomainBoundlessFormPart = require('components/domain_boundless_form_part'),
    DomainBoundedFormPart = require('components/domain_bounded_form_part'),
    DomainDatabaseFormPart = require('components/domain_database_form_part');


return Backbone.View.extend({

	template: 
		"<div class=''>" +
			"<div class='well well-small'>" +
				"<div class='domains-stack' style='margin-bottom:0.5em'><strong>" +
					"<span data-role='stack-back'>Nested domain item.</span>" +
					"<span data-role='root-indicator'>Root domain</span>" +
				"</strong></div>" +
				"<div class='domain-form-container'></div>" +
				"<a class='btn' data-role='stack-back'>Save and back</a>" +
			"</div>" +
		"</div>",

	initialize: function() {
		this.$el.append(this.template);

		// stack of traversing
		this.domains_stack = [];
		this.domain_form = null;

		this.$form_container = this.$el.find('.domain-form-container');
		this.$stack_container = this.$el.find('.domains-stack');
		this.$back_btn = this.$el.find('a[data-role=stack-back]');
		this.$root_indicator = this.$el.find('[data-role=root-indicator]');

		this.$back_btn.click(_.bind(this.back, this));

		this.load_domain_templates();

		this.open_domain(this.model);
	},

	load_domain_templates: function() {
		this.domains_collection = new DomainTemplatesCollection();
		this.domains_collection.fetch();
	},

	open_domain: function(domain) {
		this.domains_stack.push(domain);
		this.open_form(domain);
	},

	open_form: function(domain) {
		if (this.domain_form != null) {
			//this.domain_form.update_to_model();
			this.domain_form.remove();
		}

		var domain_form_div = $('<div />');
		this.$form_container.append(domain_form_div);

		this.domain_form = this.create_form(domain, domain_form_div);
		this.listenTo(this.domain_form, 'opennested', this.open_domain);
		this.listenTo(this.domain_form, 'change', this.trigger_change);

		this.show_title();
	},

	create_form: function(domain, el) {
		switch(domain.type) {
			case 'stack': 
				return new DomainStackFormPart({el: el, model: domain});
			case 'boundless':
				return new DomainBoundlessFormPart({el: el, model: domain});
			case 'bounded':
				return new DomainBoundedFormPart({el:el, model: domain});
			case 'domain_template':
				return new DomainDatabaseFormPart({el: el, model: domain, db_domains: this.domains_collection});
		}
	}, 

	show_title: function() {
		if (this.domains_stack.length <= 1) {
			this.$el.find('[data-role=stack-back]').hide();
			this.$root_indicator.show();
		} else {
			this.$el.find('[data-role=stack-back]').show();
			this.$root_indicator.hide();
		}
	},

	back: function() {
		if (this.domains_stack.length <= 1) {
			return false;
		}
		
		this.domain_form.update_to_model();
		this.domains_stack.splice(-1,1);
		this.open_form(this.domains_stack[this.domains_stack.length-1]);
	},

	trigger_change: function() {
		this.trigger('change');
	}

})
});