class Event < ActiveRecord::Base
  attr_accessible :name, :duration, :start, :min_duration, :max_duration, :domain_attributes, :tz_offset, :activity_id, :schedule_since, :schedule_after
  
  serialize :domain, TimeDomains::BaseTimeDomain
  serialize :events_after, Array 
  
  validates :name, :presence => true
  validates :domain, :presence => true
  validates :tz_offset, :numericality => :only_integer
  validates :duration, :numericality => {:greater_than => 0}
  validate do |event|
    return event.schedule_since < event.schedule_deadline
  end

  belongs_to :activity
  
  after_initialize do |a|   
    a.tz_offset ||= 0 rescue nil 
    a.duration ||= 0 rescue nil
    a.name ||= '' rescue nil
    a.events_after ||= [] rescue nil
    a.start ||= DateTime.now rescue nil
  end

  def self.future 
    where('start > ?', DateTime.now)
  end

  def self.in_range(start_date, end_date)
    where('start < ? AND end > ?', end_date, start_date  )
  end

  # set proper end datetime from start and duration
  before_save do |event|
    event.end = event.start + Rational(event.duration, 86400)
  end


  # instance properties 

  def duration=(d)
    Rails.logger.debug 'set duretaion'
    self['duration'] = d
    self['end'] = self.start + Rational(self.duration, 86400) rescue nil
  end

  def start=(s)
    Rails.logger.debug 'set start'
    self['start'] = s
    self['end'] = self.start + Rational(self.duration, 86400) rescue  self['start'] 
  end
  
  # get domain limmited to schedule_since to schedule_deadline
  def scheduling_domain
    TimeDomains::StackTimeDomain.create_masked(
      TimeDomains::BoundedTimeDomain.create(self.schedule_since.to_datetime, self.schedule_deadline.to_datetime),
      self.domain
      )
  end

  def ordered_after_this
    Event.all(events_after)
  end

  def ordered_after_this_ids
    events_after
  end

  # resets to initial position
  # which is max_duration and first start of domain
  def reset!
    self.duration = self.max_duration
    bounding_interval = self.domain.bounding_interval
    self.start = self..domain.get_intervals(bounding_interval.first, bounding_interval.last).first.start
    
    save
  end
end
