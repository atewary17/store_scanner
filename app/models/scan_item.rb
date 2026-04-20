class ScanItem < ApplicationRecord
  belongs_to :scan_session
  belongs_to :product
end
