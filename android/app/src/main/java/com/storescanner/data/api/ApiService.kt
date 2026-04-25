package com.storescanner.data.api

import com.storescanner.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ── Scan Sessions ─────────────────────────────────────────────────────────

    @GET("scan_sessions")
    suspend fun getScanSessions(): Response<List<ScanSession>>

    @GET("scan_sessions/{id}")
    suspend fun getScanSession(@Path("id") id: Int): Response<ScanSession>

    @POST("scan_sessions")
    suspend fun createScanSession(@Body request: Map<String, ScanSessionRequest>): Response<ScanSession>

    @PUT("scan_sessions/{id}")
    suspend fun updateScanSession(
        @Path("id") id: Int,
        @Body request: Map<String, ScanSessionRequest>
    ): Response<ScanSession>

    @DELETE("scan_sessions/{id}")
    suspend fun deleteScanSession(@Path("id") id: Int): Response<Map<String, Boolean>>

    @GET("scan_sessions/{id}/summary")
    suspend fun getSessionSummary(@Path("id") id: Int): Response<SessionSummaryResponse>

    // ── Scan Items ────────────────────────────────────────────────────────────

    @DELETE("scan_items/{id}")
    suspend fun deleteScanItem(@Path("id") id: Int): Response<Map<String, Boolean>>

    // ── Barcode / OCR Lookup ──────────────────────────────────────────────────

    @FormUrlEncoded
    @POST("scans/lookup")
    suspend fun lookupBarcode(
        @Field("barcode") barcode: String,
        @Field("barcode_type") barcodeType: String
    ): Response<LookupResponse>

    @Multipart
    @POST("scans/ocr_lookup")
    suspend fun ocrLookup(
        @Part photo: MultipartBody.Part,
        @Part("barcode") barcode: RequestBody
    ): Response<OcrResponse>

    @POST("scans/save")
    suspend fun saveProduct(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<SaveResponse>

    // ── Products ──────────────────────────────────────────────────────────────

    @GET("products")
    suspend fun getProducts(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null
    ): Response<List<Product>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<Product>

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    // ── Admin: Products ───────────────────────────────────────────────────────

    @GET("admin/products")
    suspend fun adminGetProducts(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @PUT("admin/products/{id}")
    suspend fun adminUpdateProduct(
        @Path("id") id: Int,
        @Body request: Map<String, ProductSaveRequest>
    ): Response<Product>

    @DELETE("admin/products/{id}")
    suspend fun adminDeleteProduct(@Path("id") id: Int): Response<Map<String, Boolean>>

    // ── Admin: Export ─────────────────────────────────────────────────────────

    @Streaming
    @GET("admin/exports/excel")
    suspend fun exportExcel(): Response<okhttp3.ResponseBody>
}
