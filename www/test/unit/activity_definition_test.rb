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
  		:to => end_date.iso8601,
      :type => 'fixed',
      :repeating => false
  	}

  	definition = ActivityDefinition::BaseActivityDefinition.from_attributes fixed_attributes

    #todo create test
    assert_equal 8 * 3600, definition.occurrence_max_duration
    assert_equal 8 * 3600, definition.occurrence_min_duration
  end


  test "create occurences from fixed attributes with repeating with repeat count" do
    start_date = DateTime.new 2013,5,12,10,0,0
    end_date = DateTime.new 2013,5,12,18,0,0

    fixed_attributes = {
      :type => 'fixed',
      :from => start_date.iso8601,
      :to => end_date.iso8601,
      :repeating => {
        :period_unit => 'days',
        :period_duration => 2,
        :until => 5,
        :until_type => 'repeats',
      }
    }

    definition = ActivityDefinition::BaseActivityDefinition.from_attributes fixed_attributes

    #todo
    assert_equal 8 * 3600, definition.occurrence_max_duration
    assert_equal 8 * 3600, definition.occurrence_min_duration
  end

  test "create occurences from fixed attributes with repeating until date" do
    start_date = DateTime.new 2013,5,12,10,0,0
    end_date = DateTime.new 2013,5,12,18,0,0
    repeat_until = DateTime.new 2013,6,12,18,0,0

    fixed_attributes = {
      :type => 'fixed',
      :from => start_date.iso8601,
      :to => end_date.iso8601,
      :repeating => {
        :period_unit => 'days',
        :period_duration => 2,
        :until_type => 'date',
        :until => repeat_until.iso8601
      }
    }

    definition = ActivityDefinition::BaseActivityDefinition.from_attributes fixed_attributes

    assert_equal 8 * 3600, definition.occurrence_max_duration
    assert_equal 8 * 3600, definition.occurrence_min_duration

    #assert_equal 16, definition.periods_count, 'Number of periods'
    #assert_equal 2, definition.period.duration, 'Period length'
    #assert definition.period.unit == TimeDomains::Duration::DAY

    # create some occurences (for nil activity, that should not be touched)
    occurences = definition.create_events nil

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
    domain_template = TimeDomains::StackTimeDomain.new
    domain_template.unshift TimeDomains::StackTimeDomain::Action.new(TimeDomains::StackTimeDomain::Action::ADD, afternoon_time)
    domain_template.unshift TimeDomains::StackTimeDomain::Action.new(TimeDomains::StackTimeDomain::Action::MASK, business_days_domain)

    attributes = {
      :type => 'floating',
      :from => start_date.iso8601,
      :to => end_date.iso8601,
      :duration_min => duration_min,
      :duration_max => duration_max,
      :domain_template => domain_template,
      :repeating => false
    }

    definition = ActivityDefinition::BaseActivityDefinition.from_attributes attributes

    #assert_equal 1, definition.periods_count
    #assert definition.period.duration >= 9
    #assert definition.period.unit == TimeDomains::Duration::DAY

    assert_equal duration_max, definition.occurrence_max_duration
    assert_equal duration_min, definition.occurrence_min_duration

    # create some occurences (for nil activity, that should not be touched)
    occurences = definition.create_events nil

    assert_equal 1, occurences.size, 'Number of occurences for fixed definition should be 1'
    assert_equal DateTime.new(2013,5,13,13,0,0), occurences[0].start, 'Start of occurence should equal to start of fixed definition'
    assert_equal duration_min, occurences[0].min_duration
    assert_equal duration_max, occurences[0].max_duration
    assert_equal duration_max, occurences[0].duration

  end


  test "create occurences from floating attributes repeating number" do
    start_date = DateTime.new 2013,5,12,10,0,0
    end_date = DateTime.new 2013,5,14,18,0,0
    duration_min = 2 * 86400
    duration_max = 4 * 86400

    # create domain with afternoon 13-18 on business days
    domain_template = TimeDomains::StackTimeDomain.new
    domain_template.unshift TimeDomains::StackTimeDomain::Action.new(TimeDomains::StackTimeDomain::Action::ADD, afternoon_time)
    domain_template.unshift TimeDomains::StackTimeDomain::Action.new(TimeDomains::StackTimeDomain::Action::MASK, business_days_domain)

    attributes = {
      :type => 'floating',
      :from => start_date.iso8601,
      :to => end_date.iso8601,
      :duration_min => duration_min,
      :duration_max => duration_max,
      :domain_template => domain_template,
      :repeating => {
        :period_unit_options => {:weekdays => [0]},
        :period_unit => 'weeks',
        :period_duration => 1,
        :until => 5,
        :until_type => 'repeats',
      }
    }

    definition = ActivityDefinition::BaseActivityDefinition.from_attributes attributes

    #assert_equal 5, definition.periods_count
    #assert definition.period.duration == 1
    #assert definition.period.unit == TimeDomains::Duration::WEEK

    # create some occurences (for nil activity, that should not be touched)
    occurences = definition.create_events nil

    assert_equal occurences.size, 5, 'Number of occurences for fixed definition should be 5'
    assert_equal DateTime.new(2013,5,13,13,0,0), occurences[0].start, 'Start of occurence should equal to start of fixed definition'
    
    occurences.each do |o|
      assert_equal duration_max, o.duration
      assert_equal duration_min, o.min_duration
      assert_equal duration_max, o.max_duration
    end
  end


  def business_days_domain
    business_days_domain = TimeDomains::StackTimeDomain.new 
    # monday
    business_days_domain.push (TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, TimeDomains::BoundlessTimeDomain.from_attributes({
          :from => DateTime.new(2013,1,7,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))
     # tuesday
    business_days_domain.push (TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, TimeDomains::BoundlessTimeDomain.from_attributes({
          :from => DateTime.new(2013,1,8,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))
     # wednesday
    business_days_domain.push (TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, TimeDomains::BoundlessTimeDomain.from_attributes( {
          :from => DateTime.new(2013,1,9,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))
     # thursday
    business_days_domain.push (TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, TimeDomains::BoundlessTimeDomain.from_attributes( {
          :from => DateTime.new(2013,1,10,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))
     # friday
    business_days_domain.push (TimeDomains::StackTimeDomain::Action.new TimeDomains::StackTimeDomain::Action::ADD, TimeDomains::BoundlessTimeDomain.from_attributes( {
          :from => DateTime.new( 2013,1,11,0,0,0).iso8601,
          :duration => {:duration => 1, :unit => 'day'},
          :period => {:duration => 1, :unit => 'week'}
        }))

    business_days_domain
  end

  def afternoon_time 
    TimeDomains::BoundlessTimeDomain.from_attributes({
      :from => DateTime.new(2013,1,1,13,0,0).iso8601,
      :duration => {:duration => 5, :unit => 'hour'},
      :period => {:duration => 1, :unit => 'day'}
    })
  end
end
