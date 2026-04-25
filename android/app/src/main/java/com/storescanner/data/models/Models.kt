package com.storescanner.data.models

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(val password: String)

data class LoginResponse(
    val token: String,
    @SerializedName("expires_in") val expiresIn: Int
)

// ── Product ───────────────────────────────────────────────────────────────────

data class Product(
    val id: Int?,
    val barcode: String?,
    @SerializedName("barcode_type") val barcodeType: String?,
    val name: String?,
    val brand: String?,
    val category: String?,
    @SerializedName("sub_category") val subCategory: String?,
    val description: String?,
    val unit: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val source: String?,
    val metadata: Map<String, Any?>?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

// Used when sending a product to /scans/save
data class ProductSaveRequest(
    val barcode: String,
    @SerializedName("barcode_type") val barcodeType: String = "",
    val name: String,
    val brand: String = "",
    val category: String = "",
    @SerializedName("sub_category") val subCategory: String = "",
    val description: String = "",
    val unit: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("raw_qr_content") val rawQrContent: String = "",
    val source: String = "manual"
)

// ── Scan Session ──────────────────────────────────────────────────────────────

data class ScanSession(
    val id: Int,
    val name: String,
    val location: String?,
    val notes: String?,
    @SerializedName("scanned_on") val scannedOn: String?,
    @SerializedName("item_count") val itemCount: Int,
    val items: List<ScanItem>?,
    @SerializedName("created_at") val createdAt: String?
)

data class ScanSessionRequest(
    val name: String,
    val location: String = "",
    val notes: String = "",
    @SerializedName("scanned_on") val scannedOn: String = ""
)

// ── Scan Item ─────────────────────────────────────────────────────────────────

data class ScanItem(
    val id: Int,
    @SerializedName("product_id") val productId: Int?,
    @SerializedName("scan_session_id") val scanSessionId: Int?,
    val quantity: Int,
    val notes: String?,
    val product: ProductSummary?
)

data class ProductSummary(
    val id: Int,
    val name: String?,
    val brand: String?,
    val category: String?,
    val barcode: String?,
    @SerializedName("image_url") val imageUrl: String?
)

// ── Lookup Responses ──────────────────────────────────────────────────────────

data class LookupResponse(
    val found: Boolean,
    val product: LookupProduct?,
    @SerializedName("api_suggestion") val apiSuggestion: LookupProduct?,
    @SerializedName("needs_manual") val needsManual: Boolean,
    val barcode: String,
    @SerializedName("barcode_type") val barcodeType: String
)

data class LookupProduct(
    val name: String?,
    val brand: String?,
    val category: String?,
    val description: String?,
    val unit: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val source: String?,
    @SerializedName("product_id") val productId: Int?
)

data class OcrResponse(
    val success: Boolean,
    val data: OcrData?,
    val message: String?
)

data class OcrData(
    val name: String?,
    val brand: String?,
    val unit: String?,
    val description: String?,
    @SerializedName("raw_text") val rawText: String?,
    val source: String?,
    val barcode: String?
)

data class SaveResponse(
    val success: Boolean,
    @SerializedName("scan_item") val scanItem: SavedScanItem?,
    val errors: List<String>?
)

data class SavedScanItem(
    val id: Int,
    val quantity: Int,
    val product: ProductSummary?
)

// ── Dashboard ─────────────────────────────────────────────────────────────────

data class DashboardResponse(
    val totals: DashboardTotals,
    @SerializedName("by_source") val bySource: Map<String, Int>,
    @SerializedName("by_category") val byCategory: Map<String, Int>,
    @SerializedName("recent_products") val recentProducts: List<RecentProduct>,
    @SerializedName("recent_sessions") val recentSessions: List<RecentSession>
)

data class DashboardTotals(
    val products: Int,
    val sessions: Int,
    val scans: Int,
    @SerializedName("scans_today") val scansToday: Int
)

data class RecentProduct(
    val id: Int,
    val name: String?,
    val barcode: String?,
    val category: String?,
    val source: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class RecentSession(
    val id: Int,
    val name: String?,
    val location: String?,
    @SerializedName("item_count") val itemCount: Int,
    @SerializedName("scanned_on") val scannedOn: String?
)

// ── Session Summary ───────────────────────────────────────────────────────────

data class SessionSummaryResponse(
    val session: ScanSession,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("by_category") val byCategory: Map<String, List<ScanItem>>
)
