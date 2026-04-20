# app/controllers/admin/exports_controller.rb
module Admin
  class ExportsController < BaseController
    def excel
      @products = Product.order(:category, :brand, :name)
      respond_to do |format|
        format.xlsx do
          response.headers['Content-Disposition'] =
            "attachment; filename=products_#{Date.today}.xlsx"
        end
      end
    end
  end
end