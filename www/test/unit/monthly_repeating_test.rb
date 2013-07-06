require 'test_helper'

class MonthlyRepeatingTest < ActiveSupport::TestCase
  test "skipping not existing date months" do
    
    date_from = DateTime.parse('2013-01-31T00:00:00')
  	date_to = DateTime.parse('2013-02-01T00:00:00')

  	repeating = MonthlyRepeating.new
    repeating.period_duration = 1
    repeating.until = 4

    # should ne
    # 2013-01-31
    # 2013-03-31
    # 2013-05-31
    # 2013-07-31

  	repeats = repeating.get_periods date_from, date_to

  	assert_equal 4, repeats.size
  	assert_equal DateTime.parse('2013-01-31T00:00:00'), repeats[0].first
    assert_equal DateTime.parse('2013-03-31T00:00:00'), repeats[1].first
    assert_equal DateTime.parse('2013-05-31T00:00:00'), repeats[2].first
    assert_equal DateTime.parse('2013-07-31T00:00:00'), repeats[3].first
  end

end
