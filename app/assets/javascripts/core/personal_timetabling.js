// (c) 2013 Lukas Dolezal

PersonalTimetabling.App = Backbone.View.extend({

   initialize: function() {

      this.activities = new PersonalTimetabling.Models.ActivityCollection();

      $("[data-submit=schedule-activity]").click(_.partial(this.schedule_activity_clicked, this));
      
      this.calendar_view = new PersonalTimetabling.CalendarViews.ColumnsDaysActivitiesView({el: $("#content"), collection: this.activities});
      this.$topbar = $("#topbar");

      $(window).resize(function(that) { return function() {that.resize();} } (this));
      this.render();

      //this.listenTo(this.activities, 'change', this.calendar_view.update_data_view);
      
      this.activities.fetch({success: function(activities) {
        // this is only for example
        if (activities.length == 0) {
          activities.create(PersonalTimetabling.Models.Activity.createFixed({
            name: "Lunch 01",
            description:"Lunch at Burger King",
            start: new Date().beginningOfDay().addHours(11).addMinutes(15),
            end: new Date().beginningOfDay().addHours(13).addMinutes(00)
          }));
          activities.create(PersonalTimetabling.Models.Activity.createFixed({
            name: "Lunch 02",
            description:"Lunch at Burger King",
            start: new Date().beginningOfDay().addDays(1).addHours(11).addMinutes(15),
            end: new Date().beginningOfDay().addDays(1).addHours(13).addMinutes(00)
          }));
        };
      }});
   },

   render: function() {
      this.resize();
      this.calendar_view.render();
   },

   resize: function() {
      this.calendar_view.resize();
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
   }
});
