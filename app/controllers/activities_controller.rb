class ActivitiesController < ApplicationController
  
  def index
    @activities = Activity.includes(:occurances).all
    respond_to do |format|
      format.json { render :json => @activities,  :methods => [:occurance_ids] }
    end
  end
  
  def show
    @activity = Activity.includes(:occurances).find params[:id]
    respond_to do |format|
      format.json { render :json => @activity}
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
  
  def create
    filtered_params = params.reject {|k,v| not ['name', 'description', 'occurances', 'type'].include? k}
    filtered_params['occurances'] = filtered_params['occurances'].map {|o| Occurance.new(o)} if filtered_params.include? 'occurances' and filtered_params['occurances'].is_a? Array
    
    @activity = Activity.new(filtered_params)
    @activity.save
    respond_to do |format|
      format.json { render :json => @activity}
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
  
end
