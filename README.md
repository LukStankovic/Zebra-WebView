# ZebraWebView - Zebra Scanner to WebView Integration

A demo Android application that integrates Zebra barcode scanners with WebView using DataWedge,
allowing scanned data to be injected into web pages via JavaScript.

> [!NOTE]
> You can see this repository for native Android app (Kotlin and Jetpack Compose) that uses the same
> DataWedge integration: [ZebraScanner](https://github.com/LukStankovic/Zebra-Scanner)

## Demo

The app loads `https://zebra.stankovic.cz/` and injects scanned barcode data into the web page using
the `window.onBarcodeScanned(data)` JavaScript function.

## Project Structure

```
app/src/main/java/com/stankovic/zebrawebview/
├── MainActivity.kt                   # Main activity orchestrating the app
├── screens/
│   └── ScannerWebView.kt             # Compose WebView component
├── viewmodel/
│   └── ScannerViewModel.kt           # State management for scanned data
├── scanning/
│   └── ScanBroadcastReceiver.kt      # Handles DataWedge broadcast intents
├── config/
│   └── ScanningConfig.kt             # Scanner configuration constants
└── ui/theme/                         # Material3 theme files
```

## Architecture

The app follows MVVM architecture with reactive state management:

1. **Scanner → App**: DataWedge sends broadcast intent → ScanBroadcastReceiver → ScannerViewModel
2. **App → WebView**: StateFlow triggers LaunchedEffect → JavaScript injection → Web page

## Building Your Own Zebra Scanner WebView App

### Step 1: Project Setup

- Create a new Compose project

### Step 2: Add Permissions

Add required permissions to `AndroidManifest.xml`:

```xml

<uses-permission android:name="android.permission.INTERNET" />

<permission android:name="com.stankovic.zebrawebview.SCAN_PERMISSION" android:protectionLevel="signature" />
```

### Step 3: Create Scanner Configuration

```kotlin
// config/ScanningConfig.kt
object ScanningConfig {
    const val APP_SCANNER_INTENT = "com.stankovic.zebrawebview.scan"
    const val SCAN_DATA_KEY = "com.symbol.datawedge.data_string"
}
```

### Step 4: Create ViewModel for State Management

```kotlin
// viewmodel/ScannerViewModel.kt
class ScannerViewModel : ViewModel() {
    private val _scannedData = MutableStateFlow<String?>(null)
    val scannedData: StateFlow<String?> = _scannedData.asStateFlow()

    fun updateScannedData(data: String) {
        _scannedData.value = data
    }

    fun clearScannedData() {
        _scannedData.value = null
    }
}
```

### Step 5: Create Broadcast Receiver

```kotlin
// scanning/ScanBroadcastReceiver.kt
class ScanBroadcastReceiver(
    private val scannerViewModel: ScannerViewModel
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { receivedIntent ->
            if (receivedIntent.action == ScanningConfig.APP_SCANNER_INTENT) {
                val scannedData = receivedIntent.getStringExtra(ScanningConfig.SCAN_DATA_KEY)
                scannedData?.let { data ->
                    scannerViewModel.updateScannedData(data)
                }
            }
        }
    }
}
```

### Step 6: Create WebView Composable

```kotlin
// screens/ScannerWebView.kt
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
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setSupportZoom(false)
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

            loadUrl("https://your-website.com/")
        }
    }

    // Inject scanned data into WebView
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
```

### Step 7: Set Up MainActivity

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    private lateinit var scannerReceiver: ScanBroadcastReceiver
    private lateinit var scannerViewModel: ScannerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerViewModel = ViewModelProvider(this)[ScannerViewModel::class.java]
        scannerReceiver = ScanBroadcastReceiver(scannerViewModel)

        enableEdgeToEdge()
        setContent {
            YourAppTheme {
                ScannerWebView(
                    scannerViewModel = scannerViewModel,
                    activity = this@MainActivity,
                )
            }
        }
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
```

### Step 8: Register Broadcast Receiver

Add the receiver to `AndroidManifest.xml`:

```xml

<receiver android:name=".scanning.ScanBroadcastReceiver" android:exported="true"
    android:permission="com.stankovic.zebrawebview.SCAN_PERMISSION">
    <intent-filter>
        <action android:name="com.stankovic.zebrawebview.scan" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</receiver>
```

### Step 9: Configure DataWedge

> [!IMPORTANT]
> You have to create a profile **after** installing this app on the Zebra device!

1. Open DataWedge app on the device.
2. Click on three dots in top right corner
3. Click on `New Profile`
4. Enter any name of your profile
5. Click on the profile you just created
6. Ensure that the profile is enabled (should be by default)
7. Associate that profile with the sample application
    - Click on `Associated apps`
    - Click on three dots in top right corner and click on `New app/activity`
    - Scroll in the menu, find `com.stankovic.zebrawebview` and click on it.
    - In the `Select activity` menu choose `*`
    - Go back to the profile screen
8. Ensure that `Barcode input` is enabled
9. Ensure that `Keystroke output` is enabled
10. Scroll down to `Intent output` section
11. Click on checkbox to enable it
12. Click on `Intent action` and enter `com.stankovic.zebrawebview.scan` action and click OK.
- _Intent action must be same as set in `ScanningConfig.kt` in `APP_SCANNER_INTENT` constant._
13. Click on `Intent delivery` and choose `Broadcast intent`

### Step 10: Prepare Your Web Page

Your web page must implement the JavaScript callback:

```javascript
// In your web page
window.onBarcodeScanned = function(data) {
    document.getElementById('barcode-input').value = data;
};
```

### Debug Tips

- Use `adb logcat` to view system logs
- Add logging to broadcast receiver
- Test with manual intent broadcasts using ADB:

```bash
adb shell am broadcast -a com.stankovic.zebrawebview.scan --es com.symbol.datawedge.data_string "test123"
```

## License

This project is provided as a demo for educational purposes.

## Contributing

Feel free to submit issues and pull requests to improve this demo.