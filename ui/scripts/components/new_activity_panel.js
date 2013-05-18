// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    PanelBase = require('components/panel_base'),
    DomainTemplatesCollection = require('models/domain_templates_collection');
    
return PanelBase.extend({

	template: 
		"<div>" +
			"<p><strong>New activity</strong> <a href='#' class='btn' data-role='activity_cancel_btn'>Cancel</a></p>" +

			"<label>Activity name</label>" +
			"<input type='text' name='activty_name' placeholder='Lunch, fitness, ...'/>" +
			"<div class='btn-group'>" +
				"<label class='btn'><input type='radio' name='activity_type' value='fixed' style='display:none'> Fixed</label>" +
				"<label class='btn'><input type='radio' name='activity_type' value='floating' style='display:none'> Floating</label>" +
			"</div>" +

			"<div id='activity_definition_fixed'>" +
				"<label>From</label>" +
				"<input type='text' name='fixed_from' class='datetime' />" +
				"<label>To</label>" +
				"<input type='text' name='fixed_to' class='datetime' />" +
			"</div>" +

			"<div id='activity_definition_floating'>" +
				"<label>Domain template</label>" +
				"<select name='floating_domain_template'></select>" +
				"<label>From</label>" +
				"<input type='text' name='floating_from' class='datetime' />" +
				"<label>size of window</label>" +
				"<div class='input-append'>" +
					"<input type=number min='1' name='floating_to_duration' class='span2'> <span id='floating_to_duration_unit'/>" +
				"</div>" +
				"<label>Length in minutes <span id='floating_duration'></span></label>" +
				"<div id='floating_length_slider'></div>" +
			"</div>" +

			"<div>" +
				"<label>Create activity</label>" +
				"<a data-role='activity_create' class='btn btn-primary'>Create activity</a>" +
			"</div>" +

		"</div>",

	duration_unit_template: 
		"<select class='input-mini'>" +
			"<option value='hour'>Hours</option>" +
			"<option value='day'>Days</option>" +
			"<option value='week'>Weeks</option>" +
			"<option value='month'>Months</option>" +
		"</select>", 

	events: {
		'click a[data-role=activity_cancel_btn]': 'remove',
		'change input[name=activity_type]': 'change_type_view',
		'click a[data-role=activity_create]': 'save',
	},

	initialize: function() {
		this.$el.append(this.template);
		this.$fixed_definition_box = this.$el.find('#activity_definition_fixed');
		this.$floating_definition_box = this.$el.find('#activity_definition_floating');

		// initialize datetimepickers
		this.$el.find('.datetime').datetimepicker();

		this.$el.find('#floating_to_duration_unit').append($(this.duration_unit_template).attr('name', 'floating_to_duration_unit'));

		// hide definition boxes
		this.$fixed_definition_box.hide();
		this.$floating_definition_box.hide();

		this.$floating_slider = this.$el.find('#floating_length_slider').slider({
			range:true,
			min: 15,
			max: 600,
			values: [60,120],
			step: 15,
			change: _.bind(this.set_slider_values, this),
			slide: _.bind(this.set_slider_values, this),
		});

		this.set_slider_values(null, {values:[60,120]});
		this.load_domain_templates();
	},

	set_slider_values: function(e,ui) {
		this.$el.find('#floating_duration').text(
			Math.floor(ui.values[0] / 60) + 'h' + (ui.values[0] % 60).pad(2) + 'm' +
			' - ' + 
			Math.floor(ui.values[1] / 60) + 'h' + (ui.values[1] % 60).pad(2) + 'm' );
	},

	load_domain_templates: function() {
		this.domains_collection = new DomainTemplatesCollection();
		this.$domain_selectbox = this.$el.find('select[name=floating_domain_template]');

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

	selected_activity_type: function() {
		return this.$el.find('[name=activity_type]:checked').val();
	},

	change_type_view: function(){
		var selected_type = this.selected_activity_type();

		if(this.selected_activity_type() == 'fixed') {
			this.$fixed_definition_box.show();
			this.$floating_definition_box.hide();
		} else if (this.selected_activity_type() == 'floating') {
			this.$fixed_definition_box.hide();
			this.$floating_definition_box.show();
		}
	},

	save: function() {
		var type = this.$el.find('input[name=activity_type]:checked').val();

		var activity_model;
		if (type == 'fixed') {
			activity_model = this.get_fixed_model();
		} else if (type == 'floating') {
			activity_model = this.get_floating_model();
		} else {
			return false;
		}

		activity_model.set({
			name: this.$el.find('[name=activty_name]').val()
		});

		activity_model.save()
		.success(_.bind(function() {this.trigger('added'); this.remove()}, this))
		.error(function() {alert('Error')});
	},

	get_fixed_model: function() {
		return PersonalTimetabling.Models.Activity.fixed({
			start: moment.asUtc(this.$el.find('[name=fixed_from]').datetimepicker('getDate')),
			end: moment.asUtc(this.$el.find('[name=fixed_to]').datetimepicker('getDate')),
		});
	},

	get_floating_model: function() {
		return PersonalTimetabling.Models.Activity.floating({
			domain_template_id: this.$domain_selectbox.val(),
			start: moment.asUtc(this.$el.find('[name=floating_from]').datetimepicker('getDate')),
			period: {
				duration: this.$el.find('[name=floating_to_duration]').val(),
				unit: this.$el.find('[name=floating_to_duration_unit]').val(),
				}, 
			duration_min: (this.$floating_slider.slider('values')[0] * 60),
			duration_max: (this.$floating_slider.slider('values')[1] * 60)
		});
	}

});

});