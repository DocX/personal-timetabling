class Api::SolverController < ApplicationController
  
  @@user_solvers = {}

  SOLVER_TIMEOUT = 30000

  # start new sovler by given properties
  #
  # :problem_type
  #  problem type to create for new solver instance.
  #  :all - run solver on all events of user, even past
  #  :future - run solver on future events. sovler will not know about past events,
  #   even if their domain is also in future
  #  :list - run solver for given list of events, adding all events that are transitively
  #   connected domain with events in list. :list param must be defined
  # [:events]
  #  list of {:id, :mode} definitions for scheduler for each event. :id is id of event, :mode
  #  is one of :repair or :added. :repair means event will have higher priority than all others, 
  #  causing others should move to make room for that, :added means that event will have lower 
  #  priority of all others, effectively causing less changes possible
  def new
    case params[:problem_type].to_sym
    when :all
      schedule_all
    when :future
      schedule_future
    when :list
      reschedule params[:events]
    else
      respond_error_status :bad_request
    end
  end

  # starts solver for rescheduling all occurrences in database
  #
  # responses are
  # :state => :done 
  #   if  newly started solver is done
  # :state => :runinning
  #   if newly started solver is running
  # :error => :not_started
  #   if requested solver run was not started. main reason could be that there is another solver already running. 
  #   check using check request
  def schedule_all
    return unless require_done

    # select all future occurrences
    occurrences_to_schedule = Event.all

    problem_builder = PersonalTimetablingAPI::ProblemDefinition.global_reschedule_problem occurrences_to_schedule
  
    solver_id = PersonalTimetablingAPI::SolverClient.run_solver(
      problem_builder.getDefinition,
      SOLVER_TIMEOUT, 
      {:user_id => current_user_id}
    )
    @@user_solvers[current_user_id] = solver_id

    respond_new
  end


  # starts solver for rescheduling
  # all future occurrenes without prioritizing them
  #
  # responses are
  # :state => :done 
  #   if  newly started solver is done
  # :state => :runinning
  #   if newly started solver is running
  # :error => :not_started
  #   if requested solver run was not started. main reason could be that there is another solver already running. 
  #   check using check request
  def schedule_future
    return unless require_done

    # select all future occurrences
    occurrences_to_schedule = Event.future

    problem_builder = PersonalTimetablingAPI::ProblemDefinition.global_reschedule_problem occurrences_to_schedule
    PersonalTimetablingAPI::ProblemDefinition.crop_to_future_of(problem_builder, DateTime.now)

    solver_id = PersonalTimetablingAPI::SolverClient.run_solver(
      problem_builder.getDefinition,
      SOLVER_TIMEOUT,
      {:user_id => current_user_id}
    )
    @@user_solvers[current_user_id] = solver_id

    respond_new
  end

  # reschedules given occurrences with given priority mode
  # in context of only future occurrences
  # accepts events parameter
  # with array of {:id => occurrence_id, :mode => [:repair, :added]}
  #
  # responses are
  # :state => :done 
  #   if  newly started solver is done
  # :state => :runinning
  #   if newly started solver is running
  # :error => :not_started
  #   if requested solver run was not started. main reason could be that there is another solver already running. 
  #   check using check request
  def reschedule(events)
    return unless require_done

  	occurrences = Event.includes(:activity).find(events.map{|o| o[:id] }) 
    future_occurrences = Event.includes(:activity).future
    modes = {}
    events.each do |m|
      modes[m[:id]] = m[:mode].to_sym
    end

    problem_definition = PersonalTimetablingAPI::ProblemDefinition.transitive_priortiy_problem occurrences, future_occurrences, modes
    PersonalTimetablingAPI::ProblemDefinition.crop_until(problem_builder, DateTime.now)
    
    # create and run solver in new thread, timeout 60s
    solver_id = PersonalTimetablingAPI::SolverClient.run_solver(
      problem_builder.getDefinition,
      SOLVER_TIMEOUT, 
      {:user_id => current_user_id}
    )
    @@user_solvers[current_user_id] = solver_id

    respond_new
  end

  # checks state of running solver
  # if solver is done, it saves its result and returns solution diff to database state
  # if solver is not dome, returns current best diff to database-state
  #
  # responses are
  # :state => :done 
  #   if  previously started solver is done and saved its best result, or no previously started solver was found
  # :state => :runinning
  #   if currently solver is running
  def check
    state = check_done

    respond_to do |format|
      format.json { render :json => {:state => state }} 
    end
  end

  # cancels currently running solver if any. 
  #
  # response is in all cases
  # :state => :done
  def cancel
	  unless @@user_solvers[current_user_id].nil? 
      PersonalTimetablingAPI::SolverClient.delete(@@user_solvers[current_user_id])
      @@user_solvers.delete current_user_id
    end

    respond_to do |format|
        format.json { render :json => {:state => :none }} 
    end
  end

  # return changes in best solution, if solver for current user is done
  # if solver is running, state=running messages is send in response
  # if no solver is there for user, state=none message is send in response
  def get_best
    state = check_done

    if state == :done
      changed_events = []

      PersonalTimetablingAPI::SolverClient.set_best_solution(@@user_solvers[current_user_id]) {|id, start, duration|
        e = Event.select([:id,:name,:start, :duration]).find(id)
        e.attributes = {:start => start, :duration => duration}
        changed_events << e if e.changed?
      }

      respond_to do |format|
        format.json { render :json => {:state => state, :changed_events => changed_events }} 
      end
    else
      respond_to do |format|
        format.json { render :json => {:state => state }} 
      end
    end
  end

  def save_best
    state = check_done

    if state == :done
      do_save_best
      respond_to do |format|
        format.json { render :json => {:state => :none }} 
      end
    else
      respond_to do |format|
        format.json { render :json => {:state => state }} 
      end
    end
  end

  protected 

  def require_done
    # check existing
    if check_done == :running
      respond_to do |format|
        format.json { render :json => {:error => :not_started}} 
      end
      return false
    end

    return true
  end

  def respond_new
    if wait_for_done(1000)
      # done
      respond_to do |format|
        format.json { render :json => {:state => :done}} 
      end
    else
      respond_to do |format|
        format.json { render :json => {:state => :running}} 
      end
    end
  end

  # waits given amount of ms, then checks if solver is done
  # and if yes, saves best result and return true
  # otherwise false
  def wait_for_done(milliseconds)
    # wait 500ms for solution, otherwise respond with solver id
    sleep 0.5

    return check_done
  end

  # if currently exists done solver, save its state and clear it
  # returns if new solver can be started
  def check_done
    if @@user_solvers[current_user_id].nil? 
      return :none
    end

    begin
      solver_id = @@user_solvers[current_user_id]
      if PersonalTimetablingAPI::SolverClient.is_done(solver_id)
        return :done
      end
      return :running
    rescue
      @@user_solvers.delete current_user_id
      raise 
    end
  end


  # saves best solution of solver and removes it from solvers
  def do_save_best
    if @@user_solvers[current_user_id].nil? 
      return false
    end

  	solving = PersonalTimetablingAPI::SolverClient.set_best_solution(@@user_solvers[current_user_id]) {|id, start, duration|
      Event.update(id, :start => start, :duration => duration)
    }

    PersonalTimetablingAPI::SolverClient.delete(@@user_solvers[current_user_id])
    @@user_solvers.delete current_user_id

    return true
  end
end
