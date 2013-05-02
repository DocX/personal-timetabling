require_dependency 'time_domain_stack'
require_dependency 'bounded_interval'
require_dependency 'boundless_interval_repeating'
require_dependency 'duration'

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
    
    stack = TimeDomainStack.from_attributes attributes
    self.domain_stack = stack
  end
end
