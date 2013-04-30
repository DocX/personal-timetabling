class Activity < ActiveRecord::Base
  attr_accessible :description, :name, :occurances, :activity_definition_attributes
    
  has_many :occurances

  validates :name, :presence => true
  
  validates_associated :activity_definition
  
  after_initialize do |a|
    Rails.logger.debug "after_initialize a.data = #{a.data.inspect}"
    
    a.data = {} unless a.data.is_a? Hash
    
    a.data[:definition] = ActivityDefinition.new if a.data[:definition].nil?
  end
   
  def activity_definition
    self.data[:definition]
  end
  
  def activity_definition=(definition)
    self.data = {} if data.nil?
    self.data[:definition] = definition
  end
  
  def activity_definition_attributes=(attributes)
    Rails.logger.debug 'activity_definition_attributes='
    self.activity_definition = ActivityDefinition.new attributes     
  end
end