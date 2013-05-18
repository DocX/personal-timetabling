// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var Backbone = require('backbone'),
    DomainTemplate = require('models/domain_template');


return Backbone.Collection.extend({
	url: '/domain_templates',

	model: DomainTemplate,
});


});