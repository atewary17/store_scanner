# app/services/vision_ocr_service.rb
require 'google/cloud/vision/v1'

class VisionOcrService

  def self.extract_product_info(image_source, format: :file)
    client = Google::Cloud::Vision::V1::ImageAnnotator::Client.new

    image = case format
            when :file   then { source:  { filename: image_source } }
            when :base64 then { content: Base64.decode64(image_source) }
            when :url    then { source:  { image_uri: image_source } }
            end

    response   = client.text_detection(image: image)
    annotation = response.responses.first
    return nil unless annotation&.full_text_annotation

    raw_text = annotation.full_text_annotation.text
    Rails.logger.info "Vision OCR raw:\n#{raw_text}"

    parse_fields(raw_text)
  rescue Google::Cloud::Error => e
    Rails.logger.error "Vision OCR failed: #{e.message}"
    nil
  end

  private

  def self.parse_fields(text)
    lines = text.split("\n").map(&:strip).reject(&:empty?)

    {
      raw_text:    text,
      name:        best_name_candidate(lines),
      brand:       detect_brand(lines),
      unit:        detect_unit(text),
      description: lines.first(4).join(' '),
      source:      'google_vision'
    }
  end

  # Longest line is usually the product name on a label
  def self.best_name_candidate(lines)
    lines.max_by(&:length)
  end

  KNOWN_BRANDS = %w[
    Asian\ Paints Berger Nerolac Dulux Nippon Pidilite Fevicol
    Havells Anchor Legrand Polycab Finolex Schneider
    Ultratech ACC Ambuja Ramco JK Birla
    Jaquar Cera Hindware Supreme Astral
  ].freeze

  def self.detect_brand(lines)
    full = lines.join(' ')
    KNOWN_BRANDS.find { |b| full.match?(/#{Regexp.escape(b)}/i) }
  end

  def self.detect_unit(text)
    text.match(/\d+(\.\d+)?\s*(kg|g|ltr|l|ml|m|ft|mm|pc|pcs|pack|box|bag|set|pair)/i)
        &.to_a&.first
  end
end