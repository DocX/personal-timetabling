class OccurancesController < ApplicationController

  def index
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
      format.json { render :json => @occurances, :except => [:end]}
    end    
  end
  
end
