class ApplicationController < ActionController::Base
  #protect_from_forgery

  def respond_ok_status(status)
  	respond_to do |format|
        format.json {render :json => {:ok => status} }
    end
  end

  def respond_error_status(status)
  	respond_to do |format|
        format.json {render :json => {:error => status}, :status => status}
    end
  end

  def current_user_id
    0
  end
end
