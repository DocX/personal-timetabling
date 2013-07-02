module PersonalTimetablingAPI
  module SolverClient

  	# load java classes
  	ScheduleParser = Rjb::import 'net.personaltt.client.ScheduleParser'
  	SolverClient = Rjb::import 'net.personaltt.client.SolverClient'
    IntervalsTimeDomainUtils = Rjb::import 'net.personaltt.timedomain.IntervalsTimeDomainUtils'

    @@solver_counter = 0
    @@solvers = {}

    class SolverInstance
      attr_accessor :solver, :data

      def initialize(solver, data); @solver = solver; @data = data; end;
    end


    # creates new solver thread in Java VM, start solving and return id
  	def self.run_solver(definition, timeout, data)
  		solver = SolverClient.new
      solver.solve definition, timeout 

      @@solver_counter += 1
      @@solvers[@@solver_counter] = SolverInstance.new solver, data

      return @@solver_counter
  	end

    def self.get_done_solvers() 
      done = []
      @@solvers.each do |i,s|
        unless s.solver.isRunning
          done << s
          @@solvers.delete i
        end
      end

      done
    end

    def self.is_done(solver_id) 
      if @@solvers[solver_id].nil?
        return true
      end

      return (@@solvers[solver_id].solver.isRunning) == false
    end

  	# Retrieves allocations from best solution of given solver and calls
    # set_allocation block to handle it
    #
    # set_allocation block should receive 3 params: id, start, duration
  	def self.set_best_solution(solver_id, &set_allocation)
      if @@solvers[solver_id].nil?
        return false
      end

      solver = @@solvers[solver_id].solver
  		schedule_parser = ScheduleParser.new solver.getCurrentBest

      allocations = PersonalTimetablingAPI::Core::Utils::j_list_to_ary(schedule_parser.getAllocations) {|a| a}

      allocations.each do |allocation|
  			unless allocation.nil?
          set_allocation.call(allocation.id, PersonalTimetablingAPI::Core::Utils.from_localdatetime(allocation.start), allocation.duration)
  			end
  		end

      true
  	end

    def self.delete(solver_id) 
      if @@solvers[solver_id].nil?
        return true
      end

      solver = @@solvers[solver_id].solver
      if (solver.isRunning)
        # stop it and remove
        solver.stop();
      end

      @@solvers.delete solver_id
    end
   

  end
end