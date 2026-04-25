module Api
  module V1
    class ScansController < BaseController
      # POST /api/v1/scans/lookup
      # Body: { barcode: "...", barcode_type: "..." }
      def lookup
        barcode      = params[:barcode].to_s.strip
        barcode_type = params[:barcode_type].to_s

        result = BarcodeLookupService.lookup(barcode)

        render json: {
          found:          result&.dig(:product_id).present?,
          product:        result&.dig(:product_id) ? result : nil,
          api_suggestion: result&.dig(:product_id) ? nil : result,
          needs_manual:   result.nil?,
          barcode:        barcode,
          barcode_type:   barcode_type
        }
      end

      # POST /api/v1/scans/ocr_lookup
      # Multipart: photo (image file), barcode (optional string)
      def ocr_lookup
        uploaded = params[:photo]
        barcode  = params[:barcode]

        return render json: { success: false, message: 'No photo provided' }, status: :bad_request unless uploaded

        tmp = Rails.root.join('tmp', "ocr_#{SecureRandom.hex(8)}.jpg")
        File.binwrite(tmp, uploaded.read)
        result = VisionOcrService.extract_product_info(tmp.to_s)
        File.delete(tmp)

        if result
          render json: { success: true, data: result.merge(barcode: barcode) }
        else
          render json: { success: false, message: 'OCR could not read label' }
        end
      end

      # POST /api/v1/scans/save
      # Body: { product: { barcode, name, brand, ... }, session_id: 1, quantity: 1 }
      def save
        barcode  = params[:product][:barcode].to_s.strip
        quantity = params[:quantity].to_i.clamp(1, 9999)

        product = Product.find_or_initialize_by(barcode: barcode)
        product.assign_attributes(product_params)

        if product.save
          scan_item = ScanItem.create!(
            scan_session_id: params[:session_id],
            product:         product,
            quantity:        quantity
          )
          render json: {
            success:   true,
            scan_item: {
              id:         scan_item.id,
              quantity:   scan_item.quantity,
              product: {
                id:       product.id,
                name:     product.name,
                brand:    product.brand,
                category: product.category,
                barcode:  product.barcode,
                image_url: product.image_url
              }
            }
          }, status: :created
        else
          render json: { success: false, errors: product.errors.full_messages }, status: :unprocessable_entity
        end
      end

      private

      def product_params
        params.require(:product).permit(
          :barcode, :barcode_type, :name, :brand, :category,
          :sub_category, :description, :unit, :image_url,
          :raw_qr_content, :source
        )
      end
    end
  end
end
