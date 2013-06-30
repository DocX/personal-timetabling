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
  attr_accessor :period, :domain_template, :period_start, :periods_count, :occurence_min_duration, 
    :occurence_max_duration, :linked_period
  
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

    definition.domain_template = TimeDomainStack.new
    periods_intervals = definition.set_periods_from_attributes attributes
    
    # make intervals for each period as the domain template
    # this will be cropped by periods so each occurence will have only its fixed time interval
    periods_intervals.each do |i|
      definition.domain_template.push(TimeDomainStack::Action.new TimeDomainStack::Action::ADD, i)
    end

    definition.occurence_min_duration = periods_intervals[0].seconds
    definition.occurence_max_duration = periods_intervals[0].seconds

    definition
  end

  # Creates activity definition from fixed signature of definition
  # Receives hash containing
  # - from: datetime First occurence domain cropping start
  # - to: datetime First occurence domain cropping end
  # - domaint_template: DomainTemplate object of domain template
  # - duration_min: integer, seconds of min duration
  # - duration_max: integer, seconds of max duration
  # - repeating: false | repeating definition
  #
  # Floating activity takes domain template and masks it for each period by the interval 
  # of from - to, shifted by the period. This masked domain is them domain template for 
  # the period's occurance
  def self.floating attributes
    definition = ActivityDefinition.new

    periods_intervals = definition.set_periods_from_attributes attributes
    
    # make intervals for each period as the mask for the domain template
    # this will mask inside each period the range of domain template that correspond to from-to range
    # and then in creation will be cropped to each period.
    periods_mask_domain = TimeDomainStack.new
    periods_intervals.each do |i|
      periods_mask_domain.push(TimeDomainStack::Action.new TimeDomainStack::Action::ADD, i)
    end

    if attributes[:domain_template].is_a? Hash 
      attributes[:domain_template] = TimeDomain.from_attributes attributes[:domain_template];
    end

    # create domain stack with domain template masked by periods domain
    definition.domain_template = TimeDomainStack.new
    definition.domain_template.unshift TimeDomainStack::Action.new TimeDomainStack::Action::ADD, attributes[:domain_template]
    definition.domain_template.unshift TimeDomainStack::Action.new TimeDomainStack::Action::MASK, periods_mask_domain

    definition.occurence_min_duration = attributes[:duration_min]
    definition.occurence_max_duration = attributes[:duration_max]

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
    coder['occurence_max_duration'] = @occurence_max_duration
    coder['occurence_min_duration'] = @occurence_min_duration
    coder['linked_period'] = @linked_period
    coder['linked'] = @linked

    coder
  end
  
  def init_with coder
    @period = coder['period'] 
    @domain_template = coder['domain_template'] 
    @period_start = coder['period_start'] 
    @periods_count = coder['periods_count']
    @occurence_max_duration = coder['occurence_max_duration']
    @occurence_min_duration = coder['occurence_min_duration']
    @linked_period = coder['linked_period'] || nil
    @linked = coder['linked'] || false

    @errors = ActiveModel::Errors.new(self)
    
    self
  end

  def linked?
    @linked
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


   
  # setup definition period values from attributes
  # and generate array of intervals for each period of range from - to
  def set_periods_from_attributes attributes
    definition = self

    fixed_interval = BoundedInterval.create DateTime.iso8601(attributes[:from]), DateTime.iso8601(attributes[:to])

    unless attributes[:repeating]
      # repeat definition for "once" or "no repeat"
      attributes[:repeating] = {
        :period_unit => 'days',
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
      # whole duration in the range, plus one, which needs to be tested to until date
      definition.period.between(fixed_interval.start, DateTime.iso8601(attributes[:repeating][:until_date])) + 1

    periods_intervals = []

    (1..(definition.periods_count)).each do |i|
      # for each occurence that is after until date, remove one repeat. 
      # it will be done at most once. If last full period before until_date has end too close to until_date that one 
      # occurence cant fit there, it will reduce that "hoping" last period
      definition.periods_count -= 1 and break if attributes[:repeating][:until_type] != 'repeats' and fixed_interval.end > DateTime.iso8601(attributes[:repeating][:until_date])

      periods_intervals << fixed_interval
      
      fixed_interval = fixed_interval.after_duration definition.period;
    end

    periods_intervals
  end
end