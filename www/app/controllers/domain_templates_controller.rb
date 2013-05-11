class DomainTemplatesController < ApplicationController

  def index
    @domain_templates = DomainTemplate.select('name, id')

    respond_to do |format|
      format.html {render}
      format.json {render :json => @domain_templates, :only => [:name, :id]}
    end
  end
  
  
  def new
    @domain_template = DomainTemplate.new
    @domain_templates = DomainTemplate.all
  end
  
  def create
    params[:domain_template][:domain_stack_attributes] = get_native params[:domain_template][:domain_stack_attributes]
    @domain_template = DomainTemplate.new params[:domain_template]
    if @domain_template.save
      redirect_to :action => :index
    end
  end

  def show
    @domain_template = DomainTemplate.find(params[:id])
    #@domain_template.domain_stack.to_j
    
    begin
      @intervals_from = params[:from] ? DateTime.iso8601(params[:from]) : (DateTime.now - 10) 
      @intervals_to = params[:to] ? DateTime.iso8601(params[:to]) : (DateTime.now + 10)
      @intervals = @domain_template.domain_stack.get_intervals @intervals_from, @intervals_to
    rescue
      @intervals = []
    end
     
    respond_to do |format|
      format.html {render}
      format.json {render :json => @intervals}
    end
  end
  
  def destroy
    @domain_template = DomainTemplate.find(params[:id])
    @domain_template.destroy
    redirect_to :action => :index
  end
  
  def preview
    stack = TimeDomainStack.from_attributes get_native(params['domain_stack'])
    
    @intervals_from = params[:from] ? DateTime.iso8601(params[:from]) : (DateTime.now - 10) 
    @intervals_to = params[:to] ? DateTime.iso8601(params[:to]) : (DateTime.now + 10)
    
    @intervals = stack.get_intervals @intervals_from, @intervals_to
    
    respond_to do |format|
      format.json { render :json => @intervals}
    end
  end
  
  protected 
  
  def get_native attributes
    new_attributes = {}
    attributes = attributes.each do |k,a|
      if a['type'] == 'database'
        a = {'action' => a['action'], 'type' => 'raw', 'object' => DomainTemplate.find(a['domain_template_id']).domain_stack}
      end
      new_attributes[k] = a
    end
    new_attributes
  end
  
end
