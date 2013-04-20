class Occurance < ActiveRecord::Base
  attr_accessible :duration, :start
  
  belongs_to :activity
end
