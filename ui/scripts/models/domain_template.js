// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
    Interval = require('models/interval');

var DomainTemplate;
return DomainTemplate = Backbone.Model.extend({

	defaults: {
		name: '',
		id: null,
		domain_attributes: ''
	},

	urlRoot: '/domain_templates',

	initialize: function() {
		this.intervals_collection = new DomainTemplate.DomainTemplateIntervals(null, {
			domain_template_model: this
		});
	

		this.preview_intervals_collection = new DomainTemplate.DomainTemplatePreviewIntervals(null, {
			domain_template_model: this
		})
	},

	// request domain intervals for state stored in server
	fetchIntervals: function(from, to) {
		return this.intervals_collection.fetchInRange(from, to);
	},

	// request domain intervals for current state of domain template model
	fetchPreviewIntervals: function(from, to) {
		return this.preview_intervals_collection.fetchInRange(from,to);
	}
}, {
	DomainTemplateIntervals: Backbone.Collection.extend({

		initialize: function(models, options) {
			this.url = '/domain_templates/' + options.domain_template_model.get('id') + '/domain_intervals/';
		},

		model: Interval,

		fetchInRange: function(start, end) {
			return this.fetch({data: {from:start.toJSON(), to:end.toJSON()}});
		}

	}),

	// Virtual collection for retrieving domain template preview intervals
	DomainTemplatePreviewIntervals: Backbone.Collection.extend({

		initialize: function(models, options) {
			this.domain_template_model = options.domain_template_model;
		},

		url: '/domain_templates/domain_intervals',

		model: Interval,

		fetchInRange: function(start, end) {
			if (! this.domain_template_model.get('domain_attributes')) {
				return false;
			}

			return this.fetch({
				data: JSON.stringify({
					from:start.toJSON(), 
					to:end.toJSON(), 
					domain_template: this.domain_template_model.toJSON()
				}), 
				type: "POST",
				processData: false,
				contentType: 'application/json'
			});
		}

	})

});


});