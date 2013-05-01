# Activity definition represents description of set of occurances forming the activity
# It is: 
# * If all occurances are linked with regularity (based on the repeating period bellow)
# * Repeating definition. It is First occurance domain start, count of repeating occurances.
# * Domain template for the whole time of activity (ie for all occurances). Each occurance have only the part of domain template cropped to its period of repeat

class ActivityDefinition 
  include ActiveModel::Validations
  include ActiveModel::Conversion
  extend ActiveModel::Naming
  extend ActiveRecord::Validations::ClassMethods
  
  attr_accessor :regular, :period, :domain_template, :period_start, :periods_count
  
  attr_reader :errors
  
  def initialize(attributes = {})
    # period is of kind Duration
    attributes['period'] = attributes['period'].nil? ? Duration.new : Duration.new(attributes['period'])
    @regular = false
    @domain_template = nil
    @period_start = nil
    @periods_count = 1

    unless attributes['period_start(1i)'].nil?
      attributes['period_start'] = DateTime.new(
        attributes['period_start(1i)'].to_i, 
        attributes['period_start(2i)'].to_i, 
        attributes['period_start(3i)'].to_i, 
        attributes['period_start(4i)'].to_i, 
        attributes['period_start(5i)'].to_i)
    end
    
    attributes.each do |name, value|
      send("#{name}=", value)  if respond_to? "#{name}="
    end
    
    @errors = ActiveModel::Errors.new(self)
  end
  
  validates :periods_count, :numericality => {:greater_than_or_equal_to => 1}
  
  validates_associated :period
  
  def persisted?
    false
  end
  
  def marked_for_destruction?
    false
  end
  
  def _destroy
    0
  end
  
  def encode_with coder
    coder['regular'] = @regular
    coder['period'] = @period
    coder['domain_template'] = @domain_template
    coder['period_start'] = @period_start
    coder['periods_count'] = @periods_count
  end
  
  def init_with coder
    @regularcoder = coder['regular']
    @periodcoder = coder['period'] 
    @domain_templatecoder = coder['domain_template'] 
    @period_startcoder = coder['period_start'] 
    @periods_countcoder = coder['periods_count']
    @errors = ActiveModel::Errors.new(self)
    
    self
  end
end