// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone');

return Backbone.View.extend({

	overlay_template:
		"<div class='modal hide fade' data-backdrop='static'>" +
		"</div>",

	solving_progress_tpl: 
		"<div class='modal-body'>" +
			"<div class='progress progress-striped active'>" +
	  				"<div class='bar' style='width: 100%;'>Solving...</div>" +
			"</div>" +
		"</div>",
		
	error_tpl:
		"<div class='modal-body'>" +
			"<div class='progress progress-danger progress-striped'>" +
	  			"<div class='bar' style='width: 100%;' data-role='error-msg'></div>" +
			"</div>" +
			"<div class='modal-footer'>" +
			    "<button class='btn' data-dismiss='modal' aria-hidden='true'>Close</button>" +
			"</div>" +
		"</div>",

	initialize: function() {
		this.$el.append(this.overlay_template);
		this.$overlay_modal = this.$el.find('div.modal');

		this.listenTo(this.options.app, 'new:activity', this.new_activity_saved);
		this.listenTo(this.options.app, 'move:event', this.event_updated);

		this.mode = 'realtime';
	},

	new_activity_saved: function(activity) {
		// get events ids
		var event_ids = [];
		_.each(activity.get('event_ids'), function(event_id) {
			event_ids.push({id: event_id, mode:'added'});
		});

		// start solver
		var solver_data = {
			problem_type: 'list',
			events: event_ids
		};

		this.start_solver(solver_data);
	},

	event_updated: function(event) {
		// start solver
		var solver_data = {
			problem_type: 'list',
			events: [
				{id: event.get('id'), mode: 'repair'}
			]
		};

		this.start_solver(solver_data);
	},

	start_solver: function(data) {
		$.ajax({
			url: '/scheduler',
			type: 'post',
			dataType: 'json',
			contentType: "application/json; charset=utf-8",
			processData: false,
			data: JSON.stringify(data),
			success: _.bind(function(result) {
				if (result.state == 'done') {
					// check done
					this.solver_done();
				} else if (result.state == 'running') {
					// long
					this.long_process();
				} else {
					// somethink went wrong
					this.show_error('scheduling error');
				}
			}, this),
			error: _.bind(function() {
				this.show_error('failed to start scheduler');
			}, this)
		});

		this.show_overlay();
	},

	// check done state
	solver_done: function() {
		// save best state
		$.post('/scheduler/best')
			.fail(function() { alert('scheudle done error'); })
			.done(_.bind(function() { this.trigger('done:scheduling')}, this))
			.always(_.bind(function() { this.$overlay_modal.modal('hide'); }, this ));
	},

	// dim screen and check for solver end
	long_process: function() {
		this.trigger('start:scheduling');

		// set timeer
		this.check_timeout = window.setTimeout(_.bind(this.check_running, this), 750);
	},

	check_running: function() {
		$.post('/scheduler/best')
			.fail(function() { alert('scheudle done error'); })
			.done(_.bind(function(data) { 
				if (data.state == 'done') {
					this.trigger('done:scheduling');
				} else {
					this.check_timeout = window.setTimeout(_.bind(this.check_running, this), 750);
				}
			}, this))
			.always(_.bind(function() { this.$overlay_modal.modal('hide'); }, this ));
	},

	show_overlay: function() {
		this.$overlay_modal.empty();
		this.$overlay_modal.append(this.solving_progress_tpl);
		this.$overlay_modal.modal('show');
	},

	show_error: function(error_message) {
		this.$overlay_modal.empty();
		this.$overlay_modal.append(this.error_tpl);
		this.$overlay_modal.find('[data-role=error-msg]').text(error_message);
		this.$overlay_modal.modal('show');
	}

});
});