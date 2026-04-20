class ScanSessionsController < ApplicationController
  def index
    @scan_sessions = ScanSession.order(created_at: :desc)
  end

  def new
    @scan_session = ScanSession.new(scanned_on: Date.today)
  end

  def create
    @scan_session = ScanSession.new(scan_session_params)
    if @scan_session.save
      redirect_to @scan_session, notice: 'Session started!'
    else
      render :new, status: :unprocessable_entity
    end
  end

  def show
    @scan_session = ScanSession.find(params[:id])
  end

  def summary
    @scan_session = ScanSession.find(params[:id])
    @scan_items   = @scan_session.scan_items.includes(:product).order(created_at: :desc)
    @by_category  = @scan_items.group_by { |i| i.product.category }
  end

  private

  def scan_session_params
    params.require(:scan_session).permit(:name, :location, :scanned_on, :notes)
  end
end
