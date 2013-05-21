// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    ColumnsDaysView = require('views/columns_days_view'),
    OccurancesCollection = require('models/occurances_collection'),
    jQueryAvcitivityBox = require('lib/jquery.ui.activitybox');
    

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
var ColumnsDaysActivitiesView;
return ColumnsDaysActivitiesView = Backbone.View.extend({
  
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

    // array for keeping displayed activities not from collection
    this.unmapped_activities = [];
  },

  render: function() {
    this.calendar.render();
    this.reload_activities();
  },

  set_column_type: function(type) {
    this.calendar.set_column_type(type);
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

      this.add_occurance_box(occurance);
    }

    for (var i = this.unmapped_activities.length - 1; i >= 0; i--) {
      this.unmapped_activities[i].get('occurances').each(this.add_raw_occurance_box, this);
    };
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
  
  add_raw_occurance_box: function(occurance, is_new_resizing) {
    var box = $("<div />");
    
    this.calendar.add_interval_box(box); 

    box.activity_occurance_box({
      view: this.calendar,
      steps: this.calendar.column_step_minutes,
      occurance: occurance,
      remove: _.bind(this.delete_activity_occurance, this),
      box_setup: function(e, box) {  box.data('occurance', occurance); },
    });
    box.data('occurance', occurance);

    return box;
  },

  add_occurance_box: function(occurance) {
    var box = add_raw_occurance_box(occurance);

    var activate_fn = function(that) { return function() { that.activate_occurance_box(this) } }(this);
    box.mousedown(activate_fn);
    var box_setup = box.activity_occurance_box('options').box_setup;
    box.activity_occurance_box('options', {box_setup: function(e,box) {box.mousedown(activate_fn); box_setup(e, box); }});

    if (occurance.id == this.active_id) {
      this.activate_occurance_box(box);
    }

  },

  activate_occurance_box: function(box) {
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

  // displays activity and keep track of its changes
  // return handle to remove it from display
  display_activity: function(activity) {
    this.unmapped_activities.push(activity);
    this.refresh();

    return new ColumnsDaysActivitiesView.UnmappedActivityHandle(this.unmapped_activities.length -1, this);
  }
}, {

  UnmappedActivityHandle: Base.extend({
    constructor: function(index, view) {
      this.index = index;
      this.view = view;
    },

    remove: function() {
      if (this.deleted) {
        return;
      }
      this.view.unmapped_activities.slice(this.index,1);
      this.view.refresh();
      this.deleted = true;
    }
  }),

});

});