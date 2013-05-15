// (c) 2013 Lukas Dolezal
"use strict";

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.ColumnsDaysActivitiesView = Backbone.View.extend({

  
  initialize: function() {
    
    this.calendar = new PersonalTimetabling.CalendarViews.ColumnsDaysView({el: this.el});
    
    this.collection = new PersonalTimetabling.Models.OccurancesCollection();
    
    //this.listenTo(this.collection, 'sync', this.refresh);
    this.listenTo(this.calendar, 'columns_updated', this.reload_activities);

    this.calendar.$grid_overlay_el.mouse_events({
      distance: 10,
      onstart: _.bind(this.mouse_create_box, this)
    });
    this.calendar.$grid_overlay_el.click(_.bind(function(e){
      if (e.target != this.calendar.$grid_overlay_el.get()[0] )
        return;

      this.active_id = null;
      this.calendar.$grid_overlay_el.find('.activity-occurance.active').removeClass('active');
      this.domain_intervals_display && _.forEach(this.domain_intervals_display, function(i) {i.remove()});
    }, this));
  },

  render: function() {
    this.calendar.render();
    this.reload_activities();
  },

  reload_activities: function() {
    this.calendar.clear_intervals();

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
    this.calendar.clear_intervals('.activity');

    for(var io = 0; io < occurances_to_show.length; io++) {
      var occurance = occurances_to_show[io];
      if (occurance.get('activity') == null)
        continue;
      this.add_activity_box(occurance);
    }
  },
  
  add_activity_box: function(occurance, is_new_resizing) {
    var box = $("<div />");
    
    this.calendar.add_interval_box(box);
   
    box.activity_occurance_box({
      view: this.calendar,
      steps: this.calendar.column_step_minutes,
      occurance: occurance,
      remove: _.bind(this.delete_activity_occurance, this) 
    });

    box.mousedown(function(that) { return function() { that.activate_box(this) } }(this));

    if (occurance.id == this.active_id) {
      this.activate_box(box);
    }
  },

  activate_box: function(box) {
    if ($(box).hasClass('active'))
      return;

    this.domain_intervals_display && _.forEach(this.domain_intervals_display, function(i) {i.remove()});

    //get its intervals and shows them
    var occurance = $(box).closest('.activity-occurance').activity_occurance_box('getOccurance');
    this.active_id = occurance.id;

    this.calendar.$grid_overlay_el.find('.activity-occurance.active').removeClass('active');
    $(box).addClass('active');

    var range = this.calendar.showing_dates();
    occurance.domain_intervals.fetchRange(range.start, range.end)
    .success(_.bind(_.partial(this.show_domain, occurance), this));
  },

  show_domain: function(occurance) {
    

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
