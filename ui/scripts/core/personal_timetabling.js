// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),

    ColumnsDaysActivitiesView = require('views/columns_days_activities_view'),
    CalendarButtons = require('components/calendar_buttons'),
    DomainTemplateEditor = require('components/domain_template_editor'),
    NewActivityPanel = require('components/new_activity_panel');


return Backbone.View.extend({

   view_template:
    "<div class='panel-layout'>" + 
      "<div id='sidepanel'></div>" +
      "<div id='mainview'></div>" +
    "</div>"
    ,
      
  
  initialize: function() {
    $(window).resize(function(that) { return function() {that.resize();} } (this));

    $(document).on('ajaxStart', function() {  $("#ajax-indicator").show(); });
    $(document).on('ajaxStop', function() {  $("#ajax-indicator").hide(); });
    
    this.layout = $(this.view_template);
    $("#content").append(this.layout);

    this.calendar_view = new ColumnsDaysActivitiesView({el: this.layout.find("#mainview")});
    this.calendar_buttons = new CalendarButtons({el: '#content-panel-place', calendar_view: this.calendar_view});
    
    this.sidebar = null;

    $("a[data-role=add_domain_template]").click(_.bind(function(){
      //open sidebar
      this.open_panel(DomainTemplateEditor, {
          calendar_view: this.calendar_view.calendar
        });
    }, this));

    $("a[data-role=add_activity]").click(_.bind(function(){
      //open sidebar
      this.open_panel(NewActivityPanel, {});
      this.listenTo(this.sidebar, 'added', function() {this.calendar_view.reload_activities();});
    }, this));

  },

  open_panel: function(view_class, options) {
    if (this.sidebar) {
      this.sidebar.remove();
    }

    this.layout.addClass('panel-open');

    $.extend(options, {el: $("<div class='fill'/>").appendTo(this.layout.find("#sidepanel"))});

    this.sidebar = new view_class(options);
    this.listenTo(this.sidebar, 'removed', this.hide_panel);
  },

  hide_panel: function() {
    this.layout.removeClass('panel-open')    
  },

  render: function() {
    this.calendar_view.render();
  },

});

});