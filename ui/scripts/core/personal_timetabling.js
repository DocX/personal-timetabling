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

    $("a[data-role=add_domain_template]").click(_.bind(function(){
      //open sidebar
      this.open_panel(PersonalTimetabling.Views.DomainTemplateEditor, {
          calendar_view: this.calendar_view.calendar
        });
    }, this));

    $("a[data-role=add_activity]").click(_.bind(function(){
      //open sidebar
      this.open_panel(PersonalTimetabling.Views.NewActivityPanel, {});
    }, this));


    this.render();
  },

  open_panel: function(view_class, options) {
    if (this.sidebar) {
      this.sidebar.remove();
    }

    this.layout.addClass('panel-open');

    $.extend(options, {el: $("<div class='fill'/>").appendTo(this.layout.find("#sidepanel"))});

    this.sidebar = new view_class(options);
    this.listenTo(this.sidebar, 'removed', this.hide_panel);
  },

  hide_panel: function() {
    this.layout.removeClass('panel-open')    
  },

  render: function() {
    

    this.resize();
  },

  resize: function() {
  },
});
