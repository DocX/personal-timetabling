class OccurancesController < ApplicationController

  def index
  end
  
  def show
  end
  
  def update
    @occurance = Occurance.find params[:id]
    filtered_params = params.reject {|k,v| not ['start', 'duration'].include? k}
    @occurance.update_attributes filtered_params
    respond_to do |format|
      format.json { render :json => @occurance}
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
      format.json { render :json => @occurances}
    end        
  end
  
end
