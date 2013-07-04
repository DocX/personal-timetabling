// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var PT = require('core/personal_timetabling');

return function() {
	return {
		initialize: function() {
			$(document).ready(function() {
				var pt_app = new PT({
					el: $('#application')
				});
			}); 
		}
	}
}();

});