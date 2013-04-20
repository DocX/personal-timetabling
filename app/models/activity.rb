class Activity < ActiveRecord::Base
  attr_accessible :data, :description, :name, :type, :occurances
    
  has_many :occurances

  accepts_nested_attributes_for :occurances 
end

class Fixed < Activity
  
  validate :validate_occurrances

  def validate_occurrances
    errors.add(:occurances, "Fixed activity can hold only 1 occurance") if occurances.length > 1
  end
  
end