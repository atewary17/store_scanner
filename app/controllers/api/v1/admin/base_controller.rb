module Api
  module V1
    module Admin
      class BaseController < Api::V1::BaseController
        before_action :require_admin

        private

        def require_admin
          render_unauthorized('Admin access required') unless @current_admin
        end
      end
    end
  end
end
