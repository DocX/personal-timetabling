// (c) 2013 Lukas Dolezal
"use strict";

var PersonalTimetabling = {};
var PT = PersonalTimetabling;

PT.App = null;
PT.CalendarViews = {};
PT.Models = {};



$(document).ready(function() {
   PT.AppInstance = new PT.App(); 
   PT.AppInstance.render();  
  }); 
