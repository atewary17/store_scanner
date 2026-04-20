# config/environments/production.rb

# Serve static files (Render doesn't have a separate asset server)
config.public_file_server.enabled = true

# Log to STDOUT so Render captures it
config.logger = ActiveSupport::Logger.new(STDOUT)
config.log_level = :info

# Required for asset pipeline
config.assets.compile = false
config.assets.digest  = true