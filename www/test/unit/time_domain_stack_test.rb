require 'test_helper'

class TimeDomainStackTest < ActiveSupport::TestCase
  test "to hash" do
    
  	date_from = DateTime.now.iso8601
	date_to = (DateTime.now + 10).iso8601

	attributes = {
  		:actions => [
  			{
  				:action => 'add',
		  		:domain => {
		  			:type => 'bounded',
		  			:data => {:from => date_from, :to => date_to }
		  		}
		  	},
			{
  				:action => 'mask',
		  		:domain => {
		  			:type => 'boundless',
		  			:data => {:from => date_from, :duration => {:duration => 2, :unit => 'day'}, :period => {:duration => 1, :unit => 'week'} }
		  		}
  			}		  	
  		]
  	}

  	stack = TimeDomains::StackTimeDomain.from_attributes attributes

  	stack_hash =  stack.to_hash

  	assert_equal 'stack', stack_hash[:type]
  	assert_equal attributes, stack_hash[:data]
  end
end
