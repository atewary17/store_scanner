require "test_helper"

class Admin::ExportsControllerTest < ActionDispatch::IntegrationTest
  test "should get excel" do
    get admin_exports_excel_url
    assert_response :success
  end
end
