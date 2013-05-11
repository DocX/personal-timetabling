// (c) 2013 Lukas Dolezal

PersonalTimetabling.App = Backbone.View.extend({

   view_template:
    "<div class='panel-layout'>" + 
      "<div id='sidepanel'></div>" +
      "<div id='mainview'></div>" +
    "</div>"
    ,
      
  
  initialize: function() {
    $(window).resize(function(that) { return function() {that.resize();} } (this));

    $(document).on('ajaxStart', function() {  $("#ajax-indicator").show(); });
    $(document).on('ajaxStop', function() {  $("#ajax-indicator").hide(); });
    
    this.layout = $(this.view_template);
    $("#content").append(this.layout);

    this.calendar_view = new PersonalTimetabling.CalendarViews.ColumnsDaysActivitiesView({el: this.layout.find("#mainview")});
    this.calendar_buttons = new PersonalTimetabling.Views.CalendarButtons({el: '#content-panel-place', calendar_view: this.calendar_view});
    
    this.sidebar = null;

    $("a[href='#schedule-activity']").click(_.bind(function(){
      //open sidebar
      this.layout.toggleClass('panel-open');
      if (this.layout.hasClass('panel-open')) {
        this.sidebar = new PersonalTimetabling.Views.DomainTemplateEditor({
          el: $("<div class='fill'/>").appendTo(this.layout.find("#sidepanel")),
          calendar_view: this.calendar_view.calendar
        });
      } else {
        this.sidebar.remove();
      }
    }, this));

    this.render();
  },

  render: function() {
    

    this.resize();
  },

  resize: function() {
  },
});
