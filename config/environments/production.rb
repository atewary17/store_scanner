Rails.application.configure do
  config.eager_load = true

  # Serve static files (Render doesn't have a separate asset server)
  config.public_file_server.enabled = true

  # Use SECRET_KEY_BASE env var directly so credentials.yml.enc is not needed
  config.secret_key_base = ENV.fetch("SECRET_KEY_BASE")

  # Log to STDOUT so Render captures it
  config.logger    = ActiveSupport::Logger.new(STDOUT)
  config.log_level = :info

  # Required for asset pipeline
  config.assets.compile = false
  config.assets.digest  = true
end
