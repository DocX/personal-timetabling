

// represents view geometry. translates view columns/lines to/from dates and handles column labels
PersonalTimetabling.CalendarViews.VerticalDayView.TimeColumnGeometryBase = Base.extend( {
  
  
  constructor: function(view, model) {
    this.view = view;
    this.model = model;
  },

  // returns view mode's global geometry, immutable by the time
  // geometry = {column_max_lines: number, supercolum_max_cols: number}
  get_global_geometry: function() {  },
  
  // returns number column specification to the right from position corresponding to from_id + offset columns
  // column specifications = [{id: id, title: "", lines_labels: ["", "", ...]}, ...]
  get_columns: function(number, from_id, offset) {
    // id is column's top edge Date
    
    // go to first date
    var date = from_id;
    var direction = offset < 0 ? -1 : 1;
    offset = offset * direction;
    while(offset>0) {
      offset -= 1;
      date = direction > 0 ? this.next_column(date) : this.prev_column(date);
    }
    
    var columns = [];
    while(number-- > 0) {
      columns.push(this.column_spec(date));
      date = this.next_column(date);
    }
    return columns;
  },
  
  // return supercolumns grouping columns between given bounds.
  // supercolumns are specified as array of their starts in column_id and part of that column
  // supercolumns = [{supercol_start_column: id, start_part: part, label: ""}, ...]
  get_super_columns: function(column_start_id, columns) { },
  
  get_date_of_line: function(column_id, line) {
    
  },
  
  // returns line coordinates = {column_id: id, line: number}
  get_line_of_date: function(date) { },
  
  // get date representation of center of given column
  get_center_date: function(column_id) {
    
  },
  
  /* abstract protected methods */
  
  next_column: function(col_id) {},
                                                                                        
  prev_column: function(col_id) {},
  
  column_spec: function(col_id) {},
  
  week_days: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
  
  months: ["January", "February", "March", "April", "May", "June", "Jule", "August", "October", "November", "December"],
});

PersonalTimetabling.CalendarViews.VerticalDayView.DayColumnGeometry = PT.CalendarViews.VerticalDayView.TimeColumnGeometryBase.extend({
  constructor: function(view, model,  hours_lower, hours_upper) {
    this.base(view, model);
    
  }, 
  
  lines_prototype: function() {
    var lines = [], count = 24, line_hour = 0;
    while(count-- > 0) {
      lines.push(Math.floor(line_hour).pad(2) + "h");
      line_hour += 1;
    }
    return lines;
  }(),
  
  global_geometry:  {
    column_max_lines : 24,
    supercolum_min_cols: 7,
  },

  // returns view mode's global geometry, immutable by the time
  // geometry = {column_max_lines: number, supercolum_max_cols: number}
  get_global_geometry: function() {
    return this.global_geometry;
  },
  
  next_column: function(date) { return new Date(date).addDays(1).getTime(); },
  
  prev_column: function(date) { return new Date(date).addDays(-1).getTime(); },
  
  column_spec: function(date_id) {
    var date = new Date(date_id);
    return {
        id: date_id,
        title: date.format("{Dow} {d}. {M}. {yyyy}"),
        lines_labels: this.lines_prototype           
      };
  },
  
  get_super_columns: function(column_start_id, columns) {
    // get left edge of column on the left edge of first column
    column_start_id = new Date(column_start_id);
    var left_date = column_start_id.clone().beginningOfWeek().addDays(1);
    
    var end_date = column_start_id.clone().addDays(columns);
    supercols = [];
    
    while(end_date > left_date) {
      var week = left_date.getWeekOfYear();
      supercols.push({
        supercol_start_column: left_date.getTime(),
        start_part: 0,
        label: "Week " + week.week  + "/" + week.year});
      
      left_date = left_date.addDays(7);
    }
    
    return supercols;
  },
  
  get_line_of_date: function(date) {
    return {column_id: date.getMidnight().getTime(), line: date.getHours() + (date.getMinutes()/60)};
  },
  
  get_date_of_line: function(column_id, line) {
    var date = new Date(column_id);
    date.setHours(Math.ceil(line))
    date.setMinutes(Math.round(line * 60) % 60);
    return date;
  }
});



PersonalTimetabling.CalendarViews.VerticalDayView.WeekColumnGeometry = PT.CalendarViews.VerticalDayView.TimeColumnGeometryBase.extend({
  constructor: function(view, model) {
    this.base(view, model);
    
  }, 
  
  global_geometry:  {
    column_max_lines : 7,
    supercolum_min_cols: 4,
  },

  // returns view mode's global geometry, immutable by the time
  // geometry = {column_max_lines: number, supercolum_max_cols: number}
  get_global_geometry: function() {
    return this.global_geometry;
  },
  
  get_line_of_date: function(date) {
    var week_start = date.clone().beginningOfWeek().addDays(1);
    if (week_start > date){
      week_start.addDays(-7);
    }
    return {column_id: week_start.getTime(), line: date.minutesSince(week_start) / 1440};
  },
  
  next_column: function(date) { return new Date(date).addDays(7).getTime(); },
  
  prev_column: function(date) { return new Date(date).addDays(-7).getTime(); },
  
  column_spec: function(date_id) {
    var date = new Date(date_id);
    
    var lines = [];
    var line_date = date.clone();
    for(var i = 0; i<7; i++) {
      lines.push(line_date.format("{Dow} {d}.{M}."));
      line_date.addDays(1);
    }
    var week = date.getWeekOfYear();
    return {
        id: date_id,
        title: (week.week + "/" + week.year),
        lines_labels: lines
      };
  },
  
  get_super_columns: function(column_start_id, columns) {
    // get left edge of column on the left edge of first column
    var column_date = new Date(column_start_id);
    var left_date = column_date.beginningOfMonth();
    
    var end_date = left_date.clone().addDays(7*columns);
    supercols = [];
    
    while(end_date > left_date) {
      // get months first day week - should be sure before left_date
      var week = left_date.clone().beginningOfWeek().addDays(1);
      if (week > left_date) {
        week.addDays(-7);
      }
    
      supercols.push({
        supercol_start_column: week.getTime(),
        start_part: (week.minutesUntil(left_date) / (7*1440)),
        label: left_date.format("{Month} {year}")});
      
      left_date.addMonths(1);
    }
    
    return supercols;
  },
  
  
  
  get_date_of_line: function(column_id, line) {
    var date = new Date(column_id);
    date.addMinutes(line * 1440);
    return date;
  }
});