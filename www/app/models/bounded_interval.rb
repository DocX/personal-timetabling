# Bounded interval describes one particular interval in the universe timeline.

class BoundedInterval < TimeDomain
  include Webui::Core::BoundedIntervalMixin
  
  attr_accessor :start, :end

  def initialize
    @start = DateTime.now
    @end = DateTime.now + 1
  end
  
  def self.create (start, to)
    b = BoundedInterval.new
    b.start = start
    b.end = to
    b
  end

  def self.from_attributes(attributes)
    interval = self.new

    if attributes['from']
      interval.start = DateTime.iso8601 attributes['from']
    else
      interval.start = DateTime.new Integer(attributes['from(1i)'].to_i), Integer(attributes['from(2i)'].to_i), Integer(attributes['from(3i)'].to_i), Integer(attributes['from(4i)'].to_i), Integer(attributes['from(5i)'].to_i)
    end

    if attributes['to']
      interval.end = DateTime.iso8601 attributes['to']
    else
      interval.end = DateTime.new Integer(attributes['to(1i)'].to_i), Integer(attributes['to(2i)'].to_i), Integer(attributes['to(3i)'].to_i), Integer(attributes['to(4i)'].to_i), Integer(attributes['to(5i)'].to_i)
    end

    interval
  end
  
  def to_s
    @start.to_s + ' - ' + @end.to_s
  end

  # returns number of days covering interval
  def bounding_days
      (self.end - self.start).ceil
  end

  def seconds 
    ((self.end - self.start) * 86400).ceil
  end

  def encode_with coder
    coder['start'] = @start
    coder['end'] = @end
  end

  def init_with coder
    @start = coder['start']
    @end = coder['end']
  end
end