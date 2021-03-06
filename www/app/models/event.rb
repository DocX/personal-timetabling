class Event < ActiveRecord::Base
  attr_accessible :name, :duration, :start, :min_duration, :max_duration, :domain_attributes, :tz_offset, :activity_id, :schedule_since, :schedule_deadline
  
  serialize :domain, TimeDomains::BaseTimeDomain
  serialize :events_after, Array 
  
  validates :name, :presence => true
  validates :domain, :presence => true
  validates :tz_offset, :numericality => :only_integer
  validates :duration, :numericality => {:greater_than => 0}
  validate do |event|
    return event.schedule_since < event.schedule_deadline
  end
  # validate domain
  validate do 
    return false if domain.nil?

    # get intervals of domain in event boundary
    domain_intervals = domain.get_intervals schedule_since.to_datetime, schedule_deadline.to_datetime

    # check if contains at least one
    errors.add(:domain, 'domain must contain some interval in time schedule_since to schedule_deadline') unless domain_intervals.size > 0
    return domain_intervals.size > 0
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

  def domain_attributes=(attrs)
    self.domain = TimeDomains::BaseTimeDomain.from_attributes attrs
  end

  def domain_attributes
    domain.to_hash
  end

  def duration=(d)
    self['duration'] = d
    self['end'] = self.start + Rational(self.duration, 86400) rescue nil
  end

  def start=(s)
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
    bounding_interval = self.scheduling_domain.bounding_interval
    self.start = self.scheduling_domain.get_intervals(bounding_interval.first, bounding_interval.last).first.start
    
    save
  end

  # referenced domains ids observer interface
  def referenced_domain_templates_ids
    domain.referenced_domain_templates_ids
  end

  def referenced_domain_templates_ids_changed?
    domain_changed?
  end

  def referenced_domain_templates_ids_was
    domain_was.referenced_domain_templates_ids
  end

end
