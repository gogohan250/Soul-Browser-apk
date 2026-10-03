package com.example.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.BrowserConfig
import com.example.data.TabInfo

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: TabInfo,
    config: BrowserConfig,
    onProgressChange: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onFaviconChange: (String?) -> Unit,
    onCanGoBackChange: (Boolean) -> Unit,
    onCanGoForwardChange: (Boolean) -> Unit,
    onVideoDetected: (url: String, title: String) -> Unit,
    onImagesExtracted: (List<String>) -> Unit,
    onSourceExtracted: (String) -> Unit,
    onCapturePreview: (Bitmap) -> Unit,
    onDownloadRequested: (url: String, userAgent: String, contentDisposition: String, mimeType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // React to Desktop Site toggle
    LaunchedEffect(config.desktopSite, webViewInstance) {
        webViewInstance?.let { wv ->
            wv.settings.userAgentString = if (config.desktopSite) {
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Soul/1.0"
            } else {
                "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 Soul/1.0"
            }
        }
    }

    // React to Text Zoom
    LaunchedEffect(config.textZoomPercent, webViewInstance) {
        webViewInstance?.settings?.textZoom = config.textZoomPercent
    }

    // React to Block Images
    LaunchedEffect(config.blockImages, webViewInstance) {
        webViewInstance?.settings?.loadsImagesAutomatically = !config.blockImages
        webViewInstance?.settings?.blockNetworkImage = config.blockImages
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        textZoom = config.textZoomPercent
                        loadsImagesAutomatically = !config.blockImages
                        blockNetworkImage = config.blockImages
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    // Add Media Sniffer JS bridge
                    addJavascriptInterface(
                        MediaSniffer(
                            onVideoDetected = onVideoDetected,
                            onImagesExtracted = onImagesExtracted,
                            onSourceExtracted = onSourceExtracted
                        ),
                        MediaSniffer.JS_INTERFACE_NAME
                    )

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            onProgressChange(newProgress)

                            if (newProgress > 60) {
                                // Inject media sniffer script
                                view?.evaluateJavascript(MediaSniffer.SNIFFER_SCRIPT, null)
                                if (config.adBlockEnabled) {
                                    view?.evaluateJavascript(AdBlocker.AD_BLOCK_CSS_JS, null)
                                }
                            }
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            title?.let { onTitleChange(it) }
                        }

                        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                            super.onReceivedIcon(view, icon)
                            // Favicon
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString() ?: return null

                            // Ad block check
                            if (config.adBlockEnabled && AdBlocker.isAd(reqUrl)) {
                                return WebResourceResponse("text/plain", "UTF-8", null)
                            }

                            // Media sniffer check
                            if (MediaSniffer.isVideoUrl(reqUrl)) {
                                onVideoDetected(reqUrl, view?.title ?: "Video")
                            }

                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            url?.let { onUrlChange(it) }
                            onCanGoBackChange(view?.canGoBack() ?: false)
                            onCanGoForwardChange(view?.canGoForward() ?: false)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            url?.let { onUrlChange(it) }
                            onCanGoBackChange(view?.canGoBack() ?: false)
                            onCanGoForwardChange(view?.canGoForward() ?: false)

                            // Inject media sniffer
                            view?.evaluateJavascript(MediaSniffer.SNIFFER_SCRIPT, null)

                            // Apply Ad block CSS
                            if (config.adBlockEnabled) {
                                view?.evaluateJavascript(AdBlocker.AD_BLOCK_CSS_JS, null)
                            }

                            // Web page dark mode injection
                            if (config.isWebPageDarkMode || config.isDarkMode) {
                                val darkCss = """
                                    (function() {
                                        var css = 'html { filter: invert(90%) hue-rotate(180deg) !important; background-color: #121212 !important; } img, video, canvas, svg { filter: invert(100%) hue-rotate(180deg) opacity(${config.darkImageBrightnessPercent / 100f}) !important; }';
                                        var s = document.createElement('style');
                                        s.type = 'text/css';
                                        s.appendChild(document.createTextNode(css));
                                        document.head.appendChild(s);
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(darkCss, null)
                            }

                            // Capture preview snapshot for tab switcher
                            try {
                                if (view != null && view.width > 0 && view.height > 0) {
                                    val bitmap = Bitmap.createBitmap(
                                        view.width / 2,
                                        view.height / 2,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = android.graphics.Canvas(bitmap)
                                    canvas.scale(0.5f, 0.5f)
                                    view.draw(canvas)
                                    onCapturePreview(bitmap)
                                }
                            } catch (_: Exception) {}
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler?.proceed()
                        }
                    }

                    setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                        onDownloadRequested(url, userAgent, contentDisposition, mimetype)
                    }

                    if (tab.isIncognito) {
                        clearCache(true)
                        clearHistory()
                    }

                    loadUrl(tab.url)
                    webViewInstance = this
                }
            },
            update = { wv ->
                if (wv.url != tab.url && tab.url.isNotBlank()) {
                    wv.loadUrl(tab.url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Screen filter overlay (warm amber tint / dark overlay)
        if (config.screenFilterEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(config.screenFilterColorHex))
            )
        }
    }
}
