package com.storescanner.data.repository

import com.storescanner.data.api.ApiClient
import com.storescanner.data.api.TokenManager
import com.storescanner.data.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ScannerRepository(private val tokenManager: TokenManager) {

    private val api get() = ApiClient.service

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun login(password: String): Result<LoginResponse> = runCatching {
        val resp = api.login(LoginRequest(password))
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Invalid password")
    }

    // ── Scan Sessions ─────────────────────────────────────────────────────────

    suspend fun getScanSessions(): Result<List<ScanSession>> = runCatching {
        api.getScanSessions().bodyOrThrow()
    }

    suspend fun getScanSession(id: Int): Result<ScanSession> = runCatching {
        api.getScanSession(id).bodyOrThrow()
    }

    suspend fun createScanSession(name: String, location: String, notes: String): Result<ScanSession> = runCatching {
        val body = mapOf("scan_session" to ScanSessionRequest(name, location, notes))
        api.createScanSession(body).bodyOrThrow()
    }

    suspend fun getSessionSummary(id: Int): Result<SessionSummaryResponse> = runCatching {
        api.getSessionSummary(id).bodyOrThrow()
    }

    suspend fun deleteScanSession(id: Int): Result<Unit> = runCatching {
        api.deleteScanSession(id)
    }

    // ── Scan Items ────────────────────────────────────────────────────────────

    suspend fun deleteScanItem(id: Int): Result<Unit> = runCatching {
        api.deleteScanItem(id)
    }

    // ── Barcode Lookup ────────────────────────────────────────────────────────

    suspend fun lookupBarcode(barcode: String, barcodeType: String): Result<LookupResponse> = runCatching {
        api.lookupBarcode(barcode, barcodeType).bodyOrThrow()
    }

    // ── OCR Lookup ────────────────────────────────────────────────────────────

    suspend fun ocrLookup(photoFile: File, barcode: String?): Result<OcrResponse> = runCatching {
        val requestBody = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, requestBody)
        val barcodePart = (barcode ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        api.ocrLookup(photoPart, barcodePart).bodyOrThrow()
    }

    // ── Save Product ──────────────────────────────────────────────────────────

    suspend fun saveProduct(product: ProductSaveRequest, sessionId: Int, quantity: Int = 1): Result<SaveResponse> = runCatching {
        val body = mapOf<String, Any>(
            "product"    to product,
            "session_id" to sessionId,
            "quantity"   to quantity
        )
        api.saveProduct(body).bodyOrThrow()
    }

    // ── Products ──────────────────────────────────────────────────────────────

    suspend fun getProducts(query: String? = null, category: String? = null): Result<List<Product>> = runCatching {
        api.getProducts(query, category).bodyOrThrow()
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    suspend fun getDashboard(): Result<DashboardResponse> = runCatching {
        api.getDashboard().bodyOrThrow()
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    suspend fun adminDeleteProduct(id: Int): Result<Unit> = runCatching {
        api.adminDeleteProduct(id)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun <T> retrofit2.Response<T>.bodyOrThrow(): T {
        if (isSuccessful) return body()!!
        throw Exception("API error ${code()}: ${errorBody()?.string()}")
    }
}
