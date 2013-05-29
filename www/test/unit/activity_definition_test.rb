require 'test_helper'
require 'date'

class ActivityDefinitionTest < ActiveSupport::TestCase
  # test "the truth" do
  #   assert true
  # end

  test "create occurences from fixed attributes" do
  	start_date = DateTime.new 2013,5,12,10,0,0
  	end_date = DateTime.new 2013,5,12,18,0,0

  	fixed_attributes = {
  		:from => start_date.iso8601,
  		:to => end_date.iso8601
  	}

  	definition = ActivityDefinition.fixed fixed_attributes

  	assert definition.periods_count == 1
  	assert definition.period.duration >= 1
  	assert definition.period.unit == Duration::DAY

  	# create some occurences (for nil activity, that should not be touched)
  	occurences = definition.create_occurences nil

  	assert_equal occurences.size, 1, 'Number of occurences for fixed definition should be 1'
  	assert_equal start_date, occurences[0].start, 'Start of occurence should equal to start of fixed definition'
  	assert_equal 8*3600, occurences[0].duration

  end


  test "create occurences from fixed attributes with repeating with repeat count" do
    start_date = DateTime.new 2013,5,12,10,0,0
    end_date = DateTime.new 2013,5,12,18,0,0

    fixed_attributes = {
      :from => start_date.iso8601,
      :to => end_date.iso8601,
      :repeating => {
        :period_unit => 'days',
        :period_duration => 2,
        :until_repeats => 5,
        :until_type => 'repeats',
        :until_date => nil
      }
    }

    definition = ActivityDefinition.fixed fixed_attributes

    assert definition.periods_count == 5
    assert definition.period.duration == 2
    assert definition.period.unit == Duration::DAY

    # create some occurences (for nil activity, that should not be touched)
    occurences = definition.create_occurences nil

    assert_equal occurences.size, 5, 'Number of occurences for fixed definition should be 5'
    assert_equal start_date, occurences[0].start, 'Start of occurence should equal to start of fixed definition'
    
    occurences.each do |o|
      assert_equal 8*3600, o.duration
    end

  end

  test "create occurences from fixed attributes with repeating until date" do
    start_date = DateTime.new 2013,5,12,10,0,0
    end_date = DateTime.new 2013,5,12,18,0,0
    repeat_until = DateTime.new 2013,6,12,18,0,0

    fixed_attributes = {
      :from => start_date.iso8601,
      :to => end_date.iso8601,
      :repeating => {
        :period_unit => 'days',
        :period_duration => 2,
        :until_repeats => 5,
        :until_type => 'date',
        :until_date => repeat_until.iso8601
      }
    }

    definition = ActivityDefinition.fixed fixed_attributes

    assert_equal 16, definition.periods_count, 'Number of periods'
    assert_equal 2, definition.period.duration, 'Period length'
    assert definition.period.unit == Duration::DAY

    # create some occurences (for nil activity, that should not be touched)
    occurences = definition.create_occurences nil

    assert_equal occurences.size, 16, 'Number of occurences for fixed definition should be 5'
    assert_equal start_date, occurences[0].start, 'Start of occurence should equal to start of fixed definition'
    
    occurences.each do |o|
      assert_equal 8*3600, o.duration
      assert o.start < repeat_until
    end

  end


  test "create occurences from floating attributes once" do
    start_date = DateTime.new 2013,5,12,10,0,0
    end_date = DateTime.new 2013,5,20,18,0,0
    duration_min = 2 * 86400
    duration_max = 4 * 86400

    # create domain with afternoon 13-18 on business days
    domain_template = TimeDomainStack.new
    domain_template.unshift TimeDomainStack::Action.new(TimeDomainStack::Action::ADD, afternoon_time)
    domain_template.unshift TimeDomainStack::Action.new(TimeDomainStack::Action::MASK, business_days_domain)

    attributes = {
      :from => start_date.iso8601,
      :to => end_date.iso8601,
      :duration_min => duration_min,
      :duration_max => duration_max,
      :domain_template => domain_template
    }

    definition = ActivityDefinition.floating attributes

    assert_equal 1, definition.periods_count
    assert definition.period.duration >= 9
    assert definition.period.unit == Duration::DAY

    # create some occurences (for nil activity, that should not be touched)
    occurences = definition.create_occurences nil

    assert_equal 1, occurences.size, 'Number of occurences for fixed definition should be 1'
    assert_equal DateTime.new(2013,5,13,13,0,0), occurences[0].start, 'Start of occurence should equal to start of fixed definition'
    assert_equal duration_min, occurences[0].duration
    assert_equal duration_min, occurences[0].min_duration
    assert_equal duration_max, occurences[0].max_duration

  end


  test "create occurences from floating attributes repeating number" do
    start_date = DateTime.new 2013,5,12,10,0,0
    end_date = DateTime.new 2013,5,14,18,0,0
    duration_min = 2 * 86400
    duration_max = 4 * 86400

    # create domain with afternoon 13-18 on business days
    domain_template = TimeDomainStack.new
    domain_template.unshift TimeDomainStack::Action.new(TimeDomainStack::Action::ADD, afternoon_time)
    domain_template.unshift TimeDomainStack::Action.new(TimeDomainStack::Action::MASK, business_days_domain)

    attributes = {
      :from => start_date.iso8601,
      :to => end_date.iso8601,
      :duration_min => duration_min,
      :duration_max => duration_max,
      :domain_template => domain_template,
      :repeating => {
        :period_unit => 'week',
        :period_duration => 1,
        :until_repeats => 5,
        :until_type => 'repeats',
        :until_date => nil
      }
    }

    definition = ActivityDefinition.floating attributes

    assert_equal 5, definition.periods_count
    assert definition.period.duration == 1
    assert definition.period.unit == Duration::WEEK

    # create some occurences (for nil activity, that should not be touched)
    occurences = definition.create_occurences nil

    assert_equal occurences.size, 5, 'Number of occurences for fixed definition should be 5'
    assert_equal DateTime.new(2013,5,13,13,0,0), occurences[0].start, 'Start of occurence should equal to start of fixed definition'
    
    occurences.each do |o|
      assert_equal duration_min, o.duration
      assert_equal duration_min, o.min_duration
      assert_equal duration_max, o.max_duration
    end
  end


  def business_days_domain
    business_days_domain = TimeDomainStack.new 
    # monday
    business_days_domain.push (TimeDomainStack::Action.new TimeDomainStack::Action::ADD, BoundlessIntervalRepeating.from_attributes({
          :from => DateTime.new(2013,1,7,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))
     # tuesday
    business_days_domain.push (TimeDomainStack::Action.new TimeDomainStack::Action::ADD, BoundlessIntervalRepeating.from_attributes({
          :from => DateTime.new(2013,1,8,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))
     # wednesday
    business_days_domain.push (TimeDomainStack::Action.new TimeDomainStack::Action::ADD, BoundlessIntervalRepeating.from_attributes( {
          :from => DateTime.new(2013,1,9,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))
     # thursday
    business_days_domain.push (TimeDomainStack::Action.new TimeDomainStack::Action::ADD, BoundlessIntervalRepeating.from_attributes( {
          :from => DateTime.new(2013,1,10,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))
     # friday
    business_days_domain.push (TimeDomainStack::Action.new TimeDomainStack::Action::ADD, BoundlessIntervalRepeating.from_attributes( {
          :from => DateTime.new( 2013,1,11,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))

    business_days_domain
  end

  def afternoon_time 
    BoundlessIntervalRepeating.from_attributes({
      :from => DateTime.new(2013,1,1,13,0,0).iso8601,
      :duration => {:duration => 5, :unit => 'hour'},
      :period => {:duration => 1, :unit => 'day'}
    })
  end
end
