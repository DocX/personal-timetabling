class Occurance < ActiveRecord::Base
  attr_accessible :duration, :start, :min_duration, :max_duration
  
  serialize :domain_definition
  
  belongs_to :activity
  
  before_save do |occurance|
    occurance.end = occurance.start + Rational(occurance.duration, 86400)
  end
  
  def start
    return self['start'].to_datetime
  end
  
  def self.in_range(start_date, end_date)
    where('start < ? AND end > ?', end_date, start_date  )
  end
end
