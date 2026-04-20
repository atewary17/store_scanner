# app/controllers/dashboard_controller.rb
class DashboardController < ApplicationController
  def index
    @total_products      = Product.count
    @total_sessions      = ScanSession.count
    @total_scans         = ScanItem.count
    @scans_today         = ScanItem.where(created_at: Time.zone.today.all_day).count
    @manual_entries      = Product.where(source: 'manual').count
    @api_entries         = Product.where(source: ['upcitemdb', 'go_upc']).count
    @vision_entries      = Product.where(source: 'google_vision').count
    @recent_sessions     = ScanSession.order(created_at: :desc).limit(5)
    @recent_products     = Product.order(created_at: :desc).limit(8)
    @categories          = Product.group(:category).count
  end
end