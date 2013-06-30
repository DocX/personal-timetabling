class SolverController < ApplicationController
  
  @@user_solvers = {}

  SOLVER_TIMEOUT = 30000

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
    occurrences_to_schedule = Occurance.all

    problem_builder = Webui::ProblemDefinition.global_reschedule_problem occurrences_to_schedule
  
    solver_id = Webui::SolverClient.run_solver(problem_builder.getDefinition,SOLVER_TIMEOUT, {:user_id => current_user_id})
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
    occurrences_to_schedule = Occurance.future

    problem_builder = Webui::ProblemDefinition.global_reschedule_problem occurrences_to_schedule
    Webui::ProblemDefinition.crop_until(problem_builder, DataTime.now)

    solver_id = Webui::SolverClient.run_solver(problem_builder.getDefinition,SOLVER_TIMEOUT, {:user_id => current_user_id})
    @@user_solvers[current_user_id] = solver_id

    respond_new
  end

  # reschedules given occurrences with given priority mode
  # in context of only future occurrences
  # accepts "occurrences_to_reschedule" parameter
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
  def reschedule
    return unless require_done

  	occurrences = Occurance.includes(:activity).find(params[:occurrences_to_reschedule].map{|o| o[:id] }) 
    future_occurrences = Occurrence.includes(:activity).future
    modes = {}
    params[:occurrences_to_reschedule].each do |m|
      modes[m[:id]] = m[:mode].to_sym
    end

    problem_definition = Webui::ProblemDefinition.transitive_priortiy_problem occurrences, future_occurrences, modes
    Webui::ProblemDefinition.crop_until(problem_builder, DataTime.now)
    
    # create and run solver in new thread, timeout 60s
    solver_id = Webui::SolverClient.run_solver(problem_builder.getDefinition,SOLVER_TIMEOUT, {:user_id => current_user_id})
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
  	if check_done
      # done
      respond_to do |format|
        format.json { render :json => {:state => :done }} 
      end
    else
      respond_to do |format|
        format.json { render :json => {:state => :running}} 
      end
    end
  end

  # cancels currently running solver if any. 
  #
  # response is in all cases
  # :state => :done
  def cancel
	  unless @@user_solvers[current_user_id].nil? 
      Webui::SolverClient.delete(@@user_solvers[current_user_id])
      @@user_solvers.delete current_user_id
    end

    respond_to do |format|
        format.json { render :json => {:state => :done }} 
    end
  end

  protected 

  def current_user_id
  	:user123
  end

  def require_done
    # check existing
    unless check_done
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
      return true
    end

    solver_id = @@user_solvers[current_user_id]
    if Webui::SolverClient.is_done(solver_id)
      # save state
      save_best
      Webui::SolverClient.delete(solver_id)
      @@user_solvers.delete current_user_id

      return true
    end
    return false
  end

  def save_best
    if @@user_solvers[current_user_id].nil? 
      return false
    end

  	solving = Webui::SolverClient.set_best_solution(@@user_solvers[current_user_id]) {|id, start, duration|
      Occurance.update(id, :start => start, :duration => duration)
    }

    return true
  end
end
