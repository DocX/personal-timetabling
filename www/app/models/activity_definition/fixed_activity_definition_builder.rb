module ActivityDefinition
class FixedActivityDefinition < GenericAbstractActivityDefinition
	
  # Creates activity definition from fixed signature of definition
  # Receives hash containing
  # - from: datetime 
  # - to: datetime
  # - repeating: false | repeating definition
  def self.from_attributes attributes
    definition = FixedActivityDefinition.new

    definition.domain_template = TimeDomains::StackTimeDomain.new
    periods_intervals = definition.set_periods_from_attributes attributes
    
    # make intervals for each period as the domain template
    # this will be cropped by periods so each occurence will have only its fixed time interval
    periods_intervals.each do |i|
      definition.domain_template.push(TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, i)
    end

    definition.occurence_min_duration = periods_intervals[0].seconds
    definition.occurence_max_duration = periods_intervals[0].seconds

    definition
  end

  def to_attributes
    {
      :type => 'fixed',
      :repeating => self.repeating_attributes,
      :from => self.period_start,
      :to => selft.first_period_end
    }
  end
end 
end