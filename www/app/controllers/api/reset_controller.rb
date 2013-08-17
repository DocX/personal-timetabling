class Api::ResetController < ApplicationController

	def purge_all
		# delete activities

		ActiveRecord::Base.connection.execute('DELETE FROM events')
		ActiveRecord::Base.connection.execute('DELETE FROM activities')
		ActiveRecord::Base.connection.execute('DELETE FROM domain_templates')
		
		respond_ok_status "destroyed"
	end

end