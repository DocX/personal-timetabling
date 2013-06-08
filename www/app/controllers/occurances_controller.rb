class OccurancesController < ApplicationController

  def index
    @occurances = Occurance.where(:activity_id => params[:activity_id])
    respond_to do |format|
      format.json { render :json => @occurances, :only => [:id, :activity_id, :start, :duration, :min_duration, :max_duration]}
    end    
  end
  
  def show
  end
  
  def update
    #@occurance = Occurance.find params[:id]
    filtered_params = params.reject {|k,v| not ['start', 'duration'].include? k}
    #@occurance.update_attributes filtered_params
    #Occurance.where(:id => params[:id]).update_all filtered_params
    Occurance.update params[:id], filtered_params
    respond_to do |format|
      format.json { render :json => true}
    end
  end
  
  def create
  end
  
  def destroy
    @occurance = Occurance.find(params[:id])
    @activity = @occurance.activity
    @occurance.destroy

    if @activity.occurances.size == 0
      @activity.destroy
    end
    
    respond_to do |format|
      format.json { render :json => true}
    end    
  end
  
  def list
    ids = params[:ids].split ';'
    @occurances = Occurance.where(:id => ids)

    respond_to do |format|
      format.json { render :json => @occurances, :except => [:end]}
    end        
  end
  
  def in_range
    start_date = DateTime.parse params[:start]
    end_date = DateTime.parse params[:end]
    
    @occurances = Occurance.in_range start_date, end_date
    
    respond_to do |format|
      format.json { render :json => @occurances, :except => [:end, :domain_definition]}
    end    
  end

  def domain_in_range
    start_date = DateTime.parse params[:start]
    end_date = DateTime.parse params[:end]
    
    @occurance = Occurance.find params[:id]

    @intervals = @occurance.domain_definition.get_intervals start_date, end_date
    
    
    respond_to do |format|
      format.json { render :json => @intervals}
    end
  end

  # resets all occurrences to its initial position
  # which is min_duration and first start of domain
  def reset
    Occurance.find_each do |o|
      o.duration = o.min_duration
      bounding_interval = o.domain_definition.bounding_interval
      o.start = o.domain_definition.get_intervals(bounding_interval.first, bounding_interval.last).first.start
      o.save
    end
    respond_to do |format|
      format.json { render :json => 'OK'}
    end
  end
  
end
