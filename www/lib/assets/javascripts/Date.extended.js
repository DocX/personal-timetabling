// (c) 2013 Lukas Dolezal

// PROTOTYPE (OBJECT) METHODS

Date.prototype.getMidnight = function() {
  return new Date(this.getFullYear(), this.getMonth(), this.getDate(), 0,0,0);
};


// returns day of week numbered by given start day in sun-sat week
Date.prototype.getDayStarting = function(day) {
    var day = this.getDay() - day;
    return day < 0 ? 7 + day : day;
};

// returns ISO8601 week number of year of the date
Date.prototype.getWeekOfYear = function() {
    // Copy date so don't modify original
    d = new Date(this);
    d.setHours(0,0,0);
    // Set to nearest Thursday: current date + 4 - current day number
    // Make Sunday's day number 7
    d.setDate(d.getDate() + 4 - (d.getDay()||7));
    // Get first day of year
    var yearStart = new Date(d.getFullYear(),0,1);
    // Calculate full weeks to nearest Thursday
    var weekNo = Math.ceil(( ( (d - yearStart) / 86400000) + 1)/7)
    // Return array of year and week number
    return {week: weekNo, year: d.getFullYear(), text: weekNo + '/' + d.getFullYear()};
};

Date.prototype.diff_partial = function(unit, other){
  // compute count of units in column
  switch(unit){
    case 'HOUR':
      return this.diffMinutes(other) / 60;
    case 'DAY':
      // if in interval is 23 hour day, it will be slightly less than actual amount of day, if 25 hour day, slihtly more
      // so round it
      return Math.round(this.getMidnight().diffMinutes(other.getMidnight()) / 1440) +
      ((this.getMidnight().diffMinutes(this)) + (other.diffMinutes(other.getMidnight()))) / 1440;
    case 'WEEK':
      return this.diff_partial('DAY', other) / 7;
  }
};

// CLASS METHODS

// returns current's day 0:00
Date.today = function() {
   var now = new Date();
   return new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0,0,0);
   
};

// returns date corresponding to given year, week and day in week
Date.fromWeekDay = function(y, w, d) {
  var DOb = new Date(+y, 0, 4);
  if (isNaN(DOb))
      return false;
  var D = DOb.getDay() || 7; // ISO
  DOb.setDate( DOb.getDate() + 7*(w-1) + (d-D) );
  return DOb;
}
