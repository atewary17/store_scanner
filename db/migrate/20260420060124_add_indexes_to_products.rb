class AddIndexesToProducts < ActiveRecord::Migration[7.1]
  def change
    add_index :products, :barcode, unique: true
    add_index :products, :category
    add_index :products, :brand
  end
end
