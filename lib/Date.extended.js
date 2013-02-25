// (c) 2013 Lukas Dolezal

_.extend(Date.prototype, {

   // returns new Date object with added minutes to this
   addMinutes: function(minutes) {
      return new Date(this.getTime() + minutes * 60000);
   
   },
   
   getNoon: function() {
      return new Date(this.getFullYear(), this.getMonth(), this.getDate(), 0,0,0);
   },
   
   addDays: function(days) {
      return new Date(this.getFullYear(), this.getMonth(), this.getDate()+days, this.getHours(),this.getMinutes(),this.getSeconds());
   },
   
   diffMinutes: function(other) {
      return Math.round((this.getTime() - other.getTime()) / 60000);
   },
});

