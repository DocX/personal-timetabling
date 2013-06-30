require 'test_helper'

class SolverControllerTest < ActionController::TestCase
  test "should get new" do
    get :new
    assert_response :success
  end

  test "should get check" do
    get :check
    assert_response :success
  end

  test "should get cancel" do
    get :cancel
    assert_response :success
  end

end
