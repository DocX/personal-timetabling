// (c) 2013 Lukas Dolezal

PersonalTimetabling.App = Backbone.View.extend({

   initialize: function() {
      this.calendar_view = new PersonalTimetabling.CalendarViews.TimelineView({el: $("#calendarview")});
      this.$topbar = $("#topbar");

      $(window).resize(function(that) { return function() {that.resize();} } (this));
      this.render();
   },

   render: function() {
      this.resize();
      this.calendar_view.render();
      var onchange = _.bind(function(e,ui) {this.calendar_view.setZoom(ui.value);}, this);
      $("#zoom").slider({min: 1, max: 1000, change:onchange, slide:onchange});
   },

   resize: function() {
      var window_h = $(window).height();
      var topbar_h = this.$topbar.outerHeight();
      this.calendar_view.$el.height(window_h - topbar_h);
      this.calendar_view.resize();
   }


});