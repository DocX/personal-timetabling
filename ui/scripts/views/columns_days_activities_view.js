// (c) 2013 Lukas Dolezal
"use strict";

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.ColumnsDaysActivitiesView = PersonalTimetabling.CalendarViews.ColumnsDaysView.extend({

  
    
  initialize: function() {
    
    PersonalTimetabling.CalendarViews.ColumnsDaysView.prototype.initialize.apply(this);
    
    //this.listenTo(this.collection, 'sync', this.update_data_view);
    this.listenTo(this, 'columns_updated', this.reload_activities);
    
    this.$grid_overlay_el.mouse_events({
      distance: 10,
      onstart: _.bind(this.mouse_create_box, this)
    });
  },

  reload_activities: function() {
    this.$grid_overlay_el.find("[data-type='activity-occurance']").remove();

    var range = this.showing_dates();
    this.collection.fetchRange(range.start, range.end)
    .success(_.bind(this.update_data_view, this));
  },
  
  // renders activities
  update_data_view: function() {    
    console.log('activities updating');
    var range = this.showing_dates();

    var occurances_to_show = this.collection.inRange(
      range.start, range.end
    );
    this.$grid_overlay_el.find("[data-type='activity-occurance']").remove();

    for(var io = 0; io < occurances_to_show.length; io++) {
      var occurance = occurances_to_show[io];
      if (occurance.get('activity') == null)
        continue;
      this.add_activity_box(occurance);
    }
  },
  
  add_activity_box: function(occurance, is_new_resizing) {
    var box = $("<div />");
    this.$grid_overlay_el.append(box);
   
    box.activity_occurance_box({
      view: this,
      steps: this.column_step_minutes,
      occurance: occurance,
      in_mouse_move: true,
      remove: _.bind(this.delete_activity_occurance, this) 
    });
  },
  
  delete_activity_occurance: function(e, data) {
    data.occurance.destroy();
    data.element.remove();
  },
  
  mouse_create_box: function(e) {
    // determine mouse click position
    var column_index = Math.floor(e[this.axis == 'x' ? 'offsetX' : 'offsetY'] / this.drawing_column_width);
    var column_line = this.get_box_offset_in_column({left: e.offsetX, top: e.offsetY});
    
    var start = this.geometry.get_date_of_line(this.drawing_columns_list[column_index].column_id, column_line, this.column_step_minutes);
    
    var new_activity = 
      PersonalTimetabling.Models.Activity.createFixed({
        start: start ,
        duration: 3600,
        name: 'Nova aktivita'
      });
    
    //this.collection.create(new_activity);
    
    this.add_activity_box(new_activity.get('occurances').models[0], true);
  },

});
