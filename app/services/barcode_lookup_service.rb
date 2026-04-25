class BarcodeLookupService

  # Ordered cascade — first hit wins
  PROVIDERS = %i[local_db upcitemdb go_upc open_beauty].freeze

  def self.lookup(barcode, skip_local: false)
    providers = skip_local ? PROVIDERS[1..] : PROVIDERS
    providers.each do |provider|
      result = send(:"query_#{provider}", barcode)
      if result.present?
        Rails.logger.info "Barcode #{barcode} found via #{provider}"
        return result.merge(source: provider.to_s)
      end
    end
    nil
  end

  # 1. Local DB (fastest, always free)
  def self.query_local_db(barcode)
    product = Product.find_by(barcode: barcode)
    return nil unless product
    {
      name:        product.name,
      brand:       product.brand,
      category:    product.category,
      description: product.description,
      unit:        product.unit,
      image_url:   product.image_url,
      source:      'local_db',
      product_id:  product.id
    }
  end

  # 2. UPCitemdb — 100/day free, no key needed
  #    https://api.upcitemdb.com/prod/trial/lookup?upc=BARCODE
  def self.query_upcitemdb(barcode)
    response = HTTParty.get(
      "https://api.upcitemdb.com/prod/trial/lookup",
      query:   { upc: barcode },
      headers: {
        'Accept'       => 'application/json',
        'Content-Type' => 'application/json'
        # 'user_key' => ENV['UPCITEMDB_KEY']  # uncomment if you have a paid key
      },
      timeout: 5
    )

    remaining = response.headers['X-RateLimit-Remaining']
    Rails.logger.info "UPCitemdb quota remaining: #{remaining}"

    return nil unless response.code == 200
    item = response.parsed_response.dig('items', 0)
    return nil unless item

    {
      name:        item['title'],
      brand:       item['brand'],
      description: item['description'],
      unit:        item['size'],
      image_url:   item.dig('images', 0),
      source:      'upcitemdb',
      metadata:    {
        ean:      item['ean'],
        upc:      item['upc'],
        category: item['category'],
        color:    item['color'],
        model:    item['model'],
        weight:   item['weight']
      }
    }
  rescue HTTParty::Error, Net::OpenTimeout => e
    Rails.logger.warn "UPCitemdb error: #{e.message}"
    nil
  end

  # 3. Go-UPC — 100/month free, needs API key
  #    Register: https://go-upc.com/api
  #    Set GO_UPC_KEY env var to enable
  def self.query_go_upc(barcode)
    return nil unless ENV['GO_UPC_KEY'].present?

    response = HTTParty.get(
      "https://go-upc.com/api/v1/code/#{barcode}",
      headers: {
        'Authorization' => "Bearer #{ENV['GO_UPC_KEY']}",
        'Accept'        => 'application/json'
      },
      timeout: 5
    )

    return nil unless response.code == 200
    product = response.parsed_response['product']
    return nil unless product

    {
      name:        product['name'],
      brand:       product['brand'],
      description: product['description'],
      image_url:   product['imageUrl'],
      source:      'go_upc',
      metadata:    {
        category: product['category'],
        specs:    product['specs']
      }
    }
  rescue HTTParty::Error, Net::OpenTimeout => e
    Rails.logger.warn "Go-UPC error: #{e.message}"
    nil
  end

  # 4. Open Beauty Facts — free, no key
  #    Good for: soap, paint solvents, adhesives
  #    https://world.openbeautyfacts.org/api/v0/product/BARCODE.json
  def self.query_open_beauty(barcode)
    response = HTTParty.get(
      "https://world.openbeautyfacts.org/api/v0/product/#{barcode}.json",
      timeout: 5
    )

    return nil unless response.code == 200
    return nil unless response.parsed_response['status'] == 1

    p = response.parsed_response['product']
    return nil unless p

    {
      name:        p['product_name'],
      brand:       p['brands'],
      description: p['generic_name'],
      image_url:   p['image_url'],
      source:      'open_beauty',
      metadata:    {
        categories: p['categories'],
        labels:     p['labels']
      }
    }
  rescue HTTParty::Error, Net::OpenTimeout => e
    Rails.logger.warn "OpenBeautyFacts error: #{e.message}"
    nil
  end
end
