module ActivityDefinition
class FloatingActivityDefinition < GenericAbstractActivityDefinition

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
  def self.from_attributes attributes
    definition = FloatingActivityDefinition.new

    periods_intervals = definition.set_periods_from_attributes attributes
    
    # make intervals for each period as the mask for the domain template
    # this will mask inside each period the range of domain template that correspond to from-to range
    # and then in creation will be cropped to each period.
    periods_mask_domain = TimeDomains::StackTimeDomain.new
    periods_intervals.each do |i|
      periods_mask_domain.push(TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, i)
    end

    if attributes[:domain_template].is_a? Hash 
      attributes[:domain_template] = TimeDomains::BaseTimeDomain.from_attributes attributes[:domain_template];
    end

    # create domain stack with domain template masked by periods domain
    definition.domain_template = TimeDomains::StackTimeDomain.new
    definition.domain_template.unshift TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, attributes[:domain_template]
    definition.domain_template.unshift TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::MASK, periods_mask_domain

    definition.occurence_min_duration = attributes[:duration_min]
    definition.occurence_max_duration = attributes[:duration_max]

    definition
  end

  def to_attributes
    {
      :type => 'floating',
      :repeating => self.repeating_attributes,
      :domain_template => self.domain_template.to_attributes,
      :duration_min => self.occurence_min_duration,
      :duration_max => self.occurence_max_duration,
      :from => self.period_start,
      :to => selft.first_period_end
    }
  end
end
end