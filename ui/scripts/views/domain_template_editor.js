// (c) 2013 Lukas Dolezal
"use strict";

PersonalTimetabling.Views.DomainTemplateEditor = Backbone.View.extend({

	template: 
		"<div>" +
			"<div>" +
				"<label>Name</label>" +
				"<input name='domain_name' type='text'/>" +
			"</div>" +
			"<div>" +
				"<div id='addaction'>" +
					"<div>" +
						"<label class='checkbox inline btn'><input type='radio' name='add_action_type' value='add' style='display:none'> Add</label>" +
						"<label class='checkbox inline btn'><input type='radio' name='add_action_type' value='remove'  style='display:none'> Remove</label>" +
						"<label class='checkbox inline btn'><input type='radio' name='add_action_type' value='mask'  style='display:none'> Mask</label>" +
					"</div>" +
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
						"<a href='#' class='btn btn-primary' data-role='add_action_btn'>Add</a> <a href='#' class='btn' data-role='add_action_cancel_btn'>Cancel</a>" +
					"</div>" +					
				"</div>" +
				"<div id='domain_box'>" +
					"<ul class='ui-menu'>" +
						"<li class='first fixed'><a href class='btn' id='addaction_btn'>Add action</a></li>" +
						"<li class='fixed'><span class='btn disabled' style='display:block;left:0;right:0;'>Empty domain at the bottom</span></li>" +
					"</ul>" + 
					"<div>" +
						"<a href='#' class='btn btn-primary' data-role='save-domain'>Save domain template</a>" +
					"</div>" +
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
		'<li class="sortable-item btn">' +
			'<div class="btn-group">' +
				'<a class="btn handle"><i class="icon-resize-vertical"></i></a>' +
				'<a class="btn" data-domain-item-btn="action"><i class="icon-minus"></i></a>' +
				'<a class="btn" data-domain-item-btn="edit"><i class="icon-pencil"></i></a>' +
				'<a class="btn" data-domain-item-btn="remove"><i class="icon-trash"></i></a>' +
			'</div>' +
			'<span class="btn" data-domain-item="title"></span>' +
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
		'click [data-role=add_action_close_btn]': 'closeAddAction',
		'click a[data-domain-item-btn=remove]': 'removeStackItem',
		'click a[data-domain-item-btn=action]': 'changeStackItemAction',
		'click a[data-role=save-domain]': 'save',
	},

	initialize: function() {

		this.$el.append(this.template);

		this.$el.find('#boundless_duration_unit').append($(this.duration_unit_template).attr('name', 'boundless_duration_unit'));
		this.$el.find('#boundless_period_unit').append($(this.duration_unit_template).attr('name', 'boundless_period_unit'));

		this.$el.find('input.datetime').datetimepicker();

		this.$addaction_box = this.$el.find('#addaction');
		this.$addaction_box.hide();

		this.$addaction_boundless_box = this.$addaction_box.find('#boundless_definition');
		this.$addaction_boundless_box.hide();

		this.$addaction_bounded_box = this.$addaction_box.find('#bounded_definition');
		this.$addaction_bounded_box.hide();

		this.$domain_box = this.$el.find("#domain_box");

		// make list sortable
		this.$stack_list = this.$domain_box.find('ul');
		this.$stack_list.sortable({
			placeholder: "ui-state-highlight",
			forcePlaceholderSize: true,
			handle: ".handle",
			axis: 'y',
			items: '> li.sortable-item',
			change: _.bind(		this.refresh_preview, this)
		}).disableSelection();

		this.listenTo(this.options.calendar_view, 'columns_updated', this.refresh_preview);
		this.load_domain_templates();
	},

	load_domain_templates: function() {
		this.domains_collection = new PersonalTimetabling.Models.DomainTemplatesCollection();
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

	remove_preview_intervals_display: function() {
		if (!this.handling_boxes)
			return;

		for (var i = this.handling_boxes.length - 1; i >= 0; i--) {
			for (var y = this.handling_boxes[i].length - 1; y >= 0; y--) {
				this.handling_boxes[i][y].remove();
			};
		};		
	},

	domain_stack_json: function() {
		var items_data = this.$stack_list.find('li.sortable-item').map(function() { return $(this).data('action') });
     
      	var items = $.makeArray(items_data);

      	var domain_stack = {};
      	for (var i = 0; i< items.length; i++) {
        	domain_stack[i] = items[i];
      	}
      	return domain_stack;
	},

	refresh_preview: function() {

      	// gets display date range
      	var range = this.options.calendar_view.showing_dates();

      	var items_data = {
      		'domain_stack': this.domain_stack_json(),
      		'from': range.start,
      		'to': range.end
      	};

      	var panel_view = this;

      	$.ajax({
      		url:'/domain_templates/preview',
      		type:'post',
      		dataType:'json',
      		data: JSON.stringify(items_data),
      		contentType: "application/json; charset=utf-8",
      		success: function(intervals) {
      			panel_view.remove_preview_intervals_display();
      			panel_view.handling_boxes = [];

				_.forEach(intervals, function(interval) {
					var boxes = panel_view.options.calendar_view.display_interval(
						moment.utc(interval.start),
						moment.utc(interval.end),
						function(boxdom) {boxdom.addClass('domain-highlight');}
						);	
					panel_view.handling_boxes.push(boxes);
				});
      		},
      		error: function() {
      			
      		}
      	});

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
		this.$addaction_box.find('input[type=radio]').attr('checked', false);
		this.$addaction_bounded_box.hide();
		this.$addaction_boundless_box.hide();

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

	addAction: function() {
		var action = {
			action: this.selectedAddActionType(),
			type: this.selectedAddIntervalType(),
		};

		// validate
		if (!action.action)
			return false;
		if (!action.type)
			return false;

		var title = '';

		if (action.type == 'boundless') {
			$.extend(action, {
				// discard timezone part
				from: moment.utc(moment(this.$el.find('input[name=boundless_from]').datetimepicker('getDate')).format("YYYY-MM-DDTHH:mm:ss")),
				duration: {
					duration: this.$el.find('input[name=boundless_duration]').val(),
					unit: this.$el.find('[name=boundless_duration_unit]').val(),
				},
				period: {
					duration: this.$el.find('input[name=boundless_period]').val(),
					unit: this.$el.find('[name=boundless_period_unit]').val(),
				}
			});
			title = _.template('Repeat <%= duration.duration %> <%= duration.unit %> each <%= period.duration %> <%= period.unit %> referenced at <%= from %>', action);
		} else if(action.type == 'bounded') {
			$.extend(action, {
				from: moment.utc(moment(this.$el.find('input[name=bounded_from]').datetimepicker('getDate')).format("YYYY-MM-DDTHH:mm:ss")),
				to: moment.utc(moment(this.$el.find('input[name=bounded_to]').datetimepicker('getDate')).format("YYYY-MM-DDTHH:mm:ss")),
			});
			title = _.template('<%= from %> - <%= to %>', action);
		} else {
			// existing template
			action.domain_template_id = action.type;
			action.type = 'database';
			title = this.$el.find('select[name=add_interval_type] option:selected').text();
		}


		var sortable_list_item = $(this.domain_stack_item_template);
		sortable_list_item.data('action', action);
		sortable_list_item.find('[data-domain-item-btn=action] i').attr('class', this.action_icons[action.action]);
      	sortable_list_item.find('[data-domain-item=title]').text(title);

      	this.$stack_list.find('.first').after(sortable_list_item);

      	//hide adding and show domain
      	this.$addaction_box.hide();
      	this.$domain_box.show();

      	this.refresh_preview();

      	return false;
	},

	removeStackItem: function(e) {
		$(e.target).closest('li').remove();
		this.refresh_preview();
	},

	changeStackItemAction: function(e) {
		var itemdata = $(e.target).closest('li').data('action');
      
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

		$(e.target).find('i').removeClass().addClass(this.action_icons[itemdata.action]);	
		this.refresh_preview();		
	},

	save: function() {
		var domain_template = {};
		domain_template.name = this.$el.find('[name=domain_name]').val();

		if (domain_template.name == '')
			return false;

		domain_template.domain_stack_attributes = this.domain_stack_json();

		$.ajax({
      		url:'/domain_templates',
      		type:'post',
      		dataType:'json',
      		data: JSON.stringify({'domain_template': domain_template}),
      		contentType: "application/json; charset=utf-8",
      		success: function(intervals) {
      			alert('saving ok');
      		},
      		error: function() {
      			alert('saving error');
      		}
      	});

	},

});