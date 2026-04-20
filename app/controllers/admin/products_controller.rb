# app/controllers/admin/products_controller.rb
module Admin
  class ProductsController < BaseController
    def index
      @products = Product.order(created_at: :desc)
                         .page(params[:page]).per(30)
      @products = @products.where("name ILIKE ?", "%#{params[:q]}%") if params[:q].present?
      @products = @products.where(category: params[:category])       if params[:category].present?
    end

    def edit
      @product = Product.find(params[:id])
    end

    def update
      @product = Product.find(params[:id])
      if @product.update(product_params)
        redirect_to admin_products_path, notice: 'Updated successfully'
      else
        render :edit
      end
    end

    def destroy
      Product.find(params[:id]).destroy
      redirect_to admin_products_path, notice: 'Deleted'
    end

    private
    def product_params
      params.require(:product).permit(
        :name, :brand, :category, :sub_category,
        :description, :unit, :image_url, :source
      )
    end
  end
end