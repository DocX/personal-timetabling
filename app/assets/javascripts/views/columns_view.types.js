

// represents view geometry. translates view columns/lines to/from dates and handles column labels
PersonalTimetabling.CalendarViews.ColumnsView.TimeColumnGeometryBase = Base.extend( {
  
  
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
    var date = this.get_offset_column(from_id, offset);
    
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
  
  // gets column which is number of columns apart from given column
  get_offset_column: function(column, offset) {
    var date = column;
    var direction = offset < 0 ? -1 : 1;
    offset = offset * direction;
    while(offset>0) {
      offset -= 1;
      date = direction > 0 ? this.next_column(date) : this.prev_column(date);
    }
    return date;
  },
  
  /* abstract protected methods */
  
  next_column: function(col_id) {},
                                                                                        
  prev_column: function(col_id) {},
  
  column_spec: function(col_id) {},
  
  week_days: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
  
  months: ["January", "February", "March", "April", "May", "June", "Jule", "August", "October", "November", "December"],
});

PersonalTimetabling.CalendarViews.ColumnsView.DayColumnGeometry = PT.CalendarViews.ColumnsView.TimeColumnGeometryBase.extend({
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
  
  next_column: function(date) { return moment.utc(date).add('d',1).valueOf(); },
  
  prev_column: function(date) { return moment.utc(date).add('d',-1).valueOf(); },
  
  column_spec: function(date_id) {
    var date = moment.utc(date_id).startOf('day');
    var sun_or_sat = ((date.day() +6) % 7) >= 5;
    return {
        id: date_id,
        title: date.format("ddd D. M. YYYY"),
        lines_labels: this.lines_prototype.map(function(el) {return {label: el, style: sun_or_sat ? 'shaded' : ''};})
      };
  },
  
  get_super_columns: function(column_start_id, columns) {
    // get left edge of column on the left edge of first column
    column_start_id = moment.utc(column_start_id);
    var left_date = column_start_id.clone().startOf('week').day(1);
    
    var end_date = column_start_id.clone().add('d', columns);
    supercols = [];
    
    while(end_date.isAfter(left_date)) {
      var week = left_date.clone().day(1);
      supercols.push({
        supercol_start_column: left_date.valueOf(),
        start_part: 0,
        label: "Week " + week.isoWeek()  + "/" + week.year()});
      
      left_date = left_date.add('w',1);
    }
    
    return supercols;
  },
  
  get_line_of_date: function(date) {
    return {column_id: date.clone().startOf('day').valueOf(), line: date.hour() + (date.minute()/60)};
  },
  
  get_date_of_line: function(column_id, line, round_to_minutes) {
    var date = moment.utc(column_id);
    
    if (!round_to_minutes)
      round_to_minutes = 1;
    
    minutes_since_zero = Math.round((line * 60) / round_to_minutes) * round_to_minutes;
    
    date.hours(Math.floor(minutes_since_zero / 60))
    date.minutes(minutes_since_zero % 60);
    return date;
  }
});



PersonalTimetabling.CalendarViews.ColumnsView.WeekColumnGeometry = PT.CalendarViews.ColumnsView.TimeColumnGeometryBase.extend({
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
    // get monday of the week in which date is
    var week_start = date.clone().startOf('week').day(1);
    
    return {column_id: week_start.valueOf(), line: date.diff(week_start, 'hours', true) / 24};
  },
  
  next_column: function(date) { return moment.utc(date).add('w',1).valueOf(); },
  
  prev_column: function(date) { return moment.utc(date).add('w',-1).valueOf(); },
  
  column_spec: function(date_id) {
    var date = moment.utc(date_id);
    
    var lines = [];
    var line_date = date.clone();
    for(var i = 0; i<7; i++) {
      var sun_or_sat = ((line_date.day() +6) % 7) >= 5;
      lines.push({label: line_date.format("ddd D.M."), style: sun_or_sat ? 'shaded' : ''});
      line_date.add('d',1);
    }
    var week = date.isoWeek();
    return {
        id: date_id,
        title: (week + "/" + date.day(4).year()),
        lines_labels: lines
      };
  },
  
  get_super_columns: function(column_start_id, columns) {
    // get left edge of column on the left edge of first column
    var column_date = moment.utc(column_start_id);
    var left_date = column_date.clone().startOf('month').day(1);
    
    var end_date = column_date.clone().add('w',columns);
    supercols = [];
    
    while(end_date.isAfter(left_date)) {
      // get months first day week - should be sure before left_date
      var week = left_date.clone().day(1);
    
      supercols.push({
        supercol_start_column: week.valueOf(),
        start_part: left_date.diff(week, 'weeks', true),
        label: left_date.format("MMM YYYY")});
      
      left_date.add('month', 1);
    }
    
    return supercols;
  },
  
  
  
  get_date_of_line: function(column_id, line, round_to_minutes) {
    var date = moment.utc(column_id);
    
    if (!round_to_minutes)
      round_to_minutes = 1;
    
    date.add('minutes', Math.round(line * 1440 / round_to_minutes) * round_to_minutes);
    return date;
  }
});