// (c) 2013 Lukas Dolezal

PersonalTimetabling.App = Backbone.View.extend({

  initialize: function() {

    this.occurances = new PersonalTimetabling.Models.OccurancesCollection();

    $("[data-submit=schedule-activity]").click(_.partial(this.schedule_activity_clicked, this));
    
    this.calendar_view = new PersonalTimetabling.CalendarViews.ColumnsDaysActivitiesView({el: $("#content"), collection: this.occurances});
    this.$topbar = $("#topbar");

    $(window).resize(function(that) { return function() {that.resize();} } (this));
    this.render();

    $(document).on('ajaxStart', function() {  $("#ajax-indicator").show(); });
    $(document).on('ajaxStop', function() {  $("#ajax-indicator").hide(); });
  },

  render: function() {
    this.resize();
    this.calendar_view.render();
  },

  resize: function() {
    this.calendar_view.resize();
  },
   
  request_start: function(model, xhr, options) {
    console.log('reuqest started');
    var current_ref = this.request_ref;
    $("#ajax-indicator").data('ref', this.request_ref).show();
    xhr.always(
      function() {if ($("#ajax-indicator").data('ref') == current_ref) $("#ajax-indicator").hide();}
    );
    this.request_ref++;
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
