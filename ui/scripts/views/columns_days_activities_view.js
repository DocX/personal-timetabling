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
    this.listenTo(this.calendar, 'geometry_changed', function() {this.trigger('geometry_changed');});


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

  // fetches occurances in current view range 
  // and then triggers its redraw
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
      this.unmapped_activities[i].get('occurances').each(
        function(occurance) { if (occurance.inRange(range.start, range.end)) { this.add_raw_occurance_box(occurance) } },
         this);
    };
  },

  keyup: function(e) {
    if (e.which == 46 && this.active_id) {
      this.collection.findWhere({id: this.active_id}).destroy();
      this.clear_selection();
    }
  },


  // removes state of activated occurances
  clear_selection: function() {
    this.active_id = null;
    this.calendar.$grid_overlay_el.find('.activity-occurance.active').removeClass('active');
    this.domain_intervals_display && _.forEach(this.domain_intervals_display, function(i) {i.remove()});
  },
  
  // creates view box for given occurance
  // and binds it to the model. changes are not triggered to sync with server
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

  // binds occurance model with created view for it
  // and synchronize all changes from view to model and server with save on model
  add_occurance_box: function(occurance) {
    var box = this.add_raw_occurance_box(occurance);

    var activate_fn = function(that) { return function() { that.activate_occurance_box(this) } }(this);
    box.mousedown(activate_fn);
    var box_setup = box.activity_occurance_box('option', 'box_setup');
    box.activity_occurance_box('option', 'box_setup', function(e,box) {box.mousedown(activate_fn); box_setup(e, box); });
    box.activity_occurance_box('option', 'dropped', function(e, occurance) {occurance.save();} );


    if (occurance.id == this.active_id) {
      this.activate_occurance_box(box);
    }

  },

  // handle for activationg occurance box in the view
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

  // fetches and displays domain of given occurance model
  show_domain: function(occurance) {
     // remove currenlty displaying intervals
    this.domain_intervals_display && _.forEach(this.domain_intervals_display, function(i) {i.remove()});   

    this.domain_intervals_display = this.calendar.display_intervals(
        occurance.domain_intervals.models,
        function(box) {box.addClass('domain-highlight')}
        );
  },
  
  // handler for delete key on the active occurance
  delete_activity_occurance: function(e, data) {
    data.occurance.destroy();
    data.element.remove();
  },

  // displays activity and keep track of its changes
  // return handle to remove it from display
  display_activity: function(activity) {
    this.unmapped_activities.push(activity);
    activity.on('all', _.debounce(this.refresh, 250), this);
    //activity.on('all', this.refresh, this);

    this.refresh();

    return new ColumnsDaysActivitiesView.UnmappedActivityHandle(this.unmapped_activities.length -1, this);
  },

  // sets view to be centered on the date
  show_date: function(date) {
    this.calendar.display_date(date);
  },

  // returns name of geometry currently displayed in view
  // eg days, weeks, months
  get_view_geometry_name: function(){
    return this.calendar.geometry.get_name();
  },

  // returns date aligned to current view granularity
  get_date_aligned_to_view_grid: function(date) {
    return this.calendar.geometry.get_rounded_date(date, this.calendar.column_step_minutes);
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