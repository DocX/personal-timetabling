# Base Time domain class. Time domain provides set of time intervals depending of its actual implementation.

class TimeDomain

  attr_accessor :name

  def initialize
  end
  
  # This is only data layer for intended algorithm implemented in java
  # get_intervals_in_interval
  # includes_interval?
  # ...

  def self.from_attributes attributes
  	if attributes['type'] == 'raw'
        domain = attributes['object']
    else
        type = {'stack' => TimeDomainStack, 'bounded' => BoundedInterval, 'boundless' => BoundlessIntervalRepeating}[attributes['type']]
        domain = type.from_attributes attributes['data']
    end

    domain
  end

end