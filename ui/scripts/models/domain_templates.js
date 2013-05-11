// (c) 2013 Lukas Dolezal
"use strict";

PersonalTimetabling.Models.DomainTemplate = Backbone.Model.extend({

	defaults: {
		name: '',
		id: null,
	},

	initialize: function() {
		this.intervals_collection = new PersonalTimetabling.Models.DomainTemplateIntervals(null, {
			domain_template_id: this.get('id')
		});
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