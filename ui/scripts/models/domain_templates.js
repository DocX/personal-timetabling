// (c) 2013 Lukas Dolezal
"use strict";

PersonalTimetabling.Models.DomainTemplate = Backbone.Model.extend({

	defaults: {
		name: '',
		id: null,
		domain_stack: {},
	},

	initialize: function() {
		if (this.get('id')) {
			this.intervals_collection = new PersonalTimetabling.Models.DomainTemplateIntervals(null, {
				domain_template_id: this.get('id')
			});
		} else {
			this.intervals_collection = new PersonalTimetabling.Models.DomainTemplatePreviewIntervals(null, {
				domain_template_model: this
			})
		}
	},

	fetchIntervals: function(from, to) {
		return this.intervals_collection.fetchInRange(from, to);
	}
});

PersonalTimetabling.Models.Interval = Backbone.Model.extend({

	defaults: {
		start: moment.utc(),
		end: moment.utc(),
	},

	parse: function(data) {
		return {
			start: moment.utc(data.start),
			end: moment.utc(data.end)
		};
	}

});


PersonalTimetabling.Models.DomainTemplatesCollection = Backbone.Collection.extend({

	url: '/domain_templates',

	model: PersonalTimetabling.Models.DomainTemplate,

});

PersonalTimetabling.Models.DomainTemplateIntervals = Backbone.Collection.extend({

	initialize: function(models, options) {
		this.url = '/domain_templates/' + options.domain_template_id + '/';
	},

	model: PersonalTimetabling.Models.Interval,

	fetchInRange: function(start, end) {
		return this.fetch({data: {from:start.toJSON(), to:end.toJSON()}});
	}

});

// Virtual collection for retrieving domain template preview intervals
PersonalTimetabling.Models.DomainTemplatePreviewIntervals = Backbone.Collection.extend({

	initialize: function(models, options) {
		this.domain_template_model = options.domain_template_model;
	},

	url: '/domain_templates/preview',

	model: PersonalTimetabling.Models.Interval,

	fetchInRange: function(start, end) {
		if (!('0' in this.domain_template_model.get('domain_stack'))) {
			return false;
		}

		return this.fetch({
			data: {
				from:start.toJSON(), 
				to:end.toJSON(), 
				domain_stack: this.domain_template_model.get('domain_stack')
			}, 
			type: "POST"
		});
	}

});