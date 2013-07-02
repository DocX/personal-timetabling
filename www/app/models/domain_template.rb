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

  before_save :store_referenced_ids
  before_destroy :check_references

  def domain_attributes
    self.domain.to_hash
  end

  def domain_attributes=(attributes)
    self.domain = TimeDomains::BaseTimeDomain.from_attributes attributes
  end

  protected 

  # inflates domain definition hash with database references
  def get_referenced_ids
    domain.referenced_domain_templates_ids
  end

  def store_referenced_ids
    # get referenced ids
    ids = self.get_referenced_ids

    # make diff of before safe and after safe ids
    # and store that diff to counter
   
  end

  def check_references
    true
  end
end
