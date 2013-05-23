// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons'),
    Activity = require('models/activity'),
    ActivityOccurance = require('models/activity_occurance'),
    PanelBase = require('components/panel_base');
    
return PanelBase.extend({

	template: 
		"<div>" +
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
		"</div>",

	duration_unit_template: 
		"<select class='input-mini'>" +
			"<option value='hour'>Hours</option>" +
			"<option value='day'>Days</option>" +
			"<option value='week'>Weeks</option>" +
			"<option value='month'>Months</option>" +
		"</select>", 

	initialize: function() {
		this.$el.append(this.template);

		// initialize datetimepickers
		this.$el.find('.datetime').datetimepicker();

		this.$el.find('#floating_to_duration_unit').append($(this.duration_unit_template).attr('name', 'floating_to_duration_unit'));

		// hide definition boxes

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
	
	set_slider_values: function(e,ui) {
		this.$el.find('#floating_duration').text(
			Math.floor(ui.values[0] / 60) + 'h' + (ui.values[0] % 60).pad(2) + 'm' +
			' - ' + 
			Math.floor(ui.values[1] / 60) + 'h' + (ui.values[1] % 60).pad(2) + 'm' );
	},
});

});