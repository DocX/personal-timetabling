module Webui
  module Core
    # load java classes to this module
    
    DomainStack = Rjb::import 'net.personaltt.core.ActionStackDomain'
    BoundedIntervalDomain = Rjb::import 'net.personaltt.core.Interval'
    RepeatingIntervalDomain = Rjb::import 'net.personaltt.core.RepeatingIntervalDomain'
    
    LocalDateTime = Rjb::import 'org.joda.time.LocalDateTime'
    PeriodDays = Rjb::import 'org.joda.time.Days'
    PeriodHours = Rjb::import 'org.joda.time.Hours'
    PeriodMonths = Rjb::import 'org.joda.time.Months'

    module Utils
      def Utils.from_localdatetime (ldt)
        DateTime.new ldt.getYear, ldt.getMonthOfYear, ldt.getDayOfMonth, ldt.getHourOfDay, ldt.getMinuteOfHour, ldt.getSecondOfMinute
      end
      
      def Utils.to_localdatetime (ruby_datetime)
        LocalDateTime.new ruby_datetime.year, ruby_datetime.month, ruby_datetime.day, ruby_datetime.hour, ruby_datetime.minute, ruby_datetime.second
      end
      
      # bridges to RepeatingIntervalDomain.periodIntervals static method
      def Utils.period_intervals start, duration, count 
        j_intervals = RepeatingIntervalDomain.periodsIntervals to_localdatetime(start), duration.to_j, count

        j_list_to_ary(j_intervals) {|i| BoundedInterval.create( Utils.from_localdatetime(i.getStart), Utils.from_localdatetime(i.getEnd)) }
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
    end

    module IntervalsSetBridge
      
      def get_intervals (from, to)
        javainstance = to_j
        
        from_ldt =  Utils.to_localdatetime(from)
        to_ldt =  Utils.to_localdatetime(to)
        
        intervalsset = javainstance.getIntervalsIn BoundedIntervalDomain.new(from_ldt, to_ldt)
        java_intervals = intervalsset.getIntervals
        
        Utils.j_list_to_ary(java_intervals) {|i| BoundedInterval.create( Utils.from_localdatetime(i.getStart), Utils.from_localdatetime(i.getEnd)) }
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
        start = Utils.to_localdatetime(self.start)
        enddate = Utils.to_localdatetime(self.end)
        
        BoundedIntervalDomain.new start, enddate
      end
    end


    
  end
end
  
