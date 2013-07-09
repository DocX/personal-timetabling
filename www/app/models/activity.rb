class Activity < ActiveRecord::Base
  attr_accessible :name, :link_events, :link_comparator, :definition_attributes
    
  has_many :events, :dependent => :destroy
  serialize :definition

  validates :name, :presence => true
  
  after_initialize do |a|   
  	a.link_comparator = a.link_comparator && a.link_comparator.to_sym
  end

  def self.with_events_in_range(from, to)
    Activity.where('EXISTS (SELECT 1 FROM events WHERE events.activity_id = activities.id AND start <= ? AND end >= ?)', to, from)
  end

  def definition_attributes
  	self.definition && self.definition.to_attributes
  end

  # set definition and create events
  def definition_attributes=(definition_attributes)
  	self.definition = ActivityDefinition::BaseActivityDefinition.from_attributes definition_attributes
  	self.create_events_from_definition
  end

  def create_events_from_definition
  	self.events << self.definition.create_events(self)
  end

  def linked?
    return link_events
  end

  def referenced_domain_templates_ids
    return [] if definition.nil?
    definition.referenced_domain_templates_ids
  end

  def referenced_domain_templates_ids_changed?
    definition_changed?
  end

  def referenced_domain_templates_ids_was
    return [] if definition_was.nil?
    definition_was.referenced_domain_templates_ids
  end


end