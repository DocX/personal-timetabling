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
end
