Rails.application.config.middleware.insert_before 0, Rack::Cors do
  allow do
    origins '*'  # Restrict to your Android app's domain/IP in production

    resource '/api/*',
      headers: :any,
      methods: [:get, :post, :put, :patch, :delete, :options, :head],
      expose: ['Authorization']
  end
end
