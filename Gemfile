source "https://rubygems.org"

ruby "3.3.5"
gem "rails", "~> 7.1.6"
gem "propshaft"
gem "pg", "~> 1.1"
gem "puma", ">= 5.0"
gem "jsbundling-rails"
gem "turbo-rails"
gem "stimulus-rails"
gem "cssbundling-rails"
gem "jbuilder"
gem "tzinfo-data", platforms: %i[ windows jruby ]
gem "bootsnap", require: false
gem 'caxlsx'               # Excel export
gem 'caxlsx_rails'         # Rails integration for caxlsx
gem 'httparty'             # HTTP client for external APIs
gem 'image_processing'     # For photo uploads (label photos)
gem 'active_storage_validations'
gem 'pagy'
gem 'google-cloud-vision-v1'
gem 'rack-cors'         # CORS for Android API client
gem 'jwt'               # JWT auth tokens for API

group :development, :test do
  gem 'dotenv-rails'
  gem 'factory_bot_rails'
  gem 'faker'
  gem "debug", platforms: %i[ mri windows ]
end

group :development do
  gem "web-console"
end

group :test do
  gem "capybara"
  gem "selenium-webdriver"
end

group :production do
  gem 'rack-timeout'   # prevents hung requests
end
