module ActivityDefinition
class FloatingActivityDefinition < GenericAbstractActivityDefinition

  # domain which will be splited by occurrences windows
  attr_accessor :domain_template

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

    definition.set_periods_from_attributes attributes

    # load domain template
    if attributes[:domain_template].is_a? Hash 
      definition.domain_template = TimeDomains::BaseTimeDomain.from_attributes attributes[:domain_template];
    else
      definition.domain_template = attributes[:domain_template]
    end

    definition.occurrence_min_duration = attributes[:duration_min].to_i
    definition.occurrence_max_duration = attributes[:duration_max].to_i

    definition.first_occurrence_window_start = DateTime.parse attributes[:from]
    definition.first_occurrence_window_end = DateTime.parse attributes[:to]

    definition
  end

  # return domain for occurrence with given period
  def domain_for_event_occurrence(from, to)
    TimeDomains::StackTimeDomain.create_masked(
      TimeDomains::BoundedTimeDomain.create(from,to),
      @domain_template
    )
  end

  def to_attributes
    {
      :type => 'floating',
      :repeating => self.repeating_attributes,
      :domain_template => self.domain_template.to_attributes,
      :duration_min => self.occurence_min_duration,
      :duration_max => self.occurence_max_duration,
      :from => self.first_occurrence_window_start,
      :to => selft.first_occurrence_window_end
    }
  end

  def encode_with coder
    coder['domain_template'] = @domain_template

    super coder
  end

  def init_with coder
    @domain_template = coder['domain_template']

    super coder
  end

  def referenced_domain_templates_ids
    domain_template.referenced_domain_templates_ids
  end
end
end