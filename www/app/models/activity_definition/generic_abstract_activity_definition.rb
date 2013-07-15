# Activity definition represents description of set of occurances for their creation
# It is: 
# * If all occurances are linked with regularity (based on the repeating period bellow)
# * Repeating definition. It is First occurance domain start, count of repeating occurances.
# * Domain template for the whole time of activity (ie for all occurances). Each occurance have only the part of domain template cropped to its period of repeat

module ActivityDefinition
class GenericAbstractActivityDefinition < BaseActivityDefinition 
  
  # Activity definition describes occurances of some activity. 
  # Occurences created by activity definition is independend on this definition
  attr_accessor( 
    # min and max duration of events of occurrences
    :occurrence_min_duration, 
    :occurrence_max_duration, 
    # first window time
    :first_occurrence_window_start,
    :first_occurrence_window_end,
    # repeating definition
    :repeating
  )
  
  def encode_with coder
    coder['repeating'] = @repeating
    coder['first_occurrence_window_start'] = @first_occurrence_window_start
    coder['first_occurrence_window_end'] = @first_occurrence_window_end
    coder['occurence_max_duration'] = @occurrence_max_duration
    coder['occurence_min_duration'] = @occurrence_min_duration

    coder
  end
  
  def init_with coder
    @repeating = coder['repeating'] 
    @first_occurrence_window_start = coder['first_occurrence_window_start'] 
    @first_occurrence_window_end = coder['first_occurrence_window_end']
    @occurrence_max_duration = coder['occurence_max_duration']
    @occurrence_min_duration = coder['occurence_min_duration']
    
    self
  end

  # creates events and connects them to given activity
  def create_events for_activity
    counter = 0
    events = []

    # get list of repeating periods, that cuts domain into per occurrence
    period_intervals = @repeating.get_periods(@first_occurrence_window_start, @first_occurrence_window_end)

    period_intervals.each do |period_interval|

      # get domain for this period by implementation of domain_for_event_occurrence
      period_domain = domain_for_event_occurrence(period_interval.first, period_interval.last)


      Rails.logger.debug 'activity definition create event new event'
      event = Event.new
      event.activity = for_activity
      unless for_activity.nil?
        event.name = for_activity.name
        event.name += " #{counter + 1}/#{period_intervals.size}" if period_intervals.size > 1 
      end

      
      # set initial duration to max duration as it is considered as optimal
      event.duration = @occurrence_max_duration
      event.min_duration = @occurrence_min_duration
      event.max_duration = @occurrence_max_duration
      event.domain = period_domain

      # set schedule interval for event to current repeat period interval
      event.schedule_since = period_interval.first
      event.schedule_deadline = period_interval.last

      # get first interval of domain cut
      period_domain_intervals = event.scheduling_domain.get_intervals period_interval.first, period_interval.last

      # if zero intervals in this period, skip to the next and dont create occurence
      if period_domain_intervals.size == 0
        counter += 1
        next
      end

      # set initial start of event to start of very least moment in domain
      event.start = period_domain_intervals.first.start

      events << event
      Rails.logger.debug 'activity definition create event event added'

      counter += 1
    end

    events
  end
   
  # setup definition period values from attributes
  # and generate array of intervals for each period of range from - to
  def set_periods_from_attributes attributes
    if attributes[:repeating].nil? || attributes[:repeating] == false
      @repeating = OnceRepeating.new 
      return
    end

    case attributes[:repeating][:period_unit]
    when 'days'
      @repeating =  DaylyRepeating.from_attributes attributes[:repeating]
    when 'weeks'
      @repeating = WeeklyRepeating.from_attributes attributes[:repeating]
    when 'monthly'
      @repeating = MonthlyRepeating.from_attributes attributes[:repeating]
    end

    @repeating
  end

  def repeating_attributes
    return @repeating.to_attributes
  end
end
end