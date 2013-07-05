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

	url: '/domain_templates',

	initialize: function() {
		if (this.get('id')) {
			this.intervals_collection = new DomainTemplate.DomainTemplateIntervals(null, {
				domain_template_id: this.get('id')
			});
		} else {
			this.intervals_collection = new DomainTemplate.DomainTemplatePreviewIntervals(null, {
				domain_template_model: this
			})
		}

	},

	fetchIntervals: function(from, to) {
		return this.intervals_collection.fetchInRange(from, to);
	},

	syncFetchIntervals: function(from, to, callback) {
      	var xhr = this.fetchIntervals(from, to);
 
      	xhr.success(callback);
      	return xhr;
	}
}, {
	DomainTemplateIntervals: Backbone.Collection.extend({

		initialize: function(models, options) {
			this.url = '/domain_templates/' + options.domain_template_id + '/domain_intervals/';
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