// (c) 2013 Lukas Dolezal
"use strict";

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.ColumnsDaysActivitiesView = PersonalTimetabling.CalendarViews.ColumnsDaysView.extend({

  activity_template: 
    "<div data-type='activity-occurance' class='activity-occurance'>"+
      "<div class='activity-occurance-inner'>" +
        "<div class='columnbox-handle-front activity-occurance-handle'></div>" +
        "<div class='columnbox-handle-back activity-occurance-handle'></div>" +
        "<div class='activity-occurance-labels'>" +
          "<div class='name' data-source='name'></div>" +
          "<div class='time'>" +
            "<span class='start' data-source='start'></span> - " +
            "<span class='end' data-source='end'></span>" +
          "</div>" +
        "</div>" +
      "</div>" +
    "</div>",
 
  activity_date_format: 'DD.MM.YY H:mm',
    
  initialize: function() {
    
    PersonalTimetabling.CalendarViews.ColumnsDaysView.prototype.initialize.apply(this);
    
    this.listenTo(this.collection, 'sync', this.update_data_view);
    this.listenTo(this, 'columns_updated', this.fetch_data_view);
  },

  fetch_data_view: function() {
    this.$grid_overlay_el.find("[data-type='activity-occurance']").remove();

    var range = this.showing_dates();
    this.collection.fetchRange(range.start, range.end);
  },
  
  // renders activities
  update_data_view: function() {    
    var range = this.showing_dates();

    var occurances_to_show = this.collection.inRange(
      range.start, range.end
    );

    for(var io = 0; io < occurances_to_show.length; io++) {
      var occurance = occurances_to_show[io];
      if (occurance.get('activity') == null)
        continue;
      this.add_activity_box(occurance);
    }
  },
  
  add_activity_box: function(occurance) {
    // determine column
    var start_coord = this.geometry.get_line_of_date(occurance.get("start"));
    var end_coord = this.geometry.get_line_of_date(occurance.get("end"));

    // find column for start
    var column = this.drawing_columns_list.find(function(k) {return k.column_id == start_coord.column_id;}); 
    if (column == undefined)
      return;
    
    var box = $(this.activity_template).data({
      'occurance': occurance,
      'column': column,
      'start_date': occurance.get('start'),
      'duration': occurance.get('duration'),
    });
    box.css(this.columns_size_attr, this.drawing_column_width);
    box.css(this.columns_offset_attr, column.$column.position()[this.columns_offset_attr]);
    box.find('[data-source=name]').text(occurance.get("activity").get("name")); 
    box.find('[data-source=start]').text(occurance.get("start").format(this.activity_date_format));
    box.find('[data-source=end]').text( occurance.get("end").format(this.activity_date_format));
    
    this.set_box_offset_and_size_for_column(box, start_coord.line, end_coord.line - start_coord.line);
    
    this.$grid_overlay_el.append(box);
    /*
    box.draggable({
      grid:[this.drawing_column_line_height/this.column_line_steps, this.drawing_column_line_height/this.column_line_steps], 
      containment: "parent", 
      axis: this.axis == 'y' ? 'x' : 'y',
      drag: _.bind(this.activity_box_moved,this),
      stop: _.bind(this.activity_box_moving_stop, this)
    });     
    */
    box.column_box({
      view: this,
      orientation: this.axis == 'x' ? 'y' : 'x',
      steps: this.column_step_minutes,
      drag: _.bind(this.activity_box_moved,this),
      stop: _.bind(this.activity_box_moving_stop, this)
    });
  },
    
  activity_box_moved: function(e, ui) {
    var $box = $(ui.el);
    var occurance = $box.data('occurance');
    var column = $box.data('column');
    
    occurance.set({
      start: ui.date_front,
      duration: ui.duration
    });
    
    $box.find('[data-source=start]').text(ui.date_front.format(this.activity_date_format));
    $box.find('[data-source=end]').text( occurance.get('end').format(this.activity_date_format));
    
    // align to new date time
    return true;
  },
  
  activity_box_moving_stop: function(e, ui) {
    var $box = $(ui.el);
    var occurance = $box.data('occurance');

    occurance.save();
  }

});
