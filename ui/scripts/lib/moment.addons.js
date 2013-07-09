// (c) 2013 Lukas Dolezal
"use strict";

define(['moment'], function(moment) {

// Parses given date as UTC ignoring its timezone
moment.asUtc = function(date) {
	return moment.utc(moment(date).toJSON().replace(/Z$/,''))
};

moment.asLocal = function(date) {
	date = moment.utc(date).toJSON();
	
	return moment(date.replace(/(Z|\d\d\d\d)$/,''))	
}

});