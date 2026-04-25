module Api
  module V1
    class AuthController < ActionController::API
      # POST /api/v1/auth/login
      # Body: { password: "..." }
      # Returns: { token: "...", expires_in: 2592000 }
      def login
        password = params[:password].to_s

        if password == ENV.fetch('ADMIN_PASSWORD', 'scanner123')
          payload = { admin: true, exp: 30.days.from_now.to_i }
          token = JWT.encode(payload, Rails.application.secret_key_base, 'HS256')
          render json: { token: token, expires_in: 30.days.to_i }
        else
          render json: { error: 'Invalid password' }, status: :unauthorized
        end
      end
    end
  end
end
