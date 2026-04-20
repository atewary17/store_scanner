class ScanItemsController < ApplicationController
  def create
    @scan_session = ScanSession.find(params[:scan_session_id])
    @scan_item    = @scan_session.scan_items.build(scan_item_params)
    if @scan_item.save
      redirect_to @scan_session, notice: 'Item added.'
    else
      redirect_to @scan_session, alert: @scan_item.errors.full_messages.join(', ')
    end
  end

  def update
    @scan_item = ScanItem.find(params[:id])
    if @scan_item.update(scan_item_params)
      redirect_to @scan_item.scan_session, notice: 'Item updated.'
    else
      redirect_to @scan_item.scan_session, alert: @scan_item.errors.full_messages.join(', ')
    end
  end

  private

  def scan_item_params
    params.require(:scan_item).permit(:product_id, :quantity, :notes)
  end
end
