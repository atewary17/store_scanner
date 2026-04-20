# app/helpers/application_helper.rb
module ApplicationHelper
  def product_emoji(category)
    map = {
      'Paint'       => '🎨',
      'Hardware'    => '🔧',
      'Cement'      => '🏗️',
      'Electrical'  => '⚡',
      'Plumbing'    => '🚿',
      'Tools'       => '🔨',
    }
    map[category] || '📦'
  end
end