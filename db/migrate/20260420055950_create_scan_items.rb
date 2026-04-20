class CreateScanItems < ActiveRecord::Migration[7.1]
  def change
    create_table :scan_items do |t|
      t.references :scan_session, null: false, foreign_key: true
      t.references :product, null: false, foreign_key: true
      t.integer :quantity
      t.text :notes
      t.string :photo

      t.timestamps
    end
  end
end
