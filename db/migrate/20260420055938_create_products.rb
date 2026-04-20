class CreateProducts < ActiveRecord::Migration[7.1]
  def change
    create_table :products do |t|
      t.string :barcode
      t.string :barcode_type
      t.string :name
      t.string :brand
      t.string :category
      t.string :sub_category
      t.text :description
      t.string :unit
      t.string :image_url
      t.text :raw_qr_content
      t.string :source
      t.jsonb :metadata

      t.timestamps
    end
  end
end
