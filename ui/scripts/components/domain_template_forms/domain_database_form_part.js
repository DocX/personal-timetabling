// (c) 2013 Lukas Dolezal
"use strict";

/*

database select domain form part

*/

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    jQueryDateTimePicker= require('jquery-ui-timepicker'),
    moment = require('moment'),
    momentAddons = require('lib/moment.addons');

return Backbone.View.extend({

	template:
		"<div>" +
			"<select name='domains-select'>"+
			"</select>"+
		"</div>",

	initialize: function() {
		this.$el.append(this.template);

		this.$databases_select = this.$el.find('[name=domains-select]');
		this.render_options();

		this.load_from_model();
	},

	render_options: function() {
		this.$databases_select.empty();
		this.options.db_domains.forEach(function(domain) {
			this.$databases_select.append($('<option />').attr('value', domain.id).text(domain.get('name')));
		}, this);
	},

	load_from_model: function() {
		this.$databases_select.find('[value=' + this.model.data.id + ']').prop('selected', true);
	},

	update_to_model: function() {
		this.model.type = 'domain_template';
		this.model.data.id = this.$databases_select.find(':selected').attr('value');
		this.trigger('change');
	}

})
});