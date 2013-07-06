require 'test_helper'

class Api::DomainTemplatesControllerTest < ActionController::TestCase
  # test "the truth" do
  #   assert true
  # end

  test "create new domain template" do
  	date_from = DateTime.now.iso8601
	  date_to = (DateTime.now + 10).iso8601

  	params = {
			'name' => "TEST DOMAIN",
      'domain_attributes' => {
  			"type" => "stack",
  			"data" => {
  				"actions" => [
  					{"action" => "add", "domain" => {"type" => "bounded", "from" => date_from, "to" => date_to}}
  				]
  			}
      }
  	}.with_indifferent_access

  	post :create, params

  	assert_not_nil assigns[:domain_template]

  	domain = DomainTemplate.find_by_name 'TEST DOMAIN'

  	assert_not_nil domain
  	assert domain.domain.is_a? TimeDomains::StackTimeDomain
  end
end
