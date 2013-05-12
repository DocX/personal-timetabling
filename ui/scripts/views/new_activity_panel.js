// (c) 2013 Lukas Dolezal
"use strict";

PersonalTimetabling.Views.NewActivityPanel = PersonalTimetabling.Views.PanelBase.extend({

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
				"<label>Between</label>" +
				"<input type='text' name='fixed_from' class='datetime' />" +
				"<label>and</label>" +
				"<input type='text' name='fixed_to' class='datetime' />" +
				"<label>Length in minutes</label>" +
				"<div id='floating_length_slider'></div>" +
			"</div>" +

			"<div>" +
				"<label>Create activity</label>" +
				"<a data-role='activity_create' class='btn btn-primary'>Create activity</a>" +
			"</div>" +

		"</div>",

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

		// hide definition boxes
		this.$fixed_definition_box.hide();
		this.$floating_definition_box.hide();

		this.$el.find('#floating_length_slider').slider({
			range:true,
			min: 1,
			max: 600
		});
	},

	selected_activity_type: function() {
		return this.$el.find('[name=activity_type]:checked').val();
	},

	change_type_view: function(){
		var selected_type = this.selected_activity_type();

		if(this.selected_activity_type() == 'fixed') {
			this.$fixed_definition_box.show();
			this.$floating_definition_box.hide();
		} else if (selected_activity_type == 'floating') {
			this.$fixed_definition_box.hide();
			this.$floating_definition_box.show();
		}
	},

	save: function() {
		var type = this.$el.find('input[name=activity_type]:checked').val();

		var activity_model;
		if (type == 'fixed') {
			activity_model = this.get_fixed_model();
		} else if (activity.type == 'floating') {
			activity_model = this.get_floating_model();
		}

		activity_model.set({
			name: this.$el.find('[name=activty_name]').val()
		});

		activity_model.save();
	},

	get_fixed_model: function() {
		return PersonalTimetabling.Models.Activity.fixed({
			start: moment.asUtc(this.$el.find('[name=fixed_from]').datetimepicker('getDate')),
			end: moment.asUtc(this.$el.find('[name=fixed_to]').datetimepicker('getDate')),
		});
	}

});