# Monthly repeating
# repeats given interval every specified number of months
class MonthlyRepeating

	attr_accessor :period_duration, :until

	def self.from_attributes(attributes)
		monthly = MonthlyRepeating.new

		monthly.period_duration = attributes[:period_duration].to_i
		monthly.until = 
			attributes[:until_type].to_sym == :date ? 
			DateTime.parse(attributes[:until]) :
			attributes[:until].to_i
		monthly
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
		count = 0
		while not_ends.call(count, current_start)
			periods << [current_start, current_start + duration]

			i += 1
			# get first month that has given day number
			i+=1 while (first_start << -(i * @period_duration)).day != first_start.day

			current_start = (first_start << -(i * @period_duration))
			count += 1 
		end

		periods
	end
end