module Api
  module V1
    module Admin
      class ProductsController < BaseController
        # GET /api/v1/admin/products?q=...&category=...&page=1
        def index
          products = Product.order(created_at: :desc)
          products = products.where("name ILIKE ?", "%#{params[:q]}%") if params[:q].present?
          products = products.where(category: params[:category])        if params[:category].present?

          page     = [params[:page].to_i, 1].max
          per_page = 50
          total    = products.count
          products = products.offset((page - 1) * per_page).limit(per_page)

          render json: {
            products:   products.map { |p| product_json(p) },
            pagination: { page: page, per_page: per_page, total: total, pages: (total.to_f / per_page).ceil }
          }
        end

        # GET /api/v1/admin/products/:id
        def show
          render json: product_json(Product.find(params[:id]))
        end

        # PUT /api/v1/admin/products/:id
        def update
          product = Product.find(params[:id])
          if product.update(product_params)
            render json: product_json(product)
          else
            render_error(product.errors.full_messages.join(', '))
          end
        end

        # DELETE /api/v1/admin/products/:id
        def destroy
          Product.find(params[:id]).destroy
          render json: { success: true }
        end

        private

        def product_params
          params.require(:product).permit(
            :barcode, :barcode_type, :name, :brand, :category,
            :sub_category, :description, :unit, :image_url,
            :raw_qr_content, :source
          )
        end

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
end
