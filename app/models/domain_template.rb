class DomainTemplate < ActiveRecord::Base
  attr_accessible :name, :domain_stack_attributes
  
  validates :name, :presence => true
  
  # templates describes stack of interval defintionions
  # validates_associated :templates
  serialize :domain_data
  
  
  def domain_stack
    self.domain_data
  end
  
  def domain_stack=(definition)
    self.domain_data = definition
  end
  
  def domain_stack_attributes=(attributes)
    new_attributes = {}
    attributes = attributes.each do |k,a|
      if a['type'] == 'database'
        a = {'action' => a['action'], 'type' => 'raw', 'object' => DomainTemplate.find(a['domain_template_id']).domain_stack}
      end
      new_attributes[k] = a
    end
    stack = TimeDomainStack.from_attributes new_attributes
    self.domain_stack = stack
  end
end