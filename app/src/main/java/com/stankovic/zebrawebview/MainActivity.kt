package com.stankovic.zebrawebview

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.stankovic.zebrawebview.config.ScanningConfig
import com.stankovic.zebrawebview.scanning.ScanBroadcastReceiver
import com.stankovic.zebrawebview.screens.ScannerWebView
import com.stankovic.zebrawebview.ui.theme.ZebraWebViewTheme
import com.stankovic.zebrawebview.viewmodel.ScannerViewModel

class MainActivity : ComponentActivity() {
    private lateinit var scannerReceiver: ScanBroadcastReceiver
    private lateinit var scannerViewModel: ScannerViewModel
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerViewModel = ViewModelProvider(this)[ScannerViewModel::class.java]
        scannerReceiver = ScanBroadcastReceiver(scannerViewModel)

        enableEdgeToEdge()
        setContent {
            ZebraWebViewTheme {
                ScannerWebView(
                    scannerViewModel = scannerViewModel,
                    activity = this@MainActivity,
                )
            }
        }
    }

    fun setWebView(webViewInstance: WebView) {
        webView = webViewInstance
    }

    override fun onResume() {
        super.onResume()
        registerScannerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(scannerReceiver)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerScannerReceiver() {
        val filter = IntentFilter().apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addAction(ScanningConfig.APP_SCANNER_INTENT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scannerReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(scannerReceiver, filter)
        }
    }
}
