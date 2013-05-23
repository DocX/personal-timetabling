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

    unless attributes[:repeating]
      # repeat definition for "once" or "no repeat"
      attributes[:repeating] = {
        :period_unit => Duration::DAY,
        :period_duration => fixed_interval.bounding_days,
        :until_repeats => 1,
        :until_type => 'repeats',
        :until_date => nil
      }
    end

    definition.period = Duration.new({
      :unit => Duration.unit_strings[attributes[:repeating][:period_unit]],
      :duration => attributes[:repeating][:period_duration].to_i
    })

    definition.period_start = fixed_interval.start
    definition.periods_count = 
      attributes[:repeating][:until_type] == 'repeats' ? 
      attributes[:repeating][:until_repeats].to_i :
      definition.period.between(fixed_interval.start, DateTime.iso8601(attributes[:repeating][:until_date]))

    definition.domain_template = TimeDomainStack.new

    (1..(definition.periods_count)).each do |i|
      definition.domain_template.push(TimeDomainStack::Action.new TimeDomainStack::Action::ADD, fixed_interval)
      fixed_interval = fixed_interval.after_duration definition.period;
    end

    definition.occurence_min_duration = fixed_interval.seconds
    definition.occurence_max_duration = fixed_interval.seconds

    definition
  end

  # Creates activity definition from fixed signature of definition
  # Receives hash containing
  # - from: datetime 
  # - period: Hash for Duration instance of floating period cropping domain template
  # - domaint_template_id: id of DomainTemplate model
  # - duration_min: integer, seconds of min duration
  # - duration_max: integer, seconds of max duration
    # - repeating: false | repeating definition
  def self.floating attributes
    definition = ActivityDefinition.new

    # made period simply larger than given fixed duration
    # so period will not take any effect when masking domain template
    definition.period = Duration.new attributes[:period]
    definition.period_start = DateTime.iso8601(attributes[:from])
    definition.periods_count = 1
    definition.occurence_min_duration = attributes[:duration_min].to_i
    definition.occurence_max_duration = attributes[:duration_max].to_i
    definition.domain_template = DomainTemplate.find(attributes[:domain_template_id]).domain_stack

    throw 'Domain template not found' if definition.domain_template.nil?

    definition
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

      # get first interval of domain cut
      period_domain_intervals = period_domain.get_intervals period_interval.start, period_interval.end

      # if zero intervals in this period, skip to the next and dont create occurence
      if period_domain_intervals.size == 0
        counter += 1
        next
      end

      occurence = Occurance.new
      occurence.activity = for_activity
      occurence.start = period_domain_intervals[0].start
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