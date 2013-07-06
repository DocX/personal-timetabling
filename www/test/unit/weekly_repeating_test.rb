require 'test_helper'

class WeeklyRepeatingTest < ActiveSupport::TestCase
  test "every week monday and friday" do
    
    date_from = DateTime.parse('2013-07-06T00:00:00')
  	date_to = DateTime.parse('2013-07-07T00:00:00')

  	repeating = WeeklyRepeating.new
    repeating.weekdays = [1,5]
    repeating.period_duration = 1
    repeating.until = 4

    # should ne
    # mon 2013-07-08
    # fri 2013-07-13
    # mon 2013-07-16
    # fri 2013-07-21

  	repeats = repeating.get_periods date_from, date_to

  	assert_equal 4, repeats.size
  	assert_equal DateTime.parse('2013-07-08T00:00:00'), repeats[0].first
    assert_equal DateTime.parse('2013-07-12T00:00:00'), repeats[1].first
    assert_equal DateTime.parse('2013-07-15T00:00:00'), repeats[2].first
    assert_equal DateTime.parse('2013-07-19T00:00:00'), repeats[3].first
  end

  test "every 2 weeks monday and friday" do
    
    date_from = DateTime.parse('2013-07-06T00:00:00')
    date_to = DateTime.parse('2013-07-07T00:00:00')

    repeating = WeeklyRepeating.new
    repeating.weekdays = [1,5]
    repeating.period_duration = 2
    repeating.until = 4

    # should ne
    # first monday after first date is in next week, so it is skipped
    # mon 2013-07-16
    # fri 2013-07-21
    # mon 2013-07-24
    # fri 2013-07-29

    repeats = repeating.get_periods date_from, date_to

    assert_equal 4, repeats.size
    assert_equal DateTime.parse('2013-07-15T00:00:00'), repeats[0].first
    assert_equal DateTime.parse('2013-07-19T00:00:00'), repeats[1].first
    assert_equal DateTime.parse('2013-07-29T00:00:00'), repeats[2].first
    assert_equal DateTime.parse('2013-08-02T00:00:00'), repeats[3].first
  end
end
