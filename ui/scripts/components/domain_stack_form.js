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
		"<div class='well well-small'>" +
		"<div class='domain-display'>" +
			"<ul class='ui-menu'>" +
				"<li class='first fixed' style='margin-bottom:5px;'><a href class='btn' id='addaction_btn' style='display:block; left:0;right:0;'>Add action</a></li>" +
				"<li class='fixed'><span class='btn disabled' style='display:block;left:0;right:0;'>Empty domain at the bottom</span></li>" +
			"</ul>" + 
		"</div>" +
		"<div class='item-screen'>" +
			"<p><strong>Action</strong> <a href='#' class='' data-role='add_action_cancel_btn'>Back</a></p>" +
			"<label>Action</label>" +
			"<div class='btn-group'>" +
				"<label class='btn'><input type='radio' name='add_action_type' value='add' style='display:none'> Add</label>" +
				"<label class='btn'><input type='radio' name='add_action_type' value='remove'  style='display:none'> Remove</label>" +
				"<label class='btn'><input type='radio' name='add_action_type' value='mask'  style='display:none'> Mask</label>" +
			"</div>" +
			"<label>Interval definition</label>" +
			"<div>" +
				"<select name='add_interval_type'>"+
					"<option value=''>-- New interval --</option>" +
					"<option value='bounded'>Bounded interval</option>" +
					"<option value='boundless'>Repetitive</option>" +
					"<option value='' disabled='disabled'>-- Add domain --</option>" +
				"</select>"+
			"</div>" +
			"<div id='boundless_definition'>" +
				"<label>Repeat interval starting at</label>" +
				"<input type=text name='boundless_from' class='datetime' />" +
				"<label>Lasting</label>" +
				"<div class='input-append'>" +
					"<input type=number min='1' name='boundless_duration' class='span2'> <span id='boundless_duration_unit'/>" +
				"</div>" +
				"<div class='input-append'>" +
				"<label>Each</label>" +
					"<input type=number min='1' name='boundless_period' class='span2'> <span id='boundless_period_unit'/>" +
				"</div>" +
			"</div>" +
			"<div id='bounded_definition'>" +
				"<label>From</label>" +
				"<input type='text' name='bounded_from' class='datetime' />" +
				"<label>To</label>" +
				"<input type='text' name='bounded_to' class='datetime' />" +					
			"</div>" +
			"<div>" +
				"<a href='#' class='btn btn-primary' data-role='add_action_btn'>OK</a>" +
			"</div>" +					
		"</div>" +
		"</div>",

	duration_unit_template: 
		"<select class='input-mini'>" +
			"<option value='hour'>Hours</option>" +
			"<option value='day'>Days</option>" +
			"<option value='week'>Weeks</option>" +
			"<option value='month'>Months</option>" +
		"</select>", 

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
		'change select[name=add_interval_type]': 'changeAddType',
		'change input[name=add_action_type]': 'changeActionType',
		'click [data-role=add_action_btn]': 'addAction',
		'click [data-role=add_action_cancel_btn]': 'closeAddAction',
		'click a[data-domain-item-btn=remove]': 'removeStackItem',	
		'click a[data-domain-item-btn=edit]': 'editStackItem',
		'click a[data-domain-item-btn=action]': 'changeStackItemAction',
	},

	initialize: function() {

		this.$el.append(this.template);

		this.$el.find('#boundless_duration_unit').append($(this.duration_unit_template).attr('name', 'boundless_duration_unit'));
		this.$el.find('#boundless_period_unit').append($(this.duration_unit_template).attr('name', 'boundless_period_unit'));

		this.$el.find('input.datetime').datetimepicker();

		this.$addaction_box = this.$el.find('.item-screen');
		this.$addaction_box.hide();

		this.$addaction_boundless_box = this.$addaction_box.find('#boundless_definition');
		this.$addaction_boundless_box.hide();

		this.$addaction_bounded_box = this.$addaction_box.find('#bounded_definition');
		this.$addaction_bounded_box.hide();

		this.$domain_box = this.$el.find(".domain-display");

		// make list sortable
		this.$stack_list = this.$domain_box.find('ul');
		this.$stack_list.sortable({
			placeholder: "ui-state-highlight",
			forcePlaceholderSize: true,
			handle: ".handle",
			axis: 'y',
			items: '> li.sortable-item',
			change: _.bind(this.set_model_stack, this)
		}).disableSelection();

		this.load_domain_templates();

		this.model = new DomainTemplate();
	},

	load_domain_templates: function() {
		this.domains_collection = new DomainTemplatesCollection();
		this.$domain_selectbox = this.$el.find('select[name=add_interval_type]');

		this.domains_collection.fetch()
			.success(_.bind(function() {
				this.$domain_selectbox.append("<option value=''></option>");
				var $domain_selectbox = this.$domain_selectbox;

				this.domains_collection.forEach(function(domain) {
					$domain_selectbox.append($("<option/>").attr({
						value: domain.get('id')
					}).text(domain.get('name')))
				});

			}, this)
			);

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
		var actions = this.model.get('domain_stack');
		for (var i = actions.length-1; i >= 0; i--) {
			var sortable_list_item = $(this.domain_stack_item_template);
			var action = actions[i];
			sortable_list_item.data('action', action);
			sortable_list_item.find('[data-domain-item-btn=action] i').attr('class', this.action_icons[action.action]);
	      	sortable_list_item.find('[data-domain-item=title]').text(this.action_label(action));	

			this.$stack_list.find('.first').after(sortable_list_item);
		};
		
	},

	set_model_stack: function() {
		this.model.set('domain_stack', this.domain_stack_json()); 

		// somewhen set do not fire change, so ensure about it
		this.model.trigger('change');
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
		this.$addaction_box.find('input[type!=radio]').val(null);
		this.$addaction_box.find('select').val(null);
		this.$addaction_box.find('input[type=radio]').prop('checked', false);
		this.$addaction_bounded_box.hide();
		this.$addaction_boundless_box.hide();

		this.editing_item = null;

		this.changeActionType();

		return false;
	},

	selectedAddActionType: function() {
		return this.$el.find('input:radio[name=add_action_type]:checked').val();
	},

	selectedAddIntervalType: function() {
		return this.$el.find('select[name=add_interval_type]').val();
	},

	changeAddType: function() {
		var type = this.selectedAddIntervalType();

		if (type == 'boundless'){
			this.$addaction_bounded_box.hide();
			this.$addaction_boundless_box.show();
		} else if(type=='bounded') {
			this.$addaction_bounded_box.show();
			this.$addaction_boundless_box.hide();
		} else {
			// hide all
			this.$addaction_bounded_box.hide();
			this.$addaction_boundless_box.hide();
		}
	},

	closeAddAction: function() {
		this.$addaction_box.hide();
      	this.$domain_box.show();
      	return true;
	},

	addAction: function() {
		var action = {
			action: this.selectedAddActionType(),
			domain: {
				type: this.selectedAddIntervalType(),
				data: {},
			}
		};

		// validate
		if (!action.action)
			return false;
		if (!action.domain.type)
			return false;



		if (action.domain.type == 'boundless') {
			$.extend(action.domain.data, {
				// discard timezone part
				from: moment.asUtc(this.$el.find('input[name=boundless_from]').datetimepicker('getDate')).toJSON(),
				duration: {
					duration: this.$el.find('input[name=boundless_duration]').val(),
					unit: this.$el.find('[name=boundless_duration_unit]').val(),
				},
				period: {
					duration: this.$el.find('input[name=boundless_period]').val(),
					unit: this.$el.find('[name=boundless_period_unit]').val(),
				}
			});
		} else if(action.domain.type == 'bounded') {
			$.extend(action.domain.data, {
				from: moment.asUtc(this.$el.find('input[name=bounded_from]').datetimepicker('getDate')).toJSON(),
				to: moment.asUtc(this.$el.find('input[name=bounded_to]').datetimepicker('getDate')).toJSON(),
			});
		} else {
			// existing template
			action.domain.data.id = action.domain.type;
			action.domain.type = 'database';
		}

		var title = this.action_label(action);

		var sortable_list_item = this.editing_item ? this.editing_item : $(this.domain_stack_item_template);
		sortable_list_item.data('action', action);
		sortable_list_item.find('[data-domain-item-btn=action] i').attr('class', this.action_icons[action.action]);
      	sortable_list_item.find('[data-domain-item=title]').text(title);


      	this.editing_item || this.$stack_list.find('.first').after(sortable_list_item);
      	this.editing_item = null;

      	//hide adding and show domain
      	this.$addaction_box.hide();
      	this.$domain_box.show();

      	this.set_model_stack();

      	return false;
	},

	action_label: function(action) {
		var title = '';
		if (action.domain.type == 'boundless') {
			title = _.template('Repeat <%= duration.duration %> <%= duration.unit %> each <%= period.duration %> <%= period.unit %> referenced at <%= from %>', action.domain.data);
		} else if(action.domain.type == 'bounded') {
			title = _.template('<%= from %> - <%= to %>', action.domain.data);
		} else {
			var domain = this.$el.find('select[name=add_interval_type] option[id='+action.domain.data.id+']')
			title = domain.length > 0 && domain.text() || '';
		}

		return title;
	},

	removeStackItem: function(e) {
		$(e.target).closest('li').remove();
		this.set_model_stack();
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
		this.set_model_stack();
	},

	editStackItem: function(e) {
		

		this.openAdd();

		this.editing_item = $(e.target).closest('li');

		var action = this.editing_item.data('action');
		this.$el.find('select[name=add_interval_type]').val(action.domain.type);
		this.$el.find('input[name=add_action_type][value='+action.action+']').prop('checked', 'checked');
		if (action.type == 'boundless') {
			this.$el.find('input[name=boundless_from]').datetimepicker('setDate', moment(action.domain.data.from.replace(/Z$/,'')).toDate());
			this.$el.find('input[name=boundless_duration]').val(action.domain.data.duration.duration);
			this.$el.find('[name=boundless_duration_unit]').val(action.domain.data.duration.unit);
			this.$el.find('input[name=boundless_period]').val(action.domain.data.period.duration);
			this.$el.find('[name=boundless_period_unit]').val(action.domain.data.period.unit);
		} else if(action.type == 'bounded') {
			this.$el.find('input[name=bounded_from]').datetimepicker('setDate', moment(action.domain.data.from.replace(/Z$/,'')).toDate());
			this.$el.find('input[name=bounded_to]').datetimepicker('setDate', moment(action.domain.data.to.replace(/Z$/,'')).toDate());
		} else {
			this.$el.find('select[name=add_interval_type]').val(action.domain.data.id);
		}
		this.changeActionType();
		this.changeAddType();
	},

});

});
