module Webui
  module Core
    # load java classes to this module
    
    DomainStack = Rjb::import 'net.personaltt.core.ActionStackDomain'
    RepeatingIntervalDomain = Rjb::import 'net.personaltt.core.RepeatingIntervalDomain'
    BoundedIntervalDomain = Rjb::import 'net.personaltt.core.Interval'
    
    LocalDateTime = Rjb::import 'org.joda.time.LocalDateTime'
    PeriodDays = Rjb::import 'org.joda.time.Days'
    PeriodHours = Rjb::import 'org.joda.time.Hours'
    PeriodMonths = Rjb::import 'org.joda.time.Months'
    

    module IntervalsSetBridge
      def self.from_localdatetime (ldt)
        DateTime.new ldt.getYear, ldt.getMonthOfYear, ldt.getDayOfMonth, ldt.getHourOfDay, ldt.getMinuteOfHour, ldt.getSecondOfMinute
      end
      
      def self.to_localdatetime (ruby_datetime)
        LocalDateTime.new ruby_datetime.year, ruby_datetime.month, ruby_datetime.day, ruby_datetime.hour, ruby_datetime.minute, ruby_datetime.second
      end
      
      def get_intervals (from, to)
        javainstance = to_j
        
        from_ldt =  IntervalsSetBridge.to_localdatetime(from)
        to_ldt =  IntervalsSetBridge.to_localdatetime(to)
        
        intervalsset = javainstance.getIntervalsIn BoundedIntervalDomain.new(from_ldt, to_ldt)
        java_intervals = intervalsset.getIntervals
        
        intervals = []
        i = 0
        size = java_intervals.size
        while i < size
          interval = java_intervals.get i
          intervals << BoundedInterval.create( IntervalsSetBridge.from_localdatetime(interval.getStart), IntervalsSetBridge.from_localdatetime(interval.getEnd))
          
          i += 1
        end
        
        intervals
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
    
    module BoundlessIntervalRepeatingMixin
      include IntervalsSetBridge
      
      def to_j
        reference = IntervalsSetBridge.to_localdatetime(self.reference_start)
        period = self.period.to_j
        duration = self.duration.to_j
        
        RepeatingIntervalDomain.new reference, duration, period
      end
    end
    
    module DurationMixin
      def to_j
        case self.unit
        when Duration::HOUR
          return PeriodHours.hours self.duration
        when Duration::DAY
          return PeriodDays.days self.duration
        when Duration::WEEK
          return PeriodDays.days (7 * self.duration)
        when Duration::MONTH
          return PeriodMonths.months self.duration
        end
      end
    end
    
    module BoundedIntervalMixin
      include IntervalsSetBridge
      
      def to_j
        start = IntervalsSetBridge.to_localdatetime(self.start)
        enddate = IntervalsSetBridge.to_localdatetime(self.end)
        
        BoundedIntervalDomain.new start, enddate
      end
    end
    
  end
end
  
