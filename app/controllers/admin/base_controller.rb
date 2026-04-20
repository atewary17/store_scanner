# app/controllers/admin/base_controller.rb
module Admin
  class BaseController < ApplicationController
    before_action :require_admin

    private

    def require_admin
      redirect_to admin_login_path unless session[:admin]
    end
  end
end