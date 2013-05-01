# Boundless interval repeating describes refernce interval and repeating characteristics. It actually shrinks
# the infinity timeline into repeating circle and all describes one circle sector of it.

class BoundlessIntervalRepeating < TimeDomain

  # reference start is absolute datetime defining start of the circle
  # duration is definition of circle sector size
  # period is definition of the circle circumference from one start to the next. Theoretically, duration can overlap circumference of the period. 
  attr_accessor :reference_start, :duration, :period
  
  def self.from_attributes(attributes)
    interval = self.new
    interval.reference_start = DateTime.new attributes['from(1i)'].to_i, Integer(attributes['from(2i)'].to_i), Integer(attributes['from(3i)'].to_i), Integer(attributes['from(4i)'].to_i), Integer(attributes['from(5i)'].to_i)
    
    interval.duration = Duration.new attributes['duration']
    interval.period = Duration.new attributes['period']
    
    interval
  end

end