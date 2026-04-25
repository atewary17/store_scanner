module Api
  module V1
    class DashboardController < BaseController
      # GET /api/v1/dashboard
      def index
        render json: {
          totals: {
            products:    Product.count,
            sessions:    ScanSession.count,
            scans:       ScanItem.count,
            scans_today: ScanItem.where(created_at: Date.today.all_day).count
          },
          by_source:       Product.group(:source).count,
          by_category:     Product.group(:category).count,
          recent_products: Product.order(created_at: :desc).limit(10).map do |p|
            { id: p.id, name: p.name, barcode: p.barcode, category: p.category, source: p.source, created_at: p.created_at }
          end,
          recent_sessions: ScanSession.order(created_at: :desc).limit(5).map do |s|
            { id: s.id, name: s.name, location: s.location, item_count: s.scan_items.count, scanned_on: s.scanned_on }
          end
        }
      end
    end
  end
end
