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
    DomainTemplatesCollection = require('models/domain_templates_collection');
    

return Backbone.View.extend({

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
			"<div class='btn-group'>" +
				"<label class='btn'><input type='radio' name='add_action_type' value='add' style='display:none'> Add</label>" +
				"<label class='btn'><input type='radio' name='add_action_type' value='remove'  style='display:none'> Remove</label>" +
				"<label class='btn'><input type='radio' name='add_action_type' value='mask'  style='display:none'> Mask</label>" +
			"</div>" +
			"<label>Add</label>" +
			"<div>" +
				"<a class='btn' data-role='new_create' data-type='stack'>Nested stack</a>" +
				"<a class='btn' data-role='new_create' data-type='bounded'>Bounded interval</a>" + 
				"<a class='btn' data-role='new_create' data-type='boundless'>Boundless interval</a>" +
				"<a class='btn' data-role='new_create' data-type='domain_template'>Saved domian template</a>" +
			"</div>" +			
		"</div>",

	
	domain_stack_item_template: 
		'<li class="sortable-item btn disabled" style="display:block;">' +
			'<div class="btn-group">' +
				'<a class="btn handle"><i class="icon-resize-vertical"></i></a>' +
				'<a class="btn" data-domain-item-btn="action"><i class="icon-minus"></i></a>' +
				'<a class="btn" data-domain-item-btn="edit"><i class="icon-pencil"></i></a>' +
				'<a class="btn" data-domain-item-btn="remove"><i class="icon-trash"></i></a>' +
			'</div>' +
			'<p class="" data-domain-item="title" style="margin:0; margin-top:0.5em"></p>' +
		'</li>',

	action_icons: {
      'add': 'icon-plus',
      'remove': 'icon-minus',
      'mask': 'icon-filter'
    },

    events: {
		'click #addaction_btn': "openAdd",
		'change input[name=add_action_type]': 'changeActionType',
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
	      	sortable_list_item.find('[data-domain-item=title]').text(this.action_label(action));	

			this.$stack_list.find('.first').after(sortable_list_item);
		};
		
	},

	update_to_model: function() {
		this.model.data.actions = this.domain_stack_json(); 
		this.trigger('change');
	},

	changeActionType: function() {
		var inputs = this.$el.find('[name=add_action_type]');
		inputs.not(':checked').closest('.btn').removeClass('active');
		inputs.filter(':checked').closest('.btn').addClass('active');
	},

	openAdd: function() {
		this.$domain_box.hide();
		this.$addaction_box.show();

		// reset all inputs
		this.$addaction_box.find('input[type=radio]').prop('checked', false);
		this.changeActionType();

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


		var sortable_list_item = $(this.domain_stack_item_template);
		sortable_list_item.data('action', action);
		sortable_list_item.find('[data-domain-item-btn=action] i').attr('class', this.action_icons[action.action]);

      	this.$stack_list.find('.first').after(sortable_list_item);

      	//hide adding and show domain
      	this.$addaction_box.hide();
      	this.$domain_box.show();

      	this.update_to_model();

      	// signal to open subdomain form
      	this.trigger('opennested', action.domain);

      	return false;
	},

	action_label: function(action) {
		var title = '';
		if (action.domain.type == 'boundless') {
			title = _.template('Repeat <%= duration.duration %> <%= duration.unit %> each <%= period.duration %> <%= period.unit %> referenced at <%= from %>', action.domain.data);
		} else if(action.domain.type == 'bounded') {
			title = _.template('<%= from %> - <%= to %>', action.domain.data);
		} else if(action.domain.type == 'stack') {
			title = 'Nested stack';
		} else {
			title = _.template('Database id:<%= id %>', action.domain.data);
		}

		return title;
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

});

});
