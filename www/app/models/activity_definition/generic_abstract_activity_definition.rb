# Activity definition represents description of set of occurances for their creation
# It is: 
# * If all occurances are linked with regularity (based on the repeating period bellow)
# * Repeating definition. It is First occurance domain start, count of repeating occurances.
# * Domain template for the whole time of activity (ie for all occurances). Each occurance have only the part of domain template cropped to its period of repeat

module ActivityDefinition
class GenericAbstractActivityDefinition < BaseActivityDefinition 
  
  # Activity definition describes occurances of some activity. Occurences created by activity definition is independend on this definition
  #
  # Period: Duration object specifying range length for each occurence. First periods is aligned to period_start
  # Period_start: Datetime, specifies first period alignment
  # Peroids_count: Count of occurences for this definition. Each occurence will have domain masked by the period range 
  # occurence_min_duration: Sets minimal duration for each of created occurences
  # occurence_max_duration: Sets maximaln duration for each of created occurences
  # domain_template: TimeDomains::StackTimeDomain object
  attr_accessor :period, :domain_template, :period_start, :periods_count, :occurence_min_duration, 
    :occurence_max_duration, :first_period_end
  
  attr_reader :errors

  def initialize(attributes = {})
    # period is of kind Duration
    attributes['period'] = attributes['period'].nil? ? TimeDomains::Duration.new : TimeDomains::Duration.new(attributes['period'])
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
  
  def encode_with coder
    coder['period'] = @period
    coder['domain_template'] = @domain_template
    coder['period_start'] = @period_start
    coder['first_period_end'] = @first_period_end
    coder['periods_count'] = @periods_count
    coder['occurence_max_duration'] = @occurence_max_duration
    coder['occurence_min_duration'] = @occurence_min_duration

    coder
  end
  
  def init_with coder
    @period = coder['period'] 
    @domain_template = coder['domain_template'] 
    @period_start = coder['period_start'] 
    @first_period_end = coder['first_period_end']
    @periods_count = coder['periods_count']
    @occurence_max_duration = coder['occurence_max_duration']
    @occurence_min_duration = coder['occurence_min_duration']

    @errors = ActiveModel::Errors.new(self)
    
    self
  end

  # creates events and connects them to given activity
  def create_events for_activity
    counter = 0
    events = []
    period_intervals = PersonalTimetablingAPI::Core::Utils.period_intervals @period_start, @period, @periods_count

    while counter < @periods_count
      # get period interval
      period_interval = period_intervals[counter]

      # mask domain by current period
      period_domain = TimeDomains::StackTimeDomain.create_masked period_interval,  @domain_template

      # get first interval of domain cut
      period_domain_intervals = period_domain.get_intervals period_interval.start, period_interval.end

      # if zero intervals in this period, skip to the next and dont create occurence
      if period_domain_intervals.size == 0
        counter += 1
        next
      end

      Rails.logger.debug 'activity definition create event new event'
      event = Event.new
      event.activity = for_activity
      event.name = for_activity.name
      event.name += "#{counter + 1}/#{@periods_count}" if @periods_count > 1 

      # set initial start of event to start of very least moment in domain
      event.start = period_domain_intervals[0].start
      
      # set initial duration to max duration as it is considered as optimal
      event.duration = @occurence_max_duration
      event.min_duration = @occurence_min_duration
      event.max_duration = @occurence_max_duration
      event.domain = period_domain

      # set schedule interval for event to current repeat period interval
      event.schedule_since = period_interval.start
      event.schedule_deadline = period_interval.end

      events << event
      Rails.logger.debug 'activity definition create event event added'

      counter += 1
    end

    events
  end
   
  # setup definition period values from attributes
  # and generate array of intervals for each period of range from - to
  def set_periods_from_attributes attributes
    definition = self

    fixed_interval = TimeDomains::BoundedTimeDomain.create DateTime.iso8601(attributes[:from]), DateTime.iso8601(attributes[:to])

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

    definition.period = TimeDomains::Duration.new({
      :unit => TimeDomains::Duration.unit_strings[attributes[:repeating][:period_unit]],
      :duration => attributes[:repeating][:period_duration].to_i
    })

    definition.period_start = fixed_interval.start
    definition.first_period_end = fixed_interval.end
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

  def  repeating_attributes
    return false if self.periods_count <= 1

    period_hash = self.period.to_hash

    {
      :period_unit => period_hash[:unit],
      :period_duration => period_hash[:duration],
      :until_repeats => definition.periods_count,
      :until_type => 'repeats',
      :until_date => nil
    }
  end
end
end