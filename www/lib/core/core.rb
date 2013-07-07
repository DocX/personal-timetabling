require File.expand_path('../client.rb', __FILE__)
require File.expand_path('../problem_definition.rb', __FILE__)

module PersonalTimetablingAPI
  module Core
    # load java classes to this module
    
    DomainStack = Rjb::import 'net.personaltt.timedomain.ActionStackDomain'
    BoundedIntervalDomain = Rjb::import 'net.personaltt.timedomain.Interval'
    RepeatingIntervalDomain = Rjb::import 'net.personaltt.timedomain.RepeatingIntervalDomain'
    
    LocalDateTime = Rjb::import 'org.joda.time.LocalDateTime'
    PeriodDays = Rjb::import 'org.joda.time.Days'
    PeriodHours = Rjb::import 'org.joda.time.Hours'
    PeriodMonths = Rjb::import 'org.joda.time.Months'
    PeriodMinutes = Rjb::import 'org.joda.time.Minutes'

    module Utils
      ArrayList = Rjb::import 'java.util.ArrayList'

      def Utils.from_localdatetime (ldt)
        DateTime.new ldt.getYear, ldt.getMonthOfYear, ldt.getDayOfMonth, ldt.getHourOfDay, ldt.getMinuteOfHour, ldt.getSecondOfMinute
      end
      
      def Utils.to_localdatetime (ruby_datetime)
        LocalDateTime.new ruby_datetime.year, ruby_datetime.month, ruby_datetime.day, ruby_datetime.hour, ruby_datetime.minute, ruby_datetime.second
      end
      
      # bridges to RepeatingIntervalDomain.periodIntervals static method
      def Utils.period_intervals start, duration, count 
        j_intervals = RepeatingIntervalDomain.periodsIntervals to_localdatetime(start), duration.to_j, count

        j_list_to_ary(j_intervals) {|i| TimeDomains::BoundedTimeDomain.create( Utils.from_localdatetime(i.getStart), Utils.from_localdatetime(i.getEnd)) }
      end

      def Utils.j_list_to_ary (j_list, &block)
        ary = []
        i = 0
        size = j_list.size
        
        while i < size
          item = j_list.get i
          ary << block.call(item)
          i += 1
        end     
        ary   
      end

      def Utils.ary_to_j_arraylist(array)
        ja = ArrayList.new array.length
        array.each do |a|
          ja.add(a)
        end

        ja
      end
    end

    module IntervalsSetBridge
      
      def get_intervals (from, to)
        javainstance = to_j
        
        from_ldt =  Utils.to_localdatetime(from)
        to_ldt =  Utils.to_localdatetime(to)
        
        intervalsset = javainstance.getIntervalsIn BoundedIntervalDomain.new(from_ldt, to_ldt)
        java_intervals = intervalsset.getIntervals
        
        Utils.j_list_to_ary(java_intervals) {|i| TimeDomains::BoundedTimeDomain.create( Utils.from_localdatetime(i.getStart), Utils.from_localdatetime(i.getEnd)) }
      end

      # Connection to isBounded method in IIntervalsTimeDomain iterface in java implementaton
      def is_bounded?
        to_j.isBounded
      end

      def bounding_interval
        interval = to_j.getBoundingInterval
        [ Utils.from_localdatetime(interval.getStart), Utils.from_localdatetime(interval.getEnd) ]
      end
    end
    
    module TimeDomainStackMixin
      include IntervalsSetBridge
      
      def to_j
        java_stack = DomainStack.new
        
        self.actions_stack.reverse_each {|a| java_stack.push a.action, a.time_domain.to_j }
        
        java_stack
      end
    end
    
    # RepeatingIntervalDomain in here
    module BoundlessIntervalRepeatingMixin
      include IntervalsSetBridge
      
      def to_j
        reference = Utils.to_localdatetime(self.reference_start)
        period = self.period.to_j
        duration = self.duration.to_j
        
        RepeatingIntervalDomain.new reference, duration, period
      end
    end
    
    module DurationMixin
      def to_j
        case self.unit
        when TimeDomains::Duration::MINUTE
          return PeriodMinutes.minutes self.duration
        when TimeDomains::Duration::HOUR
          return PeriodHours.hours self.duration
        when TimeDomains::Duration::DAY
          return PeriodDays.days self.duration
        when TimeDomains::Duration::WEEK
          return PeriodDays.days (7 * self.duration)
        when TimeDomains::Duration::MONTH
          return PeriodMonths.months self.duration
        end
      end

      # returns number of whole durations between given partial dates
      def between start_date, end_date
        j_unit = self.unit
        j_duration = self.duration
        if j_unit == TimeDomains::Duration::WEEK
          j_unit = TimeDomains::Duration::DAY
          j_duration *= 7
        end
        unit_name = TimeDomains::Duration::units[j_unit] + 's';
        duration_j_class = "PersonalTimetablingAPI::Core::Period#{unit_name}".constantize
        (duration_j_class.send("#{unit_name.downcase}Between", Utils.to_localdatetime(start_date), Utils.to_localdatetime(end_date)).send("get#{unit_name.capitalize}") / Float(j_duration)).floor
      end
    end
    
    module BoundedIntervalMixin
      include IntervalsSetBridge
      
      def to_j
        start = Utils.to_localdatetime(self.start)
        enddate = Utils.to_localdatetime(self.end)
        
        BoundedIntervalDomain.new start, enddate
      end

      def after_duration duration
        j_duration = duration.to_j

        start = Utils.from_localdatetime( Utils.to_localdatetime(self.start).plus j_duration )
        enddate = Utils.from_localdatetime( Utils.to_localdatetime(self.end).plus j_duration )

        TimeDomains::BoundedTimeDomain.create start, enddate
      end
    end


    
  end
end
  
