// (c) 2013 Lukas Dolezal

PersonalTimetabling.App = Backbone.View.extend({

  initialize: function() {


    $("[data-submit=schedule-activity]").click(_.partial(this.schedule_activity_clicked, this));
        
    this.$topbar = $("#topbar");

    $(window).resize(function(that) { return function() {that.resize();} } (this));

    $(document).on('ajaxStart', function() {  $("#ajax-indicator").show(); });
    $(document).on('ajaxStop', function() {  $("#ajax-indicator").hide(); });
    
    this.view = null;
    
    if ($("#calendar-view").length > 0) {
      this.occurances = new PersonalTimetabling.Models.OccurancesCollection();
      
      this.view = new PersonalTimetabling.CalendarViews.ColumnsDaysActivitiesView({el: $("#calendar-view"), collection: this.occurances});
    }

    this.render();
  },

  render: function() {
    this.resize();
    if (this.view != null)
      this.view.render();
  },

  resize: function() {
    if (this.view != null)
      this.view.resize();
  },

  schedule_activity_clicked: function(app, e) {
    e.preventDefault();

    var form = this.form;
    
    var activity_type = $(form['activity-type']).filter(":checked").val();
    
    if (activity_type == 'fixed') {
      //var activity = new PT.models.FixedActivity();

      app.activities.create(PersonalTimetabling.Models.Activity.createFixed({
        start: new Date(form["activity-fixed-start-time"].value),
        end: new Date(form["activity-fixed-end-time"].value),
        name: form["activity-name"].value,
        description: form["activity-description"].value,
      }));
      
    } else {
      alert("Not implemented") ;
    }

    
    return false;
  },
});
