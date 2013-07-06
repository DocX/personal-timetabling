# Dayly repeating
# repeats every specified occurrence of day
class DaylyRepeating

	attr_accessor :period_duration, :until

	def self.from_attributes(attributes)
		dayly = DaylyRepeating.new

		dayly.period_duration = attributes[:period_duration].to_i
		dayly.until = 
			attributes[:until_type].to_sym == :date ? 
			DateTime.parse(attributes[:until]) :
			attributes[:until].to_i

		dayly
	end

	def encode_with(coder)
		coder['period_duration'] = @period_duration
		coder['until'] = @until
	end

	def init_with(coder) 
		@period_duration = coder['period_duration'].to_i || 1
		@until = coder['until'] || 1
	end

	# generate list of periods of repeating 
	# for specified first start and end and until type
	def get_periods(first_start, first_end) 
		periods = []

		not_ends = 
			(@until.is_a? Fixnum) ?
			lambda {|i,d| i < @until} :
			lambda {|i,d| d < @until}

		duration = first_end - first_start
		current_start = first_start

		i = 0
		while not_ends.call(i, current_start)
			periods << [current_start, current_start + duration]

			current_start += @period_duration
			i +=1
		end

		periods
	end
end