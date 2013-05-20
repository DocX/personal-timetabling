// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    ColumnsDaysView = require('views/columns_days_view'),
    OccurancesCollection = require('models/occurances_collection'),
    jQueryAvcitivityBox = require('lib/jquery.ui.activitybox');
    

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
return Backbone.View.extend({
  
  initialize: function() {
    
    this.calendar = new ColumnsDaysView({el: this.el});
    
    this.collection = new OccurancesCollection();
    
    this.listenTo(this.collection, 'related:activity:fetch', this.refresh);
    this.listenTo(this.collection, 'destroy', this.refresh);
    this.listenTo(this.calendar, 'columns_updated', this.reload_activities);


    this.calendar.$grid_overlay_el.click(_.bind(function(e){
      if (e.target != this.calendar.$grid_overlay_el.get()[0] )
        return;

      this.clear_selection();
    }, this));

    $(window).resize(_.bind(this.calendar.resize, this.calendar));
    $(window).keyup(_.bind(this.keyup, this));
  },

  render: function() {
    this.calendar.render();
    this.reload_activities();
  },

  reload_activities: function() {
    // render first current state of collection
    this.refresh();

    // fetch new view
    var range = this.calendar.showing_dates();
    this.collection.fetchRange(range.start, range.end)
    .success(_.bind(this.refresh, this));
  },
  
  // renders activities
  refresh: function() {    
    console.log('activities updating');
    var range = this.calendar.showing_dates();

    var occurances_to_show = this.collection.inRange(
      range.start, range.end
    ); 
    this.calendar.clear_intervals('.activity-occurance');

    for(var io = 0; io < occurances_to_show.length; io++) {
      var occurance = occurances_to_show[io];
      if (occurance.get('activity') == null)
        continue;
      this.add_activity_box(occurance);
    }
  },

  keyup: function(e) {
    if (e.which == 46 && this.active_id) {
      this.collection.findWhere({id: this.active_id}).destroy();
      this.clear_selection();
    }
  },

  clear_selection: function() {
    this.active_id = null;
    this.calendar.$grid_overlay_el.find('.activity-occurance.active').removeClass('active');
    this.domain_intervals_display && _.forEach(this.domain_intervals_display, function(i) {i.remove()});
  },
  
  add_activity_box: function(occurance, is_new_resizing) {
    var box = $("<div />");
    
    this.calendar.add_interval_box(box);
   
    var activate_fn = function(that) { return function() { that.activate_box(this) } }(this);

    box.activity_occurance_box({
      view: this.calendar,
      steps: this.calendar.column_step_minutes,
      occurance: occurance,
      remove: _.bind(this.delete_activity_occurance, this),
      'box_setup': function(e, box) { box.mousedown(activate_fn); box.data('occurance', occurance); },
    });
    box.data('occurance', occurance);

    box.mousedown(activate_fn);

    if (occurance.id == this.active_id) {
      this.activate_box(box);
    }
  },

  activate_box: function(box) {
    if ($(box).hasClass('active'))
      return;


    //get its intervals and shows them
    var occurance = $(box).closest('.activity-occurance').data('occurance');
    this.active_id = occurance.id;

    // display again last intervals in activity memory
    //this.show_domain(occurance);

    this.calendar.$grid_overlay_el.find('.activity-occurance.active').removeClass('active');
    this.calendar.$grid_overlay_el.find('.activity-occurance')
    .filter(function() {
      return $(this).data('occurance').id == occurance.id
    }).addClass('active');

    var range = this.calendar.showing_dates();
    occurance.domain_intervals.fetchRange(range.start, range.end)
    .success(_.bind(_.partial(this.show_domain, occurance), this));
  },

  show_domain: function(occurance) {
     // remove currenlty displaying intervals
    this.domain_intervals_display && _.forEach(this.domain_intervals_display, function(i) {i.remove()});   

    this.domain_intervals_display = this.calendar.display_intervals(
        occurance.domain_intervals.models,
        function(box) {box.addClass('domain-highlight')}
        );
  },
  
  delete_activity_occurance: function(e, data) {
    data.occurance.destroy();
    data.element.remove();
  },
  
  mouse_create_box: function(e) {
    // determine mouse click position
    var column_index = Math.floor(e[this.axis == 'x' ? 'offsetX' : 'offsetY'] / this.calendar.drawing_column_width);
    var column_line = this.calendar.get_box_offset_in_column({left: e.offsetX, top: e.offsetY});
    
    var start = this.calendar.geometry.get_date_of_line(
      this.calendar.drawing_columns_list[column_index].column_id, 
      column_line, this.calendar.column_step_minutes);
    
    var new_activity = 
      PersonalTimetabling.Models.Activity.fixed({
        start: start ,
        end: start.clone().add('h', 2),
        name: 'Nova aktivita'
      });
    
    //this.collection.create(new_activity);
    
    this.add_activity_box(new_activity.get('occurances').models[0], true);
  },
});

});