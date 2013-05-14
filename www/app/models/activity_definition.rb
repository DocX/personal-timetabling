# Activity definition represents description of set of occurances for their creation
# It is: 
# * If all occurances are linked with regularity (based on the repeating period bellow)
# * Repeating definition. It is First occurance domain start, count of repeating occurances.
# * Domain template for the whole time of activity (ie for all occurances). Each occurance have only the part of domain template cropped to its period of repeat

class ActivityDefinition 
  include ActiveModel::Validations
  include ActiveModel::Conversion
  extend ActiveModel::Naming
  extend ActiveRecord::Validations::ClassMethods
  
  # Activity definition describes occurances of some activity. Occurences created by activity definition is independend on this definition
  #
  # Period: Duration object specifying range length for each occurence. First periods is aligned to period_start
  # Period_start: Datetime, specifies first period alignment
  # Peroids_count: Count of occurences for this definition. Each occurence will have domain masked by the period range 
  # occurence_min_duration: Sets minimal duration for each of created occurences
  # occurence_max_duration: Sets maximaln duration for each of created occurences
  # domain_template: TimeDomainStack object
  attr_accessor :period, :domain_template, :period_start, :periods_count, :occurence_min_duration, :occurence_max_duration
  
  attr_reader :errors
  
  def self.from_typed(attributes)
    case attributes[:type]
    when 'fixed'
      self.fixed attributes
    when 'floating'
      self.floating attributes
    end
  end

  # Creates activity definition from fixed signature of definition
  # Receives hash containing
  # - from: datetime 
  # - to: datetime
  # - repeating: false | repeating definition
  def self.fixed attributes
    definition = ActivityDefinition.new

    fixed_interval = BoundedInterval.create DateTime.iso8601(attributes[:from]), DateTime.iso8601(attributes[:to]) 
    definition.domain_template = fixed_interval

    # made period simply larger than given fixed duration
    # so period will not take any effect when masking domain template
    definition.period = Duration.new({
      :unit => Duration::DAY,
      :duration => fixed_interval.bounding_days
    })
    definition.period_start = fixed_interval.start
    definition.periods_count = 1
    definition.occurence_min_duration = fixed_interval.seconds
    definition.occurence_max_duration = fixed_interval.seconds

    definition
  end


  # Creates activity definition from fixed signature of definition
  # Receives hash containing
  # - from: datetime 
  # - to: datetime
  # - repeating: false | repeating definition
  def self.floating attributes

  end

  def initialize(attributes = {})
    # period is of kind Duration
    attributes['period'] = attributes['period'].nil? ? Duration.new : Duration.new(attributes['period'])
    @domain_template = nil
    @period_start = nil
    @periods_count = 1

    unless attributes['period_start(1i)'].nil?
      attributes['period_start'] = DateTime.new(
        attributes['period_start(1i)'].to_i, 
        attributes['period_start(2i)'].to_i, 
        attributes['period_start(3i)'].to_i, 
        attributes['period_start(4i)'].to_i, 
        attributes['period_start(5i)'].to_i)
    end
    
    attributes.each do |name, value|
      send("#{name}=", value)  if respond_to? "#{name}="
    end
    
    @errors = ActiveModel::Errors.new(self)
  end
  
  validates :periods_count, :numericality => {:greater_than_or_equal_to => 1}
  
  validates_associated :period
  
  def persisted?
    false
  end
  
  def marked_for_destruction?
    false
  end
  
  def _destroy
    0
  end
  
  def encode_with coder
    coder['period'] = @period
    coder['domain_template'] = @domain_template
    coder['period_start'] = @period_start
    coder['periods_count'] = @periods_count
  end
  
  def init_with coder
    @period = coder['period'] 
    @domain_template = coder['domain_template'] 
    @period_start = coder['period_start'] 
    @periods_count = coder['periods_count']
    @occurence_max_duration = coder['occurence_max_duration']
    @occurence_min_duration = coder['occurence_min_duration']
    @errors = ActiveModel::Errors.new(self)
    
    self
  end


  # creates occurences and connects them to given activity
  def create_occurences for_activity
    counter = 0
    occurences = []
    period_intervals = Webui::Core::Utils.period_intervals @period_start, @period, @periods_count

    while counter < @periods_count
      # get period interval
      period_interval = period_intervals[counter]

      # mask domain by current period
      period_domain = TimeDomainStack.new
      period_domain.push TimeDomainStack::Action.new(TimeDomainStack::Action::MASK, period_interval)
      period_domain.push TimeDomainStack::Action.new(TimeDomainStack::Action::ADD, @domain_template)

      occurence = Occurance.new
      occurence.activity = for_activity
      occurence.start = period_interval.start
      occurence.duration = @occurence_min_duration
      occurence.min_duration = @occurence_min_duration
      occurence.max_duration = @occurence_max_duration
      occurence.domain_definition = period_domain

      occurences << occurence

      counter += 1
    end

    occurences
  end
end