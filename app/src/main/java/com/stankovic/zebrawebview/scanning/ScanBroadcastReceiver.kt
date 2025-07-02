package com.stankovic.zebrawebview.scanning

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stankovic.zebrawebview.viewmodel.ScannerViewModel
import com.stankovic.zebrawebview.config.ScanningConfig

class ScanBroadcastReceiver : BroadcastReceiver {
    private var scannerViewModel: ScannerViewModel? = null

    constructor()

    constructor(scannerViewModel: ScannerViewModel) {
        this.scannerViewModel = scannerViewModel
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action != null && action == ScanningConfig.APP_SCANNER_INTENT) {
            val decodedData = intent.getStringExtra(ScanningConfig.ZEBRA_SCANNER_INTENT)
            scannerViewModel?.updateScannedData(decodedData ?: "")
        }
    }
}
