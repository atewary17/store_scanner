package com.storescanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.storescanner.data.api.TokenManager
import com.storescanner.data.models.*
import com.storescanner.data.repository.ScannerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    val tokenManager = TokenManager(application)
    private val repo = ScannerRepository(tokenManager)

    // ── Auth ──────────────────────────────────────────────────────────────────

    private val _loginState = MutableLiveData<UiState<String>>()
    val loginState: LiveData<UiState<String>> = _loginState

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    init {
        checkToken()
    }

    private fun checkToken() {
        viewModelScope.launch {
            val token = tokenManager.tokenFlow.first()
            _isLoggedIn.value = token != null
        }
    }

    fun login(password: String) {
        _loginState.value = UiState.Loading
        viewModelScope.launch {
            repo.login(password).fold(
                onSuccess = { response ->
                    tokenManager.saveToken(response.token)
                    _isLoggedIn.value = true
                    _loginState.value = UiState.Success(response.token)
                },
                onFailure = { _loginState.value = UiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _isLoggedIn.value = false
        }
    }

    // ── Scan Sessions ─────────────────────────────────────────────────────────

    private val _sessions = MutableLiveData<UiState<List<ScanSession>>>()
    val sessions: LiveData<UiState<List<ScanSession>>> = _sessions

    private val _currentSession = MutableLiveData<UiState<ScanSession>>()
    val currentSession: LiveData<UiState<ScanSession>> = _currentSession

    private val _sessionSummary = MutableLiveData<UiState<SessionSummaryResponse>>()
    val sessionSummary: LiveData<UiState<SessionSummaryResponse>> = _sessionSummary

    fun loadSessions() {
        _sessions.value = UiState.Loading
        viewModelScope.launch {
            repo.getScanSessions().fold(
                onSuccess = { _sessions.value = UiState.Success(it) },
                onFailure = { _sessions.value = UiState.Error(it.message ?: "Failed to load sessions") }
            )
        }
    }

    fun loadSession(id: Int) {
        _currentSession.value = UiState.Loading
        viewModelScope.launch {
            repo.getScanSession(id).fold(
                onSuccess = { _currentSession.value = UiState.Success(it) },
                onFailure = { _currentSession.value = UiState.Error(it.message ?: "Failed to load session") }
            )
        }
    }

    fun createSession(name: String, location: String, notes: String, onSuccess: (ScanSession) -> Unit) {
        viewModelScope.launch {
            repo.createScanSession(name, location, notes).fold(
                onSuccess = { onSuccess(it) },
                onFailure = { _sessions.value = UiState.Error(it.message ?: "Failed to create session") }
            )
        }
    }

    fun loadSessionSummary(id: Int) {
        _sessionSummary.value = UiState.Loading
        viewModelScope.launch {
            repo.getSessionSummary(id).fold(
                onSuccess = { _sessionSummary.value = UiState.Success(it) },
                onFailure = { _sessionSummary.value = UiState.Error(it.message ?: "Failed to load summary") }
            )
        }
    }

    fun deleteSession(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.deleteScanSession(id).fold(
                onSuccess = { onSuccess() },
                onFailure = { _sessions.value = UiState.Error(it.message ?: "Delete failed") }
            )
        }
    }

    // ── Barcode Lookup ────────────────────────────────────────────────────────

    private val _lookupResult = MutableLiveData<UiState<LookupResponse>>()
    val lookupResult: LiveData<UiState<LookupResponse>> = _lookupResult

    // Prevent re-scanning the same barcode while a lookup is in progress
    private var lastScannedBarcode: String? = null

    fun lookupBarcode(barcode: String, barcodeType: String) {
        if (barcode == lastScannedBarcode && _lookupResult.value is UiState.Loading) return
        lastScannedBarcode = barcode
        _lookupResult.value = UiState.Loading
        viewModelScope.launch {
            repo.lookupBarcode(barcode, barcodeType).fold(
                onSuccess = { _lookupResult.value = UiState.Success(it) },
                onFailure = { _lookupResult.value = UiState.Error(it.message ?: "Lookup failed") }
            )
        }
    }

    fun resetLookup() {
        lastScannedBarcode = null
        _lookupResult.value = null
    }

    // ── OCR Lookup ────────────────────────────────────────────────────────────

    private val _ocrResult = MutableLiveData<UiState<OcrResponse>>()
    val ocrResult: LiveData<UiState<OcrResponse>> = _ocrResult

    fun ocrLookup(photoFile: File, barcode: String?) {
        _ocrResult.value = UiState.Loading
        viewModelScope.launch {
            repo.ocrLookup(photoFile, barcode).fold(
                onSuccess = { _ocrResult.value = UiState.Success(it) },
                onFailure = { _ocrResult.value = UiState.Error(it.message ?: "OCR failed") }
            )
        }
    }

    // ── Save Product ──────────────────────────────────────────────────────────

    private val _saveResult = MutableLiveData<UiState<SaveResponse>>()
    val saveResult: LiveData<UiState<SaveResponse>> = _saveResult

    fun saveProduct(product: ProductSaveRequest, sessionId: Int, quantity: Int = 1) {
        _saveResult.value = UiState.Loading
        viewModelScope.launch {
            repo.saveProduct(product, sessionId, quantity).fold(
                onSuccess = { _saveResult.value = UiState.Success(it) },
                onFailure = { _saveResult.value = UiState.Error(it.message ?: "Save failed") }
            )
        }
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    private val _dashboard = MutableLiveData<UiState<DashboardResponse>>()
    val dashboard: LiveData<UiState<DashboardResponse>> = _dashboard

    fun loadDashboard() {
        _dashboard.value = UiState.Loading
        viewModelScope.launch {
            repo.getDashboard().fold(
                onSuccess = { _dashboard.value = UiState.Success(it) },
                onFailure = { _dashboard.value = UiState.Error(it.message ?: "Failed to load dashboard") }
            )
        }
    }
}
