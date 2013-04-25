class Occurance < ActiveRecord::Base
  attr_accessible :duration, :start
  
  belongs_to :activity
  
  before_save do |occurance|
    occurance.end = occurance.start + Rational(occurance.duration, 86400)
  end
  
  def self.in_range(start_date, end_date)
    where('start < ? AND end > ?', end_date, start_date  )
  end
end
