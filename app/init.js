// (c) 2013 Lukas Dolezal

var PersonalTimetabling = {};
var PT = PersonalTimetabling;

"use strict";

PT.App = null;
PT.CalendarViews = {};

// when ready, start app
$(document).ready(function() {
   PT.AppInstance = new PT.App(); 
   PT.AppInstance.render();  
});








