# app/controllers/admin/sessions_controller.rb
module Admin
  class SessionsController < ApplicationController
    def new; end   # login form

    def create
      if params[:password] == ENV.fetch('ADMIN_PASSWORD', 'scanner123')
        session[:admin] = true
        redirect_to admin_dashboard_path
      else
        flash[:error] = 'Wrong password'
        render :new
      end
    end

    def destroy
      session.delete(:admin)
      redirect_to admin_login_path
    end
  end
end