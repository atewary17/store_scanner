module Api
  module V1
    class BaseController < ActionController::API
      before_action :authenticate!

      private

      def authenticate!
        token = request.headers['Authorization']&.split(' ')&.last
        return render_unauthorized unless token

        begin
          payload = JWT.decode(token, jwt_secret, true, algorithms: ['HS256']).first
          @current_admin = payload['admin']
        rescue JWT::ExpiredSignature
          render_unauthorized('Token expired')
        rescue JWT::DecodeError
          render_unauthorized
        end
      end

      def jwt_secret
        Rails.application.secret_key_base
      end

      def render_unauthorized(message = 'Unauthorized')
        render json: { error: message }, status: :unauthorized
      end

      def render_error(message, status: :unprocessable_entity)
        render json: { error: message }, status: status
      end
    end
  end
end
