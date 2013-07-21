// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    ColumnsDaysActivitiesView = require('views/columns_days_activities_view'),
    CalendarButtons = require('components/toolbars/calendar_buttons'),
    StatusPanel = require('components/toolbars/status_panel'),
    UserMenu = require('components/toolbars/user_menu'),
    ActivitiesButtons = require('components/toolbars/activities_buttons'),
    Scheduler = require('core/scheduler'),
    SchedulerButtons = require('components/toolbars/scheduler_buttons'),
    EventEditor = require('components/side_panels/events/event_edit');

return Backbone.View.extend({

   template:
    "<div id='topbar'>" +
      "<div id='logo'>" +
        "<h1>Personal Timetabling <i>beta</i></h1> " +
      "</div>" +

      "<div id='common-panel-place'>" +
        "<div id='status-panel'></div>" +
        "<div id='activities-panel'></div>" +
        "<div id='scheduler-panel'></div>" +
        "<div id='user-panel'></div>" +
      "</div>" +

      "<div id='content-panel-place'>" +
      "</div>" +

    "</div>" +

    "<div id='content'>" +
      "<div class='panel-layout'>" + 
        "<div id='sidepanel'></div>" +
        "<div id='mainview'></div>" +
      "</div>" +
    "</div>" +

    "<div id='scheduler-el'>" +
    "</div>",
      
  
  initialize: function() {

    this.$el.append(this.template);
    this.sidebar = null;  
    this.$app_view = this.$el.find('#content .panel-layout');
  
    this.calendar_view = new ColumnsDaysActivitiesView({el: this.$el.find("#mainview")});
    this.calendar_buttons = new CalendarButtons({el: '#content-panel-place', calendar_view: this.calendar_view});

    this.status_panel = new StatusPanel({el: this.$el.find('#status-panel'), app: this});
    this.activities_panel = new ActivitiesButtons({el: this.$el.find('#activities-panel'), app: this});
    this.user_menu = new UserMenu({el: this.$el.find('#user-panel'), app: this});

    this.scheduler = new Scheduler({el: this.$el.find('#scheduler-el'), app: this});
    this.scheduler_buttons = new SchedulerButtons({el: this.$el.find('#scheduler-panel'), app: this, scheduler: this.scheduler});
    
    // plumb events
    this.listenTo(this.calendar_view, 'all', this.trigger);
    this.listenTo(this.scheduler, 'all', this.trigger);
    this.calendar_view.listenTo(this.scheduler, 'done:scheduling', this.calendar_view.reload_activities);
    this.listenTo(this.calendar_view, 'dblclick:event', this.open_event_editor);

    // start doing job
    this.calendar_view.render();
  },

  open_panel: function(view_class, options) {
    if (this.sidebar) {
      this.stopListening(this.sidebar);
      this.sidebar.remove();
    }

    this.$app_view.addClass('panel-open');
    if (this.sidebar == null) {
      this.calendar_view.render();
    }

    $.extend(options, {el: $("<div class='fill'/>").appendTo(this.$app_view.find("#sidepanel")), app: this});

    this.sidebar = new view_class(options);
    this.listenTo(this.sidebar, 'removed', this.hide_panel);
    this.listenTo(this.sidebar, 'all', this.trigger);
  },

  hide_panel: function() {
    this.$app_view.removeClass('panel-open');
    this.calendar_view.render();    
  },

  open_event_editor: function(event_model) {
    event_model.fetch()
    .success(_.bind(function() {
      this.open_panel(EventEditor, {
        model: event_model,
        activities_view: this.calendar_view
      });
    }, this));
    
  }

});

});