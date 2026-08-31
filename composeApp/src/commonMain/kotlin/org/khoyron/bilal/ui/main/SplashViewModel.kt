package org.khoyron.bilal.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.khoyron.bilal.data.local.SessionManager
import org.khoyron.bilal.domain.usecase.GetAzanTimesUseCase
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException

sealed class SplashError {
    data object NoInternet : SplashError()
    data object ApiError : SplashError()
}

class SplashViewModel(
    private val getAzanTimesUseCase: GetAzanTimesUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _error = MutableStateFlow<SplashError?>(null)
    val error = _error.asStateFlow()

    init {
        fetchAndSaveMethods()
    }

    fun fetchAndSaveMethods() {
        _error.value = null
        viewModelScope.launch {
            try {
                val methods = getAzanTimesUseCase.getMethods()
                val methodsJson = Json.encodeToString(methods)
                sessionManager.saveMethodsList(methodsJson)
                _isReady.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                // Cek apakah error jaringan (timeout atau kegagalan koneksi lainnya)
                val errorMessage = e.message?.lowercase() ?: ""
                if (e is HttpRequestTimeoutException || 
                    e is ConnectTimeoutException || 
                    errorMessage.contains("resolve") || 
                    errorMessage.contains("network") || 
                    errorMessage.contains("connection")
                ) {
                    _error.value = SplashError.NoInternet
                } else {
                    _error.value = SplashError.ApiError
                }
            }
        }
    }

    fun onOkClicked() {
        val currentError = _error.value
        _error.value = null
        
        if (currentError == SplashError.NoInternet) {
            // Coba panggil API lagi
            fetchAndSaveMethods()
        } else {
            // Jika hanya gagal API, tetap lanjut ke Home
            _isReady.value = true
        }
    }
}
