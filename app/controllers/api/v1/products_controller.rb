module Api
  module V1
    class ProductsController < BaseController
      # GET /api/v1/products?q=paint&category=Paint&barcode=123
      def index
        products = Product.order(created_at: :desc)
        products = products.where("name ILIKE ?", "%#{params[:q]}%")  if params[:q].present?
        products = products.where(category: params[:category])         if params[:category].present?
        products = products.where(barcode: params[:barcode])           if params[:barcode].present?
        render json: products.map { |p| product_json(p) }
      end

      # GET /api/v1/products/:id
      def show
        product = Product.find(params[:id])
        render json: product_json(product)
      end

      private

      def product_json(p)
        {
          id:           p.id,
          barcode:      p.barcode,
          barcode_type: p.barcode_type,
          name:         p.name,
          brand:        p.brand,
          category:     p.category,
          sub_category: p.sub_category,
          description:  p.description,
          unit:         p.unit,
          image_url:    p.image_url,
          source:       p.source,
          metadata:     p.metadata,
          created_at:   p.created_at,
          updated_at:   p.updated_at
        }
      end
    end
  end
end
