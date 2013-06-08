class ActivitiesController < ApplicationController
  
  def index
    @activities = Activity.includes(:occurances).all
    respond_to do |format|
      format.json { render :json => @activities,  :methods => [:occurance_ids]
      format.html { render :index }    
    }
      
    end
  end
  
  def show
    @activity = Activity.includes(:occurances).find params[:id]
    respond_to do |format|
      format.json { render :json => @activity, :exclude => [:domain_definition]}
    end
  end
  
  def update
    @activity = Activity.find params[:id]
    filtered_params = params.reject {|k,v| not ['name', 'description'].include? k}
    @activity.update_attributes filtered_params
    respond_to do |format|
      format.json { render :json => @activity}
    end
  end
  
  def new
    @activity = Activity.new
  end
  
  def create

    definition = params.delete :definition

    @activity = Activity.new params.except(:action, :controller, :occurances)
    

    if definition
      @activity.occurances = ActivityDefinition.from_typed(definition).create_occurences @activity
    else 
      if params[:occurances] then
        @activity.update_attributes( {:occurances => params[:occurances]} )
      end
    end


    if @activity.save
      respond_to do |format|
        format.json { render :json => @activity }
        format.html { redirect_to :action => :index}
      end
    else 
      respond_to do |format|
        format.json { render :json => {:error => @activity.errors}, :status => :bad_request}
        format.html { render 'new' }
      end
    end
    
  end
  
  def destroy
  end
  
  def list
    ids = params[:ids].split ';'
    @activities = Activity.where(:id => ids)

    respond_to do |format|
      format.json { render :json => @activities}
    end    
  end
  
  # run solver on all activities in database
  def solve
    @occurrences = Occurance.all

    problem_definition = Webui::SolverClient.build_problem_definition @occurrences
    solved_schedule = Webui::SolverClient.solve problem_definition
    Webui::SolverClient.parse_from_schedule solved_schedule, @occurrences

    # save changes
    @changed = []
    @occurrences.each do |o|
      @changed << o if o.changed?
      o.save
    end

    respond_to do |format|
      format.json { render :json => @changed, :except => [:domain_definition] }
    end
  end


  
end
