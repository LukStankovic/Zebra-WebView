package com.stankovic.zebrawebview.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScannerViewModel : ViewModel() {
    private val _scannedData = MutableStateFlow<String?>(null)
    val scannedData = _scannedData.asStateFlow()

    fun updateScannedData(data: String) {
        _scannedData.value = data
    }

    fun clearScannedData() {
        _scannedData.value = null
    }
}
