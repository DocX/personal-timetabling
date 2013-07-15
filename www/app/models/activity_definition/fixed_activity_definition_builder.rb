module ActivityDefinition
class FixedActivityDefinition < GenericAbstractActivityDefinition

  # Creates activity definition from fixed signature of definition
  # Receives hash containing
  # - from: datetime 
  # - to: datetime
  # - repeating: false | repeating definition
  def self.from_attributes attributes
    definition = FixedActivityDefinition.new

    definition.set_periods_from_attributes attributes

    definition.first_occurrence_window_start = DateTime.parse attributes[:from]
    definition.first_occurrence_window_end = DateTime.parse attributes[:to]
    
    # get seconds of event
    definition.occurrence_min_duration = ((definition.first_occurrence_window_end - definition.first_occurrence_window_start) * 86400).to_i
    definition.occurrence_max_duration = definition.occurrence_min_duration

    definition
  end

  # return domain for occurrence with given period
  def domain_for_event_occurrence(from, to)
    domain = TimeDomains::BoundlessTimeDomain.new
    domain.reference_start = DateTime.now.utc.midnight
    domain.duration = TimeDomains::Duration.new
    domain.duration.duration = 1440
    domain.duration.unit = TimeDomains::Duration::MINUTE
    domain.period = TimeDomains::Duration.new
    domain.period.duration = 1
    domain.period.unit = TimeDomains::Duration::DAY

    return domain
  end

  def to_attributes
    {
      :type => 'fixed',
      :repeating => self.repeating_attributes,
      :from => self.first_occurrence_window_start,
      :to => selft.first_occurrence_window_end
    }
  end

  def referenced_domain_templates_ids
    []
  end
end 
end