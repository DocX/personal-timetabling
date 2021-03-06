# Duration describes amount of time in respect to common gregorian calendar units of time - Month, Week, Days and Hours.
# Month unit is considered to be non fixed in count of seconds and rather that it describes addition of "month number" in the date. 

module TimeDomains
class Duration
  include ActiveModel::Validations
  include ActiveModel::Conversion
  extend ActiveModel::Naming
  include PersonalTimetablingAPI::Core::DurationMixin

  MONTH = 4
  WEEK = 3
  DAY = 2
  HOUR = 1
  MINUTE = 5

  attr_accessor :duration, :unit
  attr_reader :errors
  

  validates :duration , :numericality => { :greater_than_or_equal_to => 1 }
  validate :unit do |unit|
    Duration::units.any? {|u| u.last == unit}
  end

  def self.units
    { 
      self::MONTH => 'Month',
      self::WEEK => 'Week',
      self::DAY => 'Day',
      self::HOUR => 'Hour',
      self::MINUTE => 'Minute'
    }
  end

  def self.unit_strings
    {
      'month' => self::MONTH,
      'months' => self::MONTH,
      'day' => self::DAY,
      'days' => self::DAY,
      'hour' => self::HOUR,
      'hours' => self::HOUR,
      'week' => self::WEEK,
      'weeks' => self::WEEK,
      'minutes' => self::MINUTE,
      'minute' => self::MINUTE
    }
  end
  
  def initialize(attributes = {})
    @duration = 1
    @unit = DAY
    
    attributes.each do |name, value|
      send("#{name}=", value) if respond_to? "#{name}="
    end
    
    @errors = ActiveModel::Errors.new(self)
  end
  
  def duration= value 
    @duration = value.to_i
  end
  def unit= value 
    @unit = Duration.unit_strings[value] || value.to_i
  end
  
  def persisted?
    false
  end

  def marked_for_destruction?
    false
  end  

  def encode_with coder
    coder['duration'] = @duration.to_i
    coder['unit'] = @unit.to_i
  end
  
  def init_with coder
    @duration = coder['duration'].to_i
    @unit = coder['unit'].to_i
    @errors = ActiveModel::Errors.new(self)
  end
  
  def to_s
    @duration.to_s + ' ' + Duration.units[@unit.to_i]
  end

  def to_hash
    {
      :duration => self.duration,
      :unit => Duration.units[self.unit.to_i].downcase
    }
  end
end
end