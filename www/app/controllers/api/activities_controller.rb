class Api::ActivitiesController < ApplicationController
  
  def index
    @activities = Activity.all
    respond_to do |format|
      format.json { render :json => @activities, :only=>[:name, :id], :methods => [:event_ids] }
    end
  end
  
  def show
    if params[:event_id] 
      params[:id] = (Event.find params[:event_id]).activity_id
    end

    @activity = Activity.find params[:id]
    respond_to do |format|
      format.json { render :json => @activity, :exclude=> [:definition, :events], :methods=>[:event_ids]}
    end
  end
  
  def update
    # updates activity
    # update can be of two types:
    # - update of activity properties, such a name
    # - activity definition
    # 
    # in activity definition can be updated 
    # - domain template
    # - first window end
    # - first window start
    # - repeating definition

    # activity properties - changes them.
    # definition domain template - update events domains using definition
    # first window end - update events domains and schedule window by definition
    # repeating definition - merges changes 
    #   create new events, then go one by one by creation order
    #   for match - update current event domain and check allocation, move it to nearest in
    #   domain place
    #   new - create new event and associate with activity
    #   extra - remove event

    @activity = Activity.find params[:id]

  end
  
  def create
    @activity = Activity.new params.except(:definition, :controller, :action)
    
    if @activity.save
      @activity.definition_attributes = params[:definition]  if params[:definition]
      @activity.save
      
      respond_to do |format|
        format.json { render :json => @activity, :methods => [:event_ids] }
      end
    else 
      Rails.logger.debug 'acitivity not saved'
      Rails.logger.debug @activity.inspect
      respond_error_status :bad_request
    end
    
  end
  
  def destroy
    @activity = Activity.find params[:id]
    @activity.destroy

    respond_ok_status :ok
  end
  
  def list
    ids = params[:ids].split ';'
    @activities = Activity.where(:id => ids)

    respond_to do |format|
      format.json { render :json => @activities, :only=>[:name, :id], :methods => [:events_ids] }
    end    
  end
  
end
