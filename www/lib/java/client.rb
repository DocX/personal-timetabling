module Webui
  module SolverClient

  	# load java classes
  	ProblemDefinitionBuilder = Rjb::import 'net.personaltt.client.ProblemDefinitionBuilder'
  	ScheduleParser = Rjb::import 'net.personaltt.client.ScheduleParser'
  	SimpleSolver = Rjb::import 'net.personaltt.simplesolver.SimpleSolver'

  	def self.build_problem_definition(occurrences)
  		definition_builder = ProblemDefinitionBuilder.new

  		occurrences.each do |o|
  			definition_builder.addOccurrence( 
  				o.id, 
  				Webui::Core::Utils.to_localdatetime(o.start), 
  				o.duration, 
  				o.min_duration, 
  				o.max_duration,
  				o.domain_definition.to_j
  			)
  		end

  		definition_builder.getDefinition
  	end

  	def self.solve(definition)
  		solver = SimpleSolver.new

  		solver.solve definition
  	end

  	# For each of given occurrence set its start and duration to allocation
  	# of the same id occcurrence in given schedule if is there
  	def self.parse_from_schedule(schedule, occurrences)
  		schedule_parser = ScheduleParser.new schedule

  		occurrences.each do |o|
  			allocation = schedule_parser.getAllocationOf(o.id)
  			unless allocation.nil?
  				o.start = Webui::Core::Utils.from_localdatetime allocation.start
  				o.duration = allocation.duration
  			end
  		end
  	end

  end
end