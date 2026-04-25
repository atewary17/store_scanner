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

  # Barcode lookup (AJAX — legacy PWA)
  post '/scans/lookup',     to: 'scans#lookup'
  post '/scans/ocr_lookup', to: 'scans#ocr_lookup'
  post '/scans/save',       to: 'scans#save'

  # Products
  resources :products, only: [:index, :show, :new, :create, :edit, :update]

  # Admin (web UI)
  namespace :admin do
    get    '/login',        to: 'sessions#new',     as: :login
    post   '/login',        to: 'sessions#create'
    delete '/logout',       to: 'sessions#destroy', as: :logout
    get    '/dashboard',    to: 'dashboard#index'
    get    '/exports/excel', to: 'exports#excel',   as: :exports_excel
    resources :products
  end

  # ─── Android / Mobile REST API ───────────────────────────────────────────────
  namespace :api do
    namespace :v1 do
      # Auth — no token required
      post 'auth/login', to: 'auth#login'

      # Scan sessions
      resources :scan_sessions do
        resources :scan_items, shallow: true, only: [:create, :update, :destroy]
        member { get :summary }
      end

      # Barcode + OCR lookup, save
      post 'scans/lookup',     to: 'scans#lookup'
      post 'scans/ocr_lookup', to: 'scans#ocr_lookup'
      post 'scans/save',       to: 'scans#save'

      # Product catalogue (read-only for scanner users)
      resources :products, only: [:index, :show]

      # Dashboard stats
      get 'dashboard', to: 'dashboard#index'

      # Admin sub-namespace (same JWT, but admin flag checked)
      namespace :admin do
        resources :products
        get 'exports/excel', to: 'exports#excel'
      end
    end
  end
end
