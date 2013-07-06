# Weekly repeating
# repeats day periods on week basis. Only specified weekdays are included in periods
# and only every specified multiply of week from first date
class WeeklyRepeating

	attr_accessor :period_duration, :until, :weekdays


	def self.from_attributes attributes
		weekly = WeeklyRepeating.new

		weekly.weekdays = attributes[:period_unit_options][:weekdays].map {|w| w.to_i}.sort
		weekly.period_duration = attributes[:period_duration].to_i
		weekly.until = 
			(attributes[:until_type].to_sym == :date) ? 
			DateTime.parse(attributes[:until]) :
			attributes[:until].to_i

		weekly
	end

	def encode_with(coder)
		coder['weekdays'] = @weekdays
		coder['period_duration'] = @period_duration
		coder['until'] = @until
	end

	def init_with(coder) 
		@weekdays = coder['weekdays'] || []
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

		current_start = get_next_day(current_start) if (@weekdays.index current_start.wday).nil?

		i = 0
		while not_ends.call(i, current_start)
			periods << [current_start, current_start + duration]

			current_start = get_next_day(current_start)
			i +=1
		end

		periods		
	end

	def to_attributes
		{
			:until => @until,
			:until_type => ((@until.is_a? Fixnum) ? 'repeats' : 'date'),
			:period_unit => 'weeks',
			:period_duration => @period_duration,
			:period_unit_options => {:weekdays => @weekdays}
		}
	end

	protected

	# finds next day after given date that is weekday in @weekdays
	# or first @weekday of next period, if such a date is not in the same week
	def get_next_day(date)
		wday_index = @weekdays.index date.wday
		wday_index += 1 unless wday_index.nil?

		if wday_index.nil? || wday_index >= @weekdays.size
			# go to next period's first weekday
			return ((date - date.wday) + (@period_duration * 7)) + @weekdays.first
		else
			return date + (@weekdays[wday_index] - date.wday)
		end
	end
end