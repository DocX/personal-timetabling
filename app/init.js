// (c) 2013 Lukas Dolezal

var PersonalTimetabling = {};
var PT = PersonalTimetabling;

PT.App = null;
PT.CalendarViews = {};
PT.Models = {};

// when ready, start app
$(document).ready(function() {
   PT.AppInstance = new PT.App(); 
   PT.AppInstance.render();  
});








