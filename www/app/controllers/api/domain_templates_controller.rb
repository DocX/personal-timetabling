class Api::DomainTemplatesController < ApplicationController

  def index
    @domain_templates = DomainTemplate.select('name, id')

    respond_to do |format|
      format.json {render :json => @domain_templates, :only => [:name, :id]}
    end
  end
  
  def create
    @domain_template = DomainTemplate.new params.except(:controller, :action)
    if @domain_template.save
      respond_to do |format|
        format.json {render :json => @domain_template, :only => [:name, :id, :domain_attributes]}
      end
    else
      respond_error_status :server_error
    end
  end

  def show
    @domain_template = DomainTemplate.find(params[:id])
    
    respond_to do |format|
      format.json {render :json => @domain_template, :only => [:name, :id, :domain_attributes], :methods=>[:domain_attributes]}
    end
  end

  def update
    @domain_template = DomainTemplate.find(params[:id])

    if @domain_template.update_attributes(params.slice(:name,:domain_attributes))
      respond_ok_status :updated
    else
      respond_error_status :bad_request
    end

  end
  
  def destroy
    @domain_template = DomainTemplate.find(params[:id])
    
    if @domain_template.destroy
      respond_ok_status :destroyed
    else
      if @domain_template.is_referenced? 
        respond_error_status :method_not_allowed, :is_referenced 
      else
        respond_error_status :method_not_allowed
      end
    end
  end
  
  def new_domain_intervals
    @domain_template = DomainTemplate.new params[:domain_template]
    
    respond_domain_intervals
  end

  def domain_intervals
    @domain_template = DomainTemplate.find params[:id]
    
    respond_domain_intervals
  end


  protected 

  def respond_domain_intervals()
    @intervals_from = DateTime.iso8601(params[:from])
    @intervals_to = DateTime.iso8601(params[:to])
    @intervals = @domain_template.domain.get_intervals @intervals_from, @intervals_to
    
    respond_to do |format|
      format.json {render :json => @intervals}
    end
  end
  
end
