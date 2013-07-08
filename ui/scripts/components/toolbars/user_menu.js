// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment'),
    DomainTemplatesPanel = require('components/side_panels/domain_templates/domain_templates_index');
    
// View controller for calendar buttons
return Backbone.View.extend({

	template: 
		"<div >" +
			"<div class='btn-group '>" +
				"<button class='btn btn-inverse'><i class='icon-user icon-white'></i>  Demo</button>" +
				"<button class='btn dropdown-toggle btn-inverse' data-toggle='dropdown'>" +
					"<span class='caret'></span>" +
				"</button>" +
				"<ul class='dropdown-menu pull-right'>" +
					"<li><a tabindex='-1' href='#'>Activities</a></li>" +
					"<li><a tabindex='-1' href='#' data-role='domain-template'>Domain templates</a></li>" +
					"<li class='divider'></li>" +
					"<li><a tabindex='-1' href='#pt-modal-about' data-toggle='modal' role='button'><i class=' icon-info-sign'></i> About</a></li>" +
				"</ul>" +
			"</div>" +
		"</div>",

	initialize: function() {
		this.$el.append(this.template);
		this.$el.addClass('rightcomponents');
		
		this.$el.find('[data-role=domain-template]').click(_.bind(function() {
			this.options.app.open_panel(DomainTemplatesPanel, {calendar_view: this.options.app.calendar_view.calendar});
		}, this));
	}

});
});