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
    domain = TimeDomains::StackTimeDomain.new
    domain.push(TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, TimeDomains::BoundedTimeDomain.create(from,to))

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