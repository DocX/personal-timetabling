// (c) 2013 Lukas Dolezal
"use strict";

//setup require paths to non AMD libraries
require.config({
  paths: {
    'jquery': 				'../vendor/jquery',
    'underscore': 		'../vendor/underscore',
    'backbone': 			'../vendor/backbone',
    'backbone.relational': 	'../vendor/backbone-relational',
    'moment': 				'../vendor/moment',
    'jquery-ui': 			'../vendor/jquery-ui/js/jquery-ui',
    'jquery-ui-timepicker': '../vendor/jquery-ui-timepicker-addon',
    'bootstrap': 			'../vendor/bootstrap/js/bootstrap',
   	'base': 				  '../vendor/Base',
  },
  shim: {
  	'backbone': {
  		deps: ['underscore', 'jquery'],
  		exports: 'Backbone'
  	},
  	'backbone.relational': ['backbone'],
  	'jquery.ui.timepicker': ['jquery-ui'],
  	'base': {
  		deps:[],
  		exports: 'Base'
  	},
    'bootstrap': ['jquery', 'jquery-ui'],
    'jquery-ui': ['jquery']
  }
});

// load applicaion
require([
  	'app',
    'bootstrap'
], function(App){
  App.initialize();
});