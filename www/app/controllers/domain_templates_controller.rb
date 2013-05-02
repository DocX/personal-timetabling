class DomainTemplatesController < ApplicationController

  def index
    @domain_templates = DomainTemplate.select('name, id')
  end
  
  
  def new
    @domain_template = DomainTemplate.new
    @domain_templates = DomainTemplate.all
  end
  
  def create
    @domain_template = DomainTemplate.new params[:domain_template]
    if @domain_template.save
      redirect_to :action => :index
    end
  end

  def show
    @domain_template = DomainTemplate.find(params[:id])
    
    @intervals = @domain_template.domain_stack.get_intervals (DateTime.now - 10), (DateTime.now + 10)
    
  end
  
  def destroy
    @domain_template = DomainTemplate.find(params[:id])
    @domain_template.destroy
    redirect_to :action => :index
  end
  
end
