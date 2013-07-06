require 'test_helper'

class Api::ActivitiesControllerTest < ActionController::TestCase
  # test "the truth" do
  #   assert true
  # end

  test "create new activty using fixed definition" do
  	params = {
  		"name"=>"TEST ACTIVTY",
  		"definition"=>{
  		  	"type"=>"fixed", 
  		  	"from"=>"2013-05-14T11:00:00.000Z",
  		  	"to"=>"2013-05-14T15:00:00.000Z", 
  		  	"repeating"=>false
  		  	},
  		}.with_indifferent_access

  	post :create, params

    assert_response :success
    assert_not_nil assigns(:activity)

  	activity = Activity.find_by_name 'TEST ACTIVITY'


  	assert_not_nil activity
  	assert_equal 1, activity.events.size
    assert_equal DateTime.parse(params[:definition][:from]), activity.events.first.schedule_since
    assert_equal DateTime.parse(params[:definition][:to]), activity.events.first.schedule_deadline

  end
end
