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
  
  def new
    @activity = Activity.new
  end
  
  def create
    @activity = Activity.new params[:activity]
    

    if params[:activity][:definition] && params[:activity][:definition][:type] == 'fixed'
      @activity.occurances = ActivityDefinition.fixed(params[:activity][:definition]).create_occurences @activity
    end


    if @activity.save
      respond_to do |format|
        format.json { render :json => @activity}
        format.html { redirect_to :action => index}
      end
    else 
      respond_to do |format|
        format.json { render :json => false}
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
  
 
  
end
