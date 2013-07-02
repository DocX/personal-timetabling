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
  before_destroy :no_references?

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
    return true unless self.domain_changed?

    # get referenced ids
    was_ids = self.domain_was.nil? ? [] : self.domain_was.referenced_domain_templates_ids
    now_ids = get_referenced_ids

    # make diff of before safe and after safe ids
    # and store that diff to counter

    removed_ids = was_ids.select {|id| not now_ids.include? id }
    same_ids = was_ids.select {|id| now_ids.include? id}
    new_ids = now_ids.select {|id| not was_ids.include? id} 
   
    # count referenced templates
    DomainTemplate.where('id IN (?)', removed_ids).update_all('reference_count = reference_count - 1')
    DomainTemplate.where('id IN (?)', new_ids).update_all('reference_count = reference_count + 1')

    return true
  end

  def no_references?
    self.reference_count == 0
  end
end
