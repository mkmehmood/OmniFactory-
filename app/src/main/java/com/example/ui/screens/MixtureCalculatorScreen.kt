package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color as AndroidColor
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.preferences.MixtureCalculatorStore
import com.example.ui.MixtureViewModel

/**
 * Mixture Calculator screen.
 *
 * The calculator itself is a self-contained page bundled in assets/mixture_calculator.html
 * (works fully offline). It talks to the app through the "AndroidStore" JavaScript bridge:
 *  - loadState / saveState  -> MixtureCalculatorStore (private file, included in app backups)
 *  - copyText               -> system clipboard
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MixtureCalculatorScreen(
    viewModel: MixtureViewModel,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(AndroidColor.parseColor("#020617"))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient = WebViewClient()
                addJavascriptInterface(MixtureWebBridge(ctx.applicationContext), "AndroidStore")
                loadUrl("file:///android_asset/mixture_calculator.html")
            }
        },
        onRelease = { webView -> webView.destroy() }
    )
}

/** Methods exposed to the calculator page. Called from a WebView background thread. */
class MixtureWebBridge(private val appContext: Context) {

    @JavascriptInterface
    fun loadState(): String = MixtureCalculatorStore.read(appContext)

    @JavascriptInterface
    fun saveState(json: String): Boolean = MixtureCalculatorStore.write(appContext, json)

    @JavascriptInterface
    fun copyText(text: String): Boolean {
        return try {
            val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Mixture Calculator", text))
            true
        } catch (e: Exception) {
            false
        }
    }
}
