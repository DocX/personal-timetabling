// (c) 2013 Lukas Dolezal
"use strict";

/*

Domain stack form - displayes and edits domain stack with one level

*/

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    DomainTemplate = require('models/domain_template'),
    DomainTemplatesCollection = require('models/domain_templates_collection'),
    DomainBoundedFormPart = require('components/domain_template_forms/domain_bounded_form_part'),
    DomainBoundlessFormPart = require('components/domain_template_forms/domain_boundless_form_part');
    

var DomainStackFormPart = Backbone.View.extend({

	template:
		"<div class='domain-display'>" +
			"<ul class='ui-menu'>" +
				"<li class='first fixed' style='margin-bottom:5px;'><a href='#' class='btn' id='addaction_btn' style='display:block; left:0;right:0;'>Add action</a></li>" +
				"<li class='fixed'><span class='btn disabled' style='display:block;left:0;right:0;'>Empty domain at the bottom</span></li>" +
			"</ul>" + 
		"</div>" +

		"<div class='item-screen'>" +
			"<p><strong>New action</strong> <a href='#' class='' data-role='add_action_cancel_btn'>Cancel</a></p>" +
			"<label>Operation</label>" +
			"<div class='btn-group form-inline'>" +
				"<label class=''><input type='radio' name='add_action_type' value='add' > <i class='icon-plus'></i> Union</label> " +
				"<label class=''><input type='radio' name='add_action_type' value='remove' > <i class='icon-minus'></i> Remove</label> " +
				"<label class=''><input type='radio' name='add_action_type' value='mask'  > <i class='icon-filter'></i> Mask</label>" +
			"</div>" +
			"<label>Definition</label>" +
			"<ul class='nav-pills nav nav-stacked'>" +
				"<li><a class='' data-role='new_create' data-type='stack'>Nested stack</a></li>" +
				"<li><a class='' data-role='new_create' data-type='bounded'>Single interval</a></li>" + 
				"<li><a class='' data-role='new_create' data-type='boundless'>Repeating interval</a></li>" +
				"<li><a class='' data-role='new_create' data-type='domain_template'>Domian template</a></li>" +
			"</ul>" +			
		"</div>",

	
	domain_stack_item_template: 
		'<li class="sortable-item" style="display:block; position:relative;">' +
			'<div class=" btn disabled" style="display:block">' +
				'<div class="btn-group" style="text-align:left">' +
					'<a class="btn handle"><i class="icon-resize-vertical"></i></a>' +
					'<a class="btn" data-domain-item-btn="edit"><i class="icon-pencil"></i></a>' +
					'<a class="btn" data-domain-item-btn="remove"><i class="icon-trash"></i></a>' +
				'</div>' +
				'<p class="" data-domain-item="title" style="margin:0; margin-top:0.5em"></p>' +
			"</div>" +
			'<a class="btn" data-domain-item-btn="action" style="display:block"><i class="icon-minus"></i></a>' +
		'</li>',

	action_icons: {
      'add': 'icon-plus',
      'remove': 'icon-minus',
      'mask': 'icon-filter'
    },

    events: {
		'click #addaction_btn': "openAdd",
		'click [data-role=add_action_cancel_btn]': 'closeAddAction',
		'click a[data-domain-item-btn=remove]': 'removeStackItem',	
		'click a[data-domain-item-btn=edit]': 'editStackItem',
		'click a[data-domain-item-btn=action]': 'changeStackItemAction',
	},

	initialize: function() {

		this.$el.append(this.template);

		this.$addaction_box = this.$el.find('.item-screen');
		this.$addaction_box.hide();

		this.$domain_box = this.$el.find(".domain-display");

		// make list sortable
		this.$stack_list = this.$domain_box.find('ul');
		this.$stack_list.sortable({
			placeholder: "ui-state-highlight",
			forcePlaceholderSize: true,
			handle: ".handle",
			axis: 'y',
			items: '> li.sortable-item',
			change: _.bind(this.update_to_model, this)
		}).disableSelection();

		this.$el.find('a[data-role=new_create]').click(_.partial(function(that) { return that.create_new(this); }, this));

		// initialize model
		this.model.data = $.extend({
			actions: []
		}, this.model.data);

		this.load_from_model();
	},

	domain_stack_json: function() {
		var items_data = this.$stack_list.find('li.sortable-item').map(function() { return $(this).data('action') });
     
      	var items = $.makeArray(items_data);

      	var domain_stack = [];
      	for (var i = 0; i< items.length; i++) {
        	domain_stack.push(items[i]);
      	}
      	return domain_stack;
	},

	load_from_model: function() {
		// clear list
		this.$stack_list.find('li.sortable-item').remove();

		// for each action add item
		var actions = this.model.data.actions;
		for (var i = actions.length-1; i >= 0; i--) {
			var sortable_list_item = $(this.domain_stack_item_template);
			var action = actions[i];
			sortable_list_item.data('action', action);
			sortable_list_item.find('[data-domain-item-btn=action] i').attr('class', this.action_icons[action.action]);
	      	sortable_list_item.find('[data-domain-item=title]').html(this.action_label(action));	

			this.$stack_list.find('.first').after(sortable_list_item);
		};
		
	},

	update_to_model: function() {
		this.model.data.actions = this.domain_stack_json(); 
		this.trigger('change');
	},

	openAdd: function() {
		this.$domain_box.hide();
		this.$addaction_box.show();

		// reset all inputs
		this.$addaction_box.find('input[type=radio]').prop('checked', false);
		this.$addaction_box.find('input[value=add]').prop('checked', true);
	
		return false;
	},

	selectedAddActionType: function() {
		return this.$el.find('input:radio[name=add_action_type]:checked').val();
	},

	closeAddAction: function() {
		this.$addaction_box.hide();
      	this.$domain_box.show();
      	return true;
	},

	create_new: function(button) {
		var action = {
			action: this.selectedAddActionType(),
			domain: {
				type: $(button).data('type'),
				data: {},
			}
		};

		// validate
		if (!action.action)
			return false;
		if (!action.domain.type)
			return false;



      	//hide adding and show domain
      	this.$addaction_box.hide();
      	this.$domain_box.show();


      	// signal to open subdomain form
      	this.trigger('opennested', action.domain, action);

      	return false;
	},

	from_nested_save: function(data) {
		this.add_to_stack(data);
		this.load_from_model();
	},

	add_to_stack: function(action){
		var sortable_list_item = $(this.domain_stack_item_template);
		sortable_list_item.data('action', action);
		sortable_list_item.find('[data-domain-item-btn=action] i').attr('class', this.action_icons[action.action]);

      	this.$stack_list.find('.first').after(sortable_list_item);	

      	this.update_to_model();	
	},

	action_label: function(action) {
		return DomainStackFormPart.other_domain_label(
			action.domain, this.options.db_domains, true);
	},

	removeStackItem: function(e) {
		$(e.target).closest('li').remove();
		this.update_to_model();
	},

	changeStackItemAction: function(e) {
		var listitem =  $(e.target).closest('li');
		var itemdata = listitem.data('action');
      
		switch(itemdata.action)
		{
			case 'add':
				itemdata.action = 'remove';
				break;
			case 'remove':
				itemdata.action = 'mask';
				break;
			case 'mask':
				itemdata.action = 'add';
				break;
		}

		listitem.find('a[data-domain-item-btn=action] i').removeClass().addClass(this.action_icons[itemdata.action]);	
		this.update_to_model();
	},

	editStackItem: function(e) {
		var editing_item = $(e.target).closest('li');

		// signal to open subdomain form
      	this.trigger('opennested', editing_item.data('action').domain);
	},

}, {

	domain_label: function(domain, db_domains) {
		var strings = [];
		var prev = '';
		for (var i = domain.data.actions.length - 1; i >= 0; i--) {
			var a = domain.data.actions[i];
			var domain_label = DomainStackFormPart.other_domain_label(a.domain,db_domains, false);

			strings.push(prev + ' ' + domain_label);
			prev = {add:'+', remove:'-', mask:'>'}[a.action];
		};

		return strings.join(' ');
	},

	other_domain_label: function(domain, db_domains, nest) {
		var domain_label;
		switch(domain.type) {
			case 'stack':
				if (nest) {
					domain_label = DomainStackFormPart.domain_label(domain,db_domains);
				} else {
					domain_label = '*nested stack*';
				}
				
				break;
			case 'bounded':
				domain_label = DomainBoundedFormPart.domain_label(domain);
				break;
			case 'boundless':
				domain_label = DomainBoundlessFormPart.domain_label(domain);
				break;
			case 'database':
				domain_label = db_domains.get(domain.data.id).get('name');
				break;
		}

		return domain_label;
	}
});

return DomainStackFormPart;

});
