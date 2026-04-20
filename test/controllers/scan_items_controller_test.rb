require "test_helper"

class ScanItemsControllerTest < ActionDispatch::IntegrationTest
  test "should get create" do
    get scan_items_create_url
    assert_response :success
  end

  test "should get update" do
    get scan_items_update_url
    assert_response :success
  end
end
