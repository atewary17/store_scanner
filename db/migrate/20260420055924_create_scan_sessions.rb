class CreateScanSessions < ActiveRecord::Migration[7.1]
  def change
    create_table :scan_sessions do |t|
      t.string :name
      t.string :location
      t.text :notes
      t.date :scanned_on

      t.timestamps
    end
  end
end
