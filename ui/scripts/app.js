// (c) 2013 Lukas Dolezal
"use strict";

define([
	// load app wide libraries
	'core/personal_timetabling'
	], function(PT) {
		// define PersonalTimetabling application structure
		var app = {};
		app.initialize = function() {
			$(document).ready(function() {
				var pt_app = new PT();
				pt_app.render();  
			}); 
		};

		return app;
	}
);

