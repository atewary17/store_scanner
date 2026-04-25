module Api
  module V1
    class ScanSessionsController < BaseController
      # GET /api/v1/scan_sessions
      def index
        sessions = ScanSession.order(created_at: :desc)
        render json: sessions.map { |s| session_json(s) }
      end

      # GET /api/v1/scan_sessions/:id
      def show
        scan_session = ScanSession.includes(scan_items: :product).find(params[:id])
        render json: session_json(scan_session, include_items: true)
      end

      # POST /api/v1/scan_sessions
      def create
        scan_session = ScanSession.new(session_params)
        if scan_session.save
          render json: session_json(scan_session), status: :created
        else
          render_error(scan_session.errors.full_messages.join(', '))
        end
      end

      # PUT /api/v1/scan_sessions/:id
      def update
        scan_session = ScanSession.find(params[:id])
        if scan_session.update(session_params)
          render json: session_json(scan_session)
        else
          render_error(scan_session.errors.full_messages.join(', '))
        end
      end

      # DELETE /api/v1/scan_sessions/:id
      def destroy
        ScanSession.find(params[:id]).destroy
        render json: { success: true }
      end

      # GET /api/v1/scan_sessions/:id/summary
      def summary
        scan_session = ScanSession.includes(scan_items: :product).find(params[:id])
        by_category = scan_session.scan_items
          .group_by { |i| i.product&.category || 'Unknown' }
          .transform_values { |items| items.map { |i| scan_item_json(i) } }

        render json: {
          session:     session_json(scan_session),
          total_items: scan_session.scan_items.count,
          by_category: by_category
        }
      end

      private

      def session_params
        params.require(:scan_session).permit(:name, :location, :notes, :scanned_on)
      end

      def session_json(s, include_items: false)
        json = {
          id:          s.id,
          name:        s.name,
          location:    s.location,
          notes:       s.notes,
          scanned_on:  s.scanned_on,
          item_count:  s.scan_items.size,
          created_at:  s.created_at,
          updated_at:  s.updated_at
        }
        json[:items] = s.scan_items.map { |i| scan_item_json(i) } if include_items
        json
      end

      def scan_item_json(item)
        {
          id:          item.id,
          product_id:  item.product_id,
          quantity:    item.quantity,
          notes:       item.notes,
          product:     item.product ? product_summary(item.product) : nil
        }
      end

      def product_summary(p)
        { id: p.id, name: p.name, brand: p.brand, category: p.category, barcode: p.barcode, image_url: p.image_url }
      end
    end
  end
end
