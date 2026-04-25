# db/seeds.rb — run with: rails db:seed
# Idempotent: safe to run multiple times (uses find_or_create_by!)

puts "Seeding Store Scanner..."

# ── Products ──────────────────────────────────────────────────────────────────

products_data = [
  # Paint
  {
    barcode: "8901462101015", barcode_type: "EAN13",
    name: "Asian Paints Tractor Emulsion White", brand: "Asian Paints",
    category: "Paint", sub_category: "Emulsion",
    description: "Premium interior emulsion paint, smooth finish", unit: "20L",
    source: "manual"
  },
  {
    barcode: "8901462201019", barcode_type: "EAN13",
    name: "Berger Weathercoat Anti Dustt", brand: "Berger Paints",
    category: "Paint", sub_category: "Exterior",
    description: "Exterior wall coating with anti-dust technology", unit: "10L",
    source: "upcitemdb"
  },
  {
    barcode: "8906050850015", barcode_type: "EAN13",
    name: "Nippon Paint Matex Green", brand: "Nippon Paint",
    category: "Paint", sub_category: "Emulsion",
    description: "Eco-friendly interior emulsion, low VOC", unit: "4L",
    source: "manual"
  },
  {
    barcode: "8901024001018", barcode_type: "EAN13",
    name: "Jenson Nicholson Wall Primer", brand: "Jenson & Nicholson",
    category: "Paint", sub_category: "Primer",
    description: "Oil-based wall primer for interior and exterior use", unit: "1L",
    source: "manual"
  },

  # Cement
  {
    barcode: "8906045120018", barcode_type: "EAN13",
    name: "UltraTech OPC 53 Grade Cement", brand: "UltraTech",
    category: "Cement", sub_category: "OPC",
    description: "Ordinary Portland Cement 53 grade", unit: "50kg",
    source: "manual"
  },
  {
    barcode: "8906045230010", barcode_type: "EAN13",
    name: "ACC Gold Water Shield Cement", brand: "ACC",
    category: "Cement", sub_category: "PPC",
    description: "Portland Pozzolana Cement with water-shield technology", unit: "50kg",
    source: "manual"
  },
  {
    barcode: "8901234560018", barcode_type: "EAN13",
    name: "Ambuja Plus Roof Special Cement", brand: "Ambuja",
    category: "Cement", sub_category: "Specialty",
    description: "Special cement for roof and terrace application", unit: "50kg",
    source: "manual"
  },

  # Hardware
  {
    barcode: "4710425990018", barcode_type: "EAN13",
    name: "Stanley 25mm Chisel Set (6pc)", brand: "Stanley",
    category: "Hardware", sub_category: "Hand Tools",
    description: "Chrome vanadium steel chisel set with wooden handles", unit: "6pcs",
    source: "upcitemdb"
  },
  {
    barcode: "4006209500004", barcode_type: "EAN13",
    name: "Fischer Wall Plug S6 (100pcs)", brand: "Fischer",
    category: "Hardware", sub_category: "Fasteners",
    description: "Universal polypropylene wall plugs, suitable for all wall types", unit: "100pcs",
    source: "manual"
  },
  {
    barcode: "7311518261001", barcode_type: "EAN13",
    name: "Hultafors Folding Rule 2m", brand: "Hultafors",
    category: "Hardware", sub_category: "Measuring",
    description: "Professional wooden folding rule, 10 sections", unit: "1pc",
    source: "upcitemdb"
  },

  # Electrical
  {
    barcode: "8901212501017", barcode_type: "EAN13",
    name: "Havells Crabtree 6A Switch", brand: "Havells",
    category: "Electrical", sub_category: "Switches",
    description: "Modular 6A one-way switch, white, ISI marked", unit: "10pcs",
    source: "manual"
  },
  {
    barcode: "8901396018016", barcode_type: "EAN13",
    name: "Legrand Oteo 16A Socket", brand: "Legrand",
    category: "Electrical", sub_category: "Sockets",
    description: "16A 2-pin + earth socket with shutter, white", unit: "1pc",
    source: "upcitemdb"
  },
  {
    barcode: "8906012340017", barcode_type: "EAN13",
    name: "Polycab 2.5 sqmm FR Wire 90m", brand: "Polycab",
    category: "Electrical", sub_category: "Wires",
    description: "FR PVC insulated single core copper wire, red, 90m coil", unit: "90m",
    source: "manual"
  },

  # Plumbing
  {
    barcode: "8901432100016", barcode_type: "EAN13",
    name: "Astral CPVC Pipe 3/4 inch 3m", brand: "Astral",
    category: "Plumbing", sub_category: "Pipes",
    description: "CPVC pipe for hot and cold water supply, 3/4 inch, 3 metre", unit: "3m",
    source: "manual"
  },
  {
    barcode: "8906078990011", barcode_type: "EAN13",
    name: "Supreme uPVC Ball Valve 1 inch", brand: "Supreme",
    category: "Plumbing", sub_category: "Valves",
    description: "uPVC full bore ball valve, 1 inch, ISI marked", unit: "1pc",
    source: "manual"
  },

  # Tools
  {
    barcode: "0885911456500", barcode_type: "EAN13",
    name: "Bosch GSB 550 Impact Drill", brand: "Bosch",
    category: "Tools", sub_category: "Power Tools",
    description: "550W impact drill, 13mm keyless chuck, variable speed", unit: "1pc",
    source: "upcitemdb"
  },
  {
    barcode: "6004260000015", barcode_type: "EAN13",
    name: "Black+Decker 4 inch Angle Grinder", brand: "Black+Decker",
    category: "Tools", sub_category: "Power Tools",
    description: "500W angle grinder, 100mm disc, 11000 RPM", unit: "1pc",
    source: "manual"
  },

  # Adhesive
  {
    barcode: "8901011040016", barcode_type: "EAN13",
    name: "Fevicol SH Wood Adhesive", brand: "Pidilite",
    category: "Adhesive", sub_category: "Wood Adhesive",
    description: "Synthetic resin adhesive for wood joinery and carpentry", unit: "1kg",
    source: "upcitemdb"
  },
  {
    barcode: "8901011080012", barcode_type: "EAN13",
    name: "M-Seal Epoxy Compound", brand: "Pidilite",
    category: "Adhesive", sub_category: "Epoxy",
    description: "2-part epoxy compound for sealing pipes, cracks and holes", unit: "50g",
    source: "manual"
  },
  {
    barcode: "8906041500019", barcode_type: "EAN13",
    name: "Bison Panel Adhesive", brand: "Bison",
    category: "Adhesive", sub_category: "Construction",
    description: "Contact adhesive for panels, laminates and boards", unit: "500ml",
    source: "manual"
  }
]

puts "  Creating #{products_data.size} products..."
products_data.each do |attrs|
  Product.find_or_create_by!(barcode: attrs[:barcode]) do |p|
    p.assign_attributes(attrs)
  end
end
puts "  Products: #{Product.count}"

# ── Scan Sessions ─────────────────────────────────────────────────────────────

sessions_data = [
  {
    name: "Asansol Hardware Market Survey",
    location: "GT Road Hardware Market, Asansol",
    notes: "First survey of GT Road belt — focusing on cement and hardware",
    scanned_on: Date.today - 7
  },
  {
    name: "Burnpur Building Materials Round",
    location: "Burnpur Road, Asansol",
    notes: "Paint and adhesive product range check",
    scanned_on: Date.today - 3
  },
  {
    name: "Kulti Electrical Store Audit",
    location: "Kulti Market, Burdwan",
    notes: "Electrical fittings and wiring stock verification",
    scanned_on: Date.today
  }
]

puts "  Creating #{sessions_data.size} scan sessions..."
sessions_data.each do |attrs|
  ScanSession.find_or_create_by!(name: attrs[:name]) do |s|
    s.assign_attributes(attrs)
  end
end
puts "  Sessions: #{ScanSession.count}"

# ── Scan Items ────────────────────────────────────────────────────────────────

session1 = ScanSession.find_by!(name: "Asansol Hardware Market Survey")
session2 = ScanSession.find_by!(name: "Burnpur Building Materials Round")
session3 = ScanSession.find_by!(name: "Kulti Electrical Store Audit")

scan_items_data = [
  # Session 1 — Hardware + Cement
  { session: session1, barcode: "8906045120018", quantity: 50, notes: "Main stock item" },
  { session: session1, barcode: "8906045230010", quantity: 30, notes: nil },
  { session: session1, barcode: "8901234560018", quantity: 20, notes: "Limited stock" },
  { session: session1, barcode: "4710425990018", quantity: 5,  notes: nil },
  { session: session1, barcode: "4006209500004", quantity: 20, notes: "Popular fastener" },
  { session: session1, barcode: "7311518261001", quantity: 8,  notes: nil },

  # Session 2 — Paint + Adhesive
  { session: session2, barcode: "8901462101015", quantity: 15, notes: "Best seller" },
  { session: session2, barcode: "8901462201019", quantity: 10, notes: nil },
  { session: session2, barcode: "8906050850015", quantity: 6,  notes: "New arrival" },
  { session: session2, barcode: "8901024001018", quantity: 12, notes: nil },
  { session: session2, barcode: "8901011040016", quantity: 25, notes: "High demand" },
  { session: session2, barcode: "8901011080012", quantity: 30, notes: nil },
  { session: session2, barcode: "8906041500019", quantity: 10, notes: nil },

  # Session 3 — Electrical
  { session: session3, barcode: "8901212501017", quantity: 50, notes: "Bulk pack" },
  { session: session3, barcode: "8901396018016", quantity: 20, notes: nil },
  { session: session3, barcode: "8906012340017", quantity: 8,  notes: "Red and black" },
]

puts "  Creating scan items..."
scan_items_data.each do |item|
  product = Product.find_by!(barcode: item[:barcode])
  ScanItem.find_or_create_by!(scan_session: item[:session], product: product) do |si|
    si.quantity = item[:quantity]
    si.notes    = item[:notes]
  end
end
puts "  Scan Items: #{ScanItem.count}"

puts ""
puts "Seed complete!"
puts "  Products   : #{Product.count}"
puts "  Sessions   : #{ScanSession.count}"
puts "  Scan Items : #{ScanItem.count}"
puts "  Login      : anish / #{ENV.fetch('ADMIN_PASSWORD', 'asansol')}"
