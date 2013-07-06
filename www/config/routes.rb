PersonalTimetablingAPI::Application.routes.draw do
  
  namespace :api, :path=>'', :defaults => {:format => :json} do

    # users resources
    # access to user data and creating new users
    scope '/users' do
      get '/:id', to: 'users#show'
      post '/', to: 'user#create'
      delete '/:id', to: 'user#destroy'
      put '/:id', to: 'user#update'
    end

    # user session resource
    # handles user's session creation and invalidation
    scope '/user_session' do 
      # get current session info
      get '/', to: 'user_session#show'
      # create new session
      post '/', to: 'user_session#crate'
      # delete session, invalidating auth token
      delete '/', to: 'user_session#destroy'
    end

    # events resources
    # access and handling event occurrences
    scope '/events' do
      # collection
      # list of events with given, comma separated, ids
      get '/list/:ids', to: 'events#list'
      # list of events that intersects given period
      get '/in_period(/:from$:to)', to: 'events#in_period'
      # new event
      post '/', to: 'events#create'

      # member
      # show given id's event
      get '/:id', to: 'events#show'
      # updates event
      put '/:id', to: 'events#update'
      # destroy single event 
      delete '/:id', to: 'events#destroy'
      # get intervals of domain in given period
      get '/:id/domain_intervals(/:from$:to)', to: 'events#domain_intervals'
      # get activity object of event with given :id
      get '/:event_id/activity', to: 'activity#show'

      # reset to initial allocation. id can be "all" for reset all events
      post '/:id/reset', to: 'events#reset'     
    end

    # activities resources
    # activity is logical set of events defined by activity definition
    scope '/activities' do
      get '/', to: 'activities#index'
      get '/list/:ids', to: 'activities#list'
      post '/', to: 'activities#create'

      get '/:id', to: 'activities#show'
      put '/:id', to: 'activities#update' 
      get '/:activity_id/events', to: 'events#index'
      delete '/:id', to: 'activities#destroy'
    end

    # domain templates api
    # storing domain templates and resolving their previews
    scope '/domain_templates' do 
      get '/', to: 'domain_templates#index'
      post '/', to: 'domain_templates#create'
      post '/domain_intervals(/:from$:to)', to: 'domain_templates#new_domain_intervals'

      get '/:id', to: 'domain_templates#show'
      put '/:id', to: 'domain_templates#update'
      get '/:id/domain_intervals(/:from$:to)', to: 'domain_templates#domain_intervals'
      delete '/:id', to: 'domain_templates#destroy'
    end

    # scheduler interface
    # starting and checking schedulers
    scope '/scheduler' do
      post "/", to: 'solver#new'
      
      # check if solver for user is running or is done
      get "/", to: 'solver#check'

      # checks if solver is done and if yes returns events changed from current state in best solution
      get "/best", to: 'solver#get_best'

      # stops currently running solver and saves its best result
      post "/best", to: 'solver#save_best'

      # stops currently running solver
      delete "/", to: 'solver#cancel'
    end

  end
  
end
