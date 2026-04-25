module Api
  module V1
    module Admin
      class ExportsController < BaseController
        # GET /api/v1/admin/exports/excel
        # Returns an .xlsx file of all products
        def excel
          products = Product.order(:category, :name)

          package = Axlsx::Package.new
          wb = package.workbook

          wb.add_worksheet(name: 'Products') do |sheet|
            sheet.add_row %w[ID Barcode Name Brand Category SubCategory Unit Description Source CreatedAt]
            products.each do |p|
              sheet.add_row [
                p.id, p.barcode, p.name, p.brand, p.category,
                p.sub_category, p.unit, p.description, p.source,
                p.created_at.strftime('%Y-%m-%d %H:%M')
              ]
            end
          end

          send_data package.to_stream.read,
                    filename:    "products_#{Date.today}.xlsx",
                    type:        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                    disposition: 'attachment'
        end
      end
    end
  end
end
