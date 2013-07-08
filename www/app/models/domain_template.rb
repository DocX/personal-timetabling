# stores domain templates
# 
# it stores domain in serialized form, with addition of references to other domain templates in database
# and can deserialize them into objects
class DomainTemplate < ActiveRecord::Base
  # fields
  # domain - serialized TimeDomains::BaseTimeDomain object
  
  serialize :domain

  attr_accessible :name, :domain_attributes

  validates :name, :presence => true
  validates :domain, :presence => true

  before_destroy :is_not_referenced?

  def domain_attributes
    self.domain.to_hash
  end

  def domain_attributes=(attributes)
    self.domain = TimeDomains::BaseTimeDomain.from_attributes attributes
  end

  # referenced domain templates implementation methods for counting observer
  def referenced_domain_templates_ids
    domain.referenced_domain_templates_ids
  end

  def referenced_domain_templates_ids_changed?
    domain_changed?
  end

  def referenced_domain_templates_ids_was
    domain_was.referenced_domain_templates_ids
  end

  def is_not_referenced?
    self.reference_count == 0
  end

  def is_referenced?
    self.reference_count != 0
  end
  
end
