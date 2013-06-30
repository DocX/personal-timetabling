Webui::Application.routes.draw do
  
  get "solver/schedule_future"
  get "solver/schedule_all"
  get "solver/reschedule"
  get "solver/check"
  get "solver/cancel"

  root :to => "home#index"

  resources :activities do
    
    get 'list/:ids', :on => :collection, :action => :list
    
    resources :occurances
  end
  
  resources :occurances do
    get 'list/:ids', :on => :collection, :action => :list
    get 'in_range', :on => :collection, :action => :in_range
    get 'domain', :on => :member, :action => :domain_in_range

    get :reset, :on => :collection, :action => :reset
  end

  resources :domain_templates do
    post 'preview', :on => :collection, :action => :preview
  end
    
  
end
