// (c) 2013 Lukas Dolezal
"use strict";

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.ColumnsDaysActivitiesView = PersonalTimetabling.CalendarViews.ColumnsDaysView.extend({

  activity_template: 
    "<div data-type='activity-occurance' class='activity-occurance'>"+
      "<div class='name' data-source='name'></div>" +
      "<div class='time'>" +
        "<span class='start' data-source='start'></span>" +
        "<span class='end' data-source='end'></span>" +
      "</div>" +
    "</div>",
 
  initialize: function() {
    
    PersonalTimetabling.CalendarViews.ColumnsDaysView.prototype.initialize.apply(this);
    
    this.listenTo(this.collection, 'add', this.update_data_view);
    
    this.listenTo(this, 'columns_updated', this.update_data_view);
    
  },

  // renders activities
  update_data_view: function() {

    this.$grid_el.find("[data-type='activity-occurance']").remove();
    this.visible_occurances_boxes = {};
    
    var first_date = this.drawing_columns_list.first().column_id;
    var last_date =  this.drawing_columns_list.last().column_id;

    var activities_to_show = this.collection.withOccurancesInRange(
      this.geometry.get_date_of_line(first_date,0), 
      this.geometry.get_date_of_line(last_date, this.drawing_columns_list.last().lines_labels.length)
    );

    for(var i = 0; i < activities_to_show.length; i++) {
      var activity = activities_to_show[i];
      var activity_occurances = activity.getOccurancesInRange(first_date, last_date);

      for(var io = 0; io < activity_occurances.length; io++) {
        var occurance = activity_occurances.at(io);
        this.add_activity_box(occurance);
      }
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
    
    var box = $(this.activity_template)
      .css("top", (start_coord.line*this.drawing_column_line_height) + "px")
      // TODO support over column activities
      .css("height", (end_coord.line - start_coord.line) * this.drawing_column_line_height + "px")
      .data('occurance', occurance)
      .data('column', column);
      box.find('[data-source=name]').text(occurance.get("activity.name")); 
      box.find('[data-source=start]').text(occurance.get("start"));
      box.find('[data-source=end]').text( occurance.get("end"));
      
    column.$column.append(box);
    this.visible_occurances_boxes[occurance.id] = box;
    
    box.draggable({
      grid:[this.drawing_column_line_height/4, this.drawing_column_line_height/4], 
      containment: "parent", 
      axis: "y",
      drag: _.bind(this.activity_box_moved,this),
      stop: _.bind(this.activity_box_moving_stop, this)
    });      
  },
    
  activity_box_moved: function(e, ui) {
    var $box = $(ui.helper);
    var occurance = $box.data('occurance');
    var column = $box.data('column');
    
    // get date of current lines
    var start_date = this.geometry.get_date_of_line(column.column_id, ui.position.top / this.drawing_column_line_height);
    var end_date = start_date.clone().advance(occurance.get('end').millisecondsSince(occurance.get('start')));
    
    occurance.set({
      start: start_date,
      end: end_date});
    
    $box.find('[data-source=start]').text(start_date);
    $box.find('[data-source=end]').text( end_date);
  },
  
  activity_box_moving_stop: function(e, ui) {
    var $box = $(ui.helper);
    var occurance = $box.data('occurance');

    occurance.save();
  }

});
