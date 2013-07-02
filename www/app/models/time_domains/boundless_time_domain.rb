# Boundless interval repeating describes refernce interval and repeating characteristics. It actually shrinks
# the infinity timeline into repeating circle and all describes one circle sector of it.

module TimeDomains
class BoundlessTimeDomain < BaseTimeDomain
  include PersonalTimetablingAPI::Core::BoundlessIntervalRepeatingMixin
  
  # reference start is absolute datetime defining start of the circle
  # duration is definition of circle sector size
  # period is definition of the circle circumference from one start to the next. Theoretically, duration can overlap circumference of the period. 
  attr_accessor :reference_start, :duration, :period
  
  def self.from_attributes(attributes)
    interval = self.new

    if attributes[:from].is_a? String 
      interval.reference_start = DateTime.iso8601 attributes[:from]
    else
      interval.reference_start = DateTime.new attributes[:'from(1i)'].to_i, Integer(attributes[:'from(2i)'].to_i), Integer(attributes[:'from(3i)'].to_i), Integer(attributes[:'from(4i)'].to_i), Integer(attributes[:'from(5i)'].to_i)
    end

    interval.duration = Duration.new attributes[:duration]
    interval.period = Duration.new attributes[:period]
    
    interval
  end

  def encode_with coder
    coder['reference_start'] = @reference_start
    coder['duration'] = @duration
    coder['period'] = @period
  end

  def init_with coder
    @reference_start = coder['reference_start']
    @duration = coder['duration']
    @period = coder['period']
  end

  def to_hash
    {
      :type => 'boundless',
      :data => {
        :duration => self.duration.to_hash,
        :period => self.period.to_hash
      }
    }
  end

end

end