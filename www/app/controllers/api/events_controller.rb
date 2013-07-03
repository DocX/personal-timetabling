class Api::EventsController < ApplicationController

  # index of occurrences for activity_id
  def index
    @events = Event.where(:activity_id => params[:activity_id])
    respond_events
  end

  def list
    ids = params[:ids].split ';'
    @events = Event.where(:id => ids)

    respond_events    
  end  
  
  def show
    @event = Event.find(params[:id])

    respond_to do |format|
      format.json { render :json => @event, :except => [:activity, :end, :created_at, :updated_at]}
    end        
  end
  
  def update
    filtered_params = params.reject {|k,v| not ['start', 'duration'].include? k}
    @event = Event.update params[:id], params[:event].except(:end)

    respond_to do |format|
      format.json { render :json => @event, :except => [:activity, :end, :created_at, :updated_at]}
    end
  end
  
  def create
    raise 'not implemented yet'
  end
  
  def destroy
    @event = Event.find(params[:id])
    @activity = @event.activity
    @event.destroy

    if @activity and @activity.occurances.size == 0
      @activity.destroy
    end
    
    respond_to do |format|
      format.json { render :json => true}
    end    
  end
   
  def in_period
    start_date = DateTime.parse params[:from]
    end_date = DateTime.parse params[:to]
    
    @events = Event.in_range start_date, end_date
    
    respond_events 
  end

  def domain_intervals
    start_date = DateTime.parse params[:from]
    end_date = DateTime.parse params[:to]
    
    @event = Event.find params[:id]

    @intervals = @event.domain.get_intervals start_date, end_date
    
    respond_to do |format|
      format.json { render :json => @intervals}
    end
  end

 
  def reset
    if params[:id] == 'all' 
      reset_all
    end

    @event = Event.find params[:id]
    @event.reset!
   
    respond_to do |format|
      format.json { render :json => @event, :except => [:activity, :end, :created_at, :updated_at]}
    end    
  end

  protected

  def reset_all
    Event.for_each do |e|
      e.reset!
    end

    respond_to do |format|
      format.json { render :json => 'OK'}
    end
  end

  def respond_events
    respond_to do |format|
      format.json { render :json => @events, :only => [:id, :name, :start, :duration, :tz_offset, :min_duration, :max_duration, :activity_id]}
    end    
  end
  
end
