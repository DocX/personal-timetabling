Webui::Application.routes.draw do
  root :to => "home#index"

  resources :activities do
    
    get 'list/:ids', :on => :collection, :action => :list
    
    resources :occurances
  end
  
  resources :occurances do
    get 'list/:ids', :on => :collection, :action => :list
  end

end
