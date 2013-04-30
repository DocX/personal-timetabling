# Bounded interval describes one particular interval in the universe timeline.

class BoundedInterval < TimeDomain

  attr_accessor :start, :end

  def initialize
    @start = DateTime.now
    @end = DateTime.now + 1
  end

  def self.from_attributes(attributes)
    interval = self.new ''
    interval.start = DateTime.new Integer(attributes['from(1i)'].to_i), Integer(attributes['from(2i)'].to_i), Integer(attributes['from(3i)'].to_i), Integer(attributes['from(4i)'].to_i), Integer(attributes['from(5i)'].to_i)
    interval.end = DateTime.new Integer(attributes['to(1i)'].to_i), Integer(attributes['to(2i)'].to_i), Integer(attributes['to(3i)'].to_i), Integer(attributes['to(4i)'].to_i), Integer(attributes['to(5i)'].to_i)
    
    interval
  end
end