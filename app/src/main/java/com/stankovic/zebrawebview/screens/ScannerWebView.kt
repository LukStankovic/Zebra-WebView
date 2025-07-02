package com.stankovic.zebrawebview.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.stankovic.zebrawebview.MainActivity
import com.stankovic.zebrawebview.viewmodel.ScannerViewModel


@Composable
@SuppressLint("SetJavaScriptEnabled")
fun ScannerWebView(
    scannerViewModel: ScannerViewModel,
    activity: MainActivity,
) {
    val scannedData by scannerViewModel.scannedData.collectAsState()
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                setSupportZoom(false)
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                builtInZoomControls = false
                displayZoomControls = false
                useWideViewPort = false
                loadWithOverviewMode = true
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            }

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            loadUrl(
                "https://zebra.stankovic.cz/",
            )
        }
    }

    DisposableEffect(Unit) {
        activity.setWebView(webView)
        onDispose { }
    }

    LaunchedEffect(scannedData) {
        scannedData?.let { data ->
            webView.evaluateJavascript(
                "if (window.onBarcodeScanned) window.onBarcodeScanned('$data');",
                null
            )
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
    ) { paddingValues ->
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}
