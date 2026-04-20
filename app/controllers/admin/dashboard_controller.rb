# app/controllers/admin/dashboard_controller.rb
module Admin
  class DashboardController < BaseController
    def index
      @total_products  = Product.count
      @total_sessions  = ScanSession.count
      @products        = Product.order(created_at: :desc).page(params[:page]).per(20)
      @by_source       = Product.group(:source).count
      @by_category     = Product.group(:category).count
    end
  end
end