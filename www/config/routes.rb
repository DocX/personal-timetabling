Webui::Application.routes.draw do
  root :to => "home#index"

  resources :activities do
    
    get 'list/:ids', :on => :collection, :action => :list
    
    resources :occurances
  end
  
  resources :occurances do
    get 'list/:ids', :on => :collection, :action => :list
    get 'in_range', :on => :collection, :action => :in_range
  end

  resources :domain_templates do
    post 'preview', :on => :collection, :action => :preview
  end
    
  
end
