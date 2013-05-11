// (c) 2013 Lukas Dolezal
"use strict";

PersonalTimetabling.Views.DomainTemplatePanel = Backbone.View.extend({

	template: 
		"<div>" +
			"<label>Select domain to show</label>" +
			"<select data-source='domain_templates'></select>" +
		"</div>",

	initialize: function() {
		// load available domain templates

		this.collection = new PersonalTimetabling.Models.DomainTemplatesCollection();


		this.$form = $(this.template);
		this.$el.append(this.$form);
		this.$domain_selectbox = this.$form.find('[data-source=domain_templates]');

		this.collection.fetch()
			.success(_.bind(function() {
				this.$domain_selectbox.empty();
				this.$domain_selectbox.append("<option value=''></option>");
				var $domain_selectbox = this.$domain_selectbox;

				this.collection.forEach(function(domain) {
					$domain_selectbox.append($("<option/>").attr({
						value: domain.get('id')
					}).text(domain.get('name')))
				});

			}, this)
			);

		this.$domain_selectbox.change(_.bind(this.show_domain, this));

		this.listenTo(this.options.calendar_view, 'columns_updated', this.reload_intervals);

		// store of DOM boxes of intervals displayed in the calendar view
		this.handling_boxes = [];
	},

	show_domain: function() {
		// store selected domain model

		this.selected_domain = this.collection.get(this.$domain_selectbox.val());	
		this.reload_intervals();	
	},

	reload_intervals: function() {
		var range = this.options.calendar_view.showing_dates();
		var panel_view = this;

		// fetch intervals
		if (this.selected_domain) {
			this.selected_domain.fetchIntervals(range.start, range.end)
			.success(function() {
				var intervals = panel_view.selected_domain.intervals_collection.models;
				// delete boxes DOM
				for (var i = panel_view.handling_boxes.length - 1; i >= 0; i--) {
					for (var y = panel_view.handling_boxes[i].length - 1; y >= 0; y--) {
						panel_view.handling_boxes[i][y].remove();
					};
				};				
				panel_view.handling_boxes = [];

				_.forEach(intervals, function(interval) {
					var boxes = panel_view.options.calendar_view.display_interval(
						interval.get('start'),
						interval.get('end'),
						function(boxdom) {boxdom.css('background', 'rgba(255,255,255,0.5)').addClass('domain-highlight');}
						);	
					panel_view.handling_boxes.push(boxes);
				});

			});
		}
	}



});