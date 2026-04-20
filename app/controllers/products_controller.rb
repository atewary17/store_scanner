class ProductsController < ApplicationController
  def index
    @products = Product.order(created_at: :desc)
    @products = @products.where("name ILIKE ?", "%#{params[:q]}%") if params[:q].present?
    @products = @products.where(category: params[:category])       if params[:category].present?
  end

  def show
    @product = Product.find(params[:id])
  end

  def new
    @product = Product.new
    @product.barcode = params[:barcode] if params[:barcode].present?
    @session_id = params[:session_id]
  end

  def create
    @product    = Product.new(product_params)
    @session_id = params[:session_id]
    if @product.save
      if @session_id.present?
        ScanItem.create!(scan_session_id: @session_id, product: @product, quantity: 1)
        redirect_to scan_session_path(@session_id), notice: "#{@product.name} added."
      else
        redirect_to @product, notice: 'Product created.'
      end
    else
      render :new, status: :unprocessable_entity
    end
  end

  def edit
    @product = Product.find(params[:id])
  end

  def update
    @product = Product.find(params[:id])
    if @product.update(product_params)
      redirect_to @product, notice: 'Product updated.'
    else
      render :edit, status: :unprocessable_entity
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
