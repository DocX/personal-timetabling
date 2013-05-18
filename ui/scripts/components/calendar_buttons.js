// (c) 2013 Lukas Dolezal
"use strict";

define(function(require) {

var $ = require('jquery'),
    Backbone = require('backbone'),
    moment = require('moment');
    
// View controller for calendar buttons
return Backbone.View.extend({

 buttons_template: 
    "<div id='dayincolview-buttons' class='rightcomponents'>" +
      "<div class='btn-group'>" +
        "<a class='btn dropdown-toggle btn-inverse' data-toggle='dropdown'>" +
          "Ka-Zoom-A" +
          "<span class='caret'></span>" +
        "</a>" +
        
        "<ul class='dropdown-menu'>" +
          "<li><div data-role='zoom-slider' ></div></li>" +
          "<li class='divider'></li>" +
          "<li><a href='#' data-role='zoom-to' data-zoom='600'>Days</a></li>" +
          "<li><a href='#' data-role='zoom-to' data-zoom='300'>Weeks</a></li>" +
          "<li><a href='#' data-role='zoom-to' data-zoom='0'>Months</a></li>" +
        "</ul>" +
        "</div>" +

        "<div class='btn-group'>" +
          "<button class='btn btn-inverse active' data-role='mode-horizontal'><i class='icon-white icon-pt-horizontal'></i>H</button>" +
          "<button class='btn btn-inverse' data-role='mode-vertical' ><i class='icon-white icon-pt-vertical'></i>V</button>" +
        "</div>" +        
        
        "<div class='btn-group'>" +
          "<button class='btn btn-inverse' data-role='scroll-left'><i class='icon-white icon-chevron-left'></i></button>" +
          "<button class='btn btn-inverse' data-role='scroll-right'><i class='icon-white icon-chevron-right'></i></button>" +
        "</div>" +
      "</div>",
      
   initialize: function(options) {
      this.$buttons = $(this.buttons_template);
      this.$el.empty().append(this.$buttons);

      this.calendar_view = options.calendar_view;

      this.$zoom_slider = this.$buttons.find("[data-role=zoom-slider]")
      .slider({min: 0,
              max: 899, 
              change:_.bind(this.calendar_view.calendar.set_zoom, this.calendar_view.calendar),
              slide:_.bind(this.calendar_view.calendar.set_zoom,  this.calendar_view.calendar), 
              value:600});

      this.$buttons.find("[data-role=scroll-left]")
         .click(_.bind(this.calendar_view.calendar.move_left, this.calendar_view.calendar));
      this.$buttons.find("[data-role=scroll-right]")
         .click(_.bind(this.calendar_view.calendar.move_right, this.calendar_view.calendar));
      this.$buttons.find("[data-role=zoom-to]")
         .click(_.partial(function(view) {view.set_zoom(null, {value:$(this).attr("data-zoom")})}, this.calendar_view.calendar));
      
      var mode_vertical_btn = this.$buttons.find("[data-role=mode-vertical]");
      var mode_horizontal_btn = this.$buttons.find("[data-role=mode-horizontal]");
      
      mode_vertical_btn.click(_.bind(function() {
        mode_vertical_btn.addClass('active');
        mode_horizontal_btn.removeClass('active');
        this.calendar_view.calendar.set_axis('x');
      }, this));  
      mode_horizontal_btn.click(_.bind(function() {
        mode_vertical_btn.removeClass('active');
        mode_horizontal_btn.addClass('active');
        this.calendar_view.calendar.set_axis('y');
      }, this));  
   },


});


});