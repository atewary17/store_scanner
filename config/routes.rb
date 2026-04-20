Rails.application.routes.draw do
  root 'dashboard#index'

  # Dashboard
  get '/dashboard', to: 'dashboard#index'

  # Scanning flow
  resources :scan_sessions do
    resources :scan_items, shallow: true
    member do
      get :summary
    end
  end

  # Barcode lookup (AJAX)
  post '/scans/lookup',     to: 'scans#lookup'
  post '/scans/ocr_lookup', to: 'scans#ocr_lookup'
  post '/scans/save',       to: 'scans#save'

  # Products
  resources :products, only: [:index, :show, :new, :create, :edit, :update]

  # Admin
  namespace :admin do
    get    '/login',    to: 'sessions#new',     as: :login
    post   '/login',    to: 'sessions#create'
    delete '/logout',   to: 'sessions#destroy', as: :logout
    get    '/dashboard', to: 'dashboard#index'
    get    '/exports/excel', to: 'exports#excel', as: :exports_excel
    resources :products
  end
end
