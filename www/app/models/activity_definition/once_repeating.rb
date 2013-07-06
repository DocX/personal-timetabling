# Once repeating
# Virtual repeating that contains only one period
class OnceRepeating
	def self.from_attributes
		return OnceRepeating.new
	end

	def encode_with(coder)
	end

	def init_with(coder)
	end

	def get_periods(first_start, first_end)
		[[first_start, first_end]]
	end
end