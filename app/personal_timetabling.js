// (c) 2013 Lukas Dolezal

PersonalTimetabling.App = Backbone.View.extend({

   initialize: function() {

      this.activities = new PersonalTimetabling.Models.ActivityCollection();

      $("[data-submit=schedule-activity]").click(_.partial(this.schedule_activity_clicked, this));
      
      this.calendar_view = new PersonalTimetabling.CalendarViews.VerticalDayView({el: $("#content"), collection: this.activities});
      this.$topbar = $("#topbar");

      $(window).resize(function(that) { return function() {that.resize();} } (this));
      this.render();

      //this.listenTo(this.activities, 'change', this.calendar_view.update_data_view);
      
      this.activities.fetch({success: function(collection) {
        // this is only for example
        if (collection.length == 0) {
          collection.create({
            name: "Lunch",
            description:"Lunch at Burger King",
            occurance: new PT.Models.ActivityOccurance({start: new Date("2013/03/24 11:15:00"), end: new Date("2013/03/24 13:15:00")})});
          collection.create({
            name: "Lunch",
            description:"Lunch at Vegan bistro",
            occurance: new PT.Models.ActivityOccurance({start: new Date("2013/03/25 11:15:00"), end: new Date("2013/03/25 13:15:00")})});
          collection.create({
            name: "Something",
            occurance: new PT.Models.ActivityOccurance({start: new Date("2013/03/28 19:35:00"), end: new Date("2013/03/28 21:15:00")})});
          }
      } } );
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

        app.activities.create({
          start: new Date(form["activity-fixed-start-time"].value),
          end: new Date(form["activity-fixed-end-time"].value),
          name: form["activity-name"].value,
          description: form["activity-description"].value,
        });
        
      } else {
        alert("Not implemented") ;
      }

      
      return false;
   }
});
