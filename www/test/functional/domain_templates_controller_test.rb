require 'test_helper'

class Api::DomainTemplatesControllerTest < ActionController::TestCase
  # test "the truth" do
  #   assert true
  # end

  test "create new domain template" do
  	date_from = DateTime.now.iso8601
	  date_to = (DateTime.now + 10).iso8601

  	params = {
  		"domain_template" => {
  			"name" => "TEST DOMAIN",
  			"type" => "stack",
  			"data" => {
  				"actions" => [
  					{"action" => "add", "domain" => {"type" => "bounded", "from" => date_from, "to" => date_to}}
  				]
  			}
  		}
  	}

  	post :create, params

  	assert_not_nil assigns[:domain_template]

  	domain = DomainTemplate.find_by_name 'TEST DOMAIN'

  	assert_not_nil domain
  	assert_equal 'stack', domain.domain_type
  end
end
