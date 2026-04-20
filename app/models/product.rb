class Product < ApplicationRecord
  has_many :scan_items, dependent: :destroy

  validates :name, presence: true
  validates :barcode, uniqueness: { allow_blank: true }
end
