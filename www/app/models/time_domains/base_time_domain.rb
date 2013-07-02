# Base Time domain class. Time domain provides set of time intervals depending of its actual implementation.

module TimeDomains
class BaseTimeDomain

  attr_accessor :name

  def initialize
  end

  def referenced_domain_templates_ids
    []
  end

  def to_hash
    raise 'not implemented'
  end

  def self.from_attributes attributes
    klass = ('TimeDomains::' + (attributes[:type].downcase.underscore + '_time_domain').classify).constantize
    domain = klass.from_attributes attributes[:data]

    domain
  end

end
end