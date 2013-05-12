// (c) 2013 Lukas Dolezal
"use strict";


// Parses given date as UTC ignoring its timezone
moment.asUtc = function(date) {
	return moment.utc(moment(date).format("YYYY-MM-DDTHH:mm:ss"))
}