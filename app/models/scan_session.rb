class ScanSession < ApplicationRecord
  has_many :scan_items, dependent: :destroy
  has_many :products, through: :scan_items

  validates :name, presence: true
end
