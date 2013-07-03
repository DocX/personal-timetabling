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
    raise 'not implemented yet'
  end
  
  def create
    @activity = Activity.new params[:activity]
    @activity.definition_attributes = params[:definition]  if params[:definition]

    if @activity.save
      respond_to do |format|
        format.json { render :json => @activity, :methods => [:event_ids] }
      end
    else 
      Rails.logger.debug 'acitivity not saved'
      Rails.logger.debug @activity.events.inspect
      respond_error_status :bad_request
    end
    
  end
  
  def destroy
    raise 'not implemented yet'
  end
  
  def list
    ids = params[:ids].split ';'
    @activities = Activity.where(:id => ids)

    respond_to do |format|
      format.json { render :json => @activities, :only=>[:name, :id], :methods => [:events_ids] }
    end    
  end
  
end