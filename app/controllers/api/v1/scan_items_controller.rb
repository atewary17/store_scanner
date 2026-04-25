module Api
  module V1
    class ScanItemsController < BaseController
      # POST /api/v1/scan_sessions/:scan_session_id/scan_items
      def create
        item = ScanItem.new(item_params)
        if item.save
          render json: scan_item_json(item), status: :created
        else
          render_error(item.errors.full_messages.join(', '))
        end
      end

      # PATCH/PUT /api/v1/scan_items/:id
      def update
        item = ScanItem.find(params[:id])
        if item.update(item_params)
          render json: scan_item_json(item)
        else
          render_error(item.errors.full_messages.join(', '))
        end
      end

      # DELETE /api/v1/scan_items/:id
      def destroy
        ScanItem.find(params[:id]).destroy
        render json: { success: true }
      end

      private

      def item_params
        params.require(:scan_item).permit(:scan_session_id, :product_id, :quantity, :notes)
      end

      def scan_item_json(item)
        {
          id:              item.id,
          scan_session_id: item.scan_session_id,
          product_id:      item.product_id,
          quantity:        item.quantity,
          notes:           item.notes,
          created_at:      item.created_at
        }
      end
    end
  end
end
