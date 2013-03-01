// (c) 2013 Lukas Dolezal

_.extend(Date.prototype, {

   // returns new Date object with added minutes to this
   addMinutes: function(minutes) {
      return new Date(this.getTime() + minutes * 60000);
   
   },
   
   getMidnight: function() {
      return new Date(this.getFullYear(), this.getMonth(), this.getDate(), 0,0,0);
   },
   
   addDays: function(days) {
      return new Date(this.getFullYear(), this.getMonth(), this.getDate()+days, this.getHours(),this.getMinutes(),this.getSeconds());
   },
   
   diffMinutes: function(other) {
      return Math.round((this.getTime() - other.getTime()) / 60000);
   },
    // returns day of week numbered by given start day in sun-sat week
   getDayStarting: function(day) {
       var day = this.getDay() - day;
       return day < 0 ? 7 - day : day;
    },
    getWeekOfYear: function() {
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
        return weekNo;
    }
});

Date.today = function() {
   var now = new Date();
   return new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0,0,0);
   
};

Date.fromWeekDay = function(y, w, d) {
  var DOb = new Date(+y, 0, 4);
  if (isNaN(DOb))
      return false;
  var D = DOb.getDay() || 7; // ISO
  DOb.setDate( DOb.getDate() + 7*(w-1) + (d-D) );
  return DOb;
}
