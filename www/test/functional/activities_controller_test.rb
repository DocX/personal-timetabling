require 'test_helper'

class ActivitiesControllerTest < ActionController::TestCase
  # test "the truth" do
  #   assert true
  # end

  test "create new activty using fixed definition" do
  	params = {
  		"name"=>"TEST ACTIVTY",
  		"description"=>"Fixed activity in the TEST",
  		"definition"=>{
  		  	"type"=>"fixed", 
  		  	"from"=>"2013-05-14T11:00:00.000Z",
  		  	"to"=>"2013-05-14T15:00:00.000Z", 
  		  	"repeating"=>false
  		  	},
  		}

  	post :create, params

  	activity = Activity.find_by_name 'TEST ACTIVITY'

  	assert_not_nil activity
  	assert_equal 1, activity.occurances.size

  end
end
