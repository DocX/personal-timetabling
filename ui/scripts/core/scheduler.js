// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone');

return Backbone.View.extend({

	initialize: function() {
		this.listenTo(this.options.app, 'new:activity', this.new_activity_saved);
		this.listenTo(this.options.app, 'move:event', this.event_updated);

		this.mode = 'realtime';
	},

	new_activity_saved: function(activity) {
		// get events ids
		var event_ids = [];
		activity.get('events').forEach(function(e) {
			event_ids.push({id: e.get('id'), mode:'added'});
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
					alert('schedule error');
				}
			}, this),
			error: function() {
				alert('starting schedule error')
			}
		});
	},

	// check done state
	solver_done: function() {
		// save best state
		$.post('/scheduler/best')
			.fail(function() { alert('scheudle done error'); })
			.done(_.bind(function() { this.trigger('done:scheduling')}, this));
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
			}, this));
	}

});
});