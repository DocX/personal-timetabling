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

		this.mode = 'running';
		this.delayed_events = [];

		this.check_running();
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

		if (this.mode == 'running') {
			this.start_solver(solver_data);
		} else {
			this.delayed_events.push({type: 'new_activity', data: solver_data});
		}
		
	},

	event_updated: function(event) {
		// start solver
		var solver_data = {
			problem_type: 'list',
			events: [
				{id: event.get('id'), mode: 'repair'}
			]
		};

		if (this.mode == 'running') {
			this.start_solver(solver_data);
		} else {
			this.delayed_events.push({type: 'event_repair', data: solver_data});
		}
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
			.fail(_.bind(function() { 
				this.show_error('error when saving result.');
			}, this ))
			.done(_.bind(function(data) { 
				if (data.state == 'done') {
					this.$overlay_modal.modal('hide');
					this.trigger('done:scheduling');
				} else if (data.state == 'none') {
					this.$overlay_modal.modal('hide');
				} else {
					this.show_overlay();
					this.check_timeout = window.setTimeout(_.bind(this.check_running, this), 750);
				}
			}, this));
			
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
	},

	set_mode: function(mode) {
		if (this.mode == mode) {
			return;
		}

		if (this.mode == 'planning') {
			// run scheduler for delayed events
			this.schedule_delayed();
		} else if (this.mode == 'running') {
			this.delayed_events = [];
		}

		this.mode = mode;
	},

	schedule_delayed: function() {
		// all delayed events will be scheduled with the same priority,
		// all other will have higher priority
		// ie all add with mode 'added'

		var events = {};
		var data = {
			problem_type: 'list',
			events: []
		};
		for (var i = 0; i < this.delayed_events.length; i++) {
			var delayed_data = this.delayed_events[i];	
			
			for (var j = 0; j < delayed_data.data.events.length; j++) {
				var event = delayed_data.data.events[j];

				if (event.id in events) {
					continue;
				}

				events[event.id] = true;
				data.events.push({id: event.id, mode:'added'});
			};
		};

		this.start_solver(data);
	},

});
});