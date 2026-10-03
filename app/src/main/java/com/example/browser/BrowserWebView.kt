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
    onPageFinished: (url: String, title: String) -> Unit,
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
    var currentLoadedUrl by remember(tab.id) { mutableStateOf(tab.url) }

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
                            url?.let {
                                currentLoadedUrl = it
                                onUrlChange(it)
                            }
                            onCanGoBackChange(view?.canGoBack() ?: false)
                            onCanGoForwardChange(view?.canGoForward() ?: false)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            val finalUrl = url ?: tab.url
                            currentLoadedUrl = finalUrl
                            onUrlChange(finalUrl)
                            onTitleChange(view?.title ?: tab.title)
                            onPageFinished(finalUrl, view?.title ?: tab.title)
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

                            // Capture preview snapshot for tab switcher safely via post
                            view?.post {
                                try {
                                    if (view.width > 0 && view.height > 0) {
                                        val w = (view.width / 2).coerceAtLeast(1)
                                        val h = (view.height / 2).coerceAtLeast(1)
                                        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                                        val canvas = android.graphics.Canvas(bitmap)
                                        canvas.scale(0.5f, 0.5f)
                                        view.draw(canvas)
                                        onCapturePreview(bitmap)
                                    }
                                } catch (_: Throwable) {}
                            }
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler?.proceed()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                val failingUrl = request.url.toString()
                                val errorHtml = """
                                    <!DOCTYPE html>
                                    <html dir="rtl" lang="ar">
                                    <head>
                                        <meta charset="utf-8">
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                        <style>
                                            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; text-align: center; padding: 48px 20px; background: #F8F9FA; color: #202124; }
                                            .card { background: white; border-radius: 16px; padding: 32px 24px; box-shadow: 0 4px 16px rgba(0,0,0,0.06); max-width: 380px; margin: 0 auto; }
                                            .icon { font-size: 40px; margin-bottom: 12px; }
                                            h2 { color: #5B67CA; margin: 0 0 12px; font-size: 20px; }
                                            p { color: #5F6368; font-size: 14px; line-height: 1.5; margin: 0 0 20px; }
                                            .url-text { word-break: break-all; color: #1A73E8; font-size: 13px; font-weight: bold; }
                                            .btn { background: #5B67CA; color: white; border: none; padding: 12px 28px; border-radius: 24px; font-size: 14px; font-weight: bold; cursor: pointer; }
                                        </style>
                                    </head>
                                    <body>
                                        <div class="card">
                                            <div class="icon">🌐</div>
                                            <h2>متصفح Soul Browser</h2>
                                            <p>تعذر الاتصال بالموقع:<br><span class="url-text">$failingUrl</span></p>
                                            <p>يرجى التأكد من اتصال الإنترنت أو كتابة عنوان آخر في شريط البحث.</p>
                                            <button class="btn" onclick="location.reload()">إعادة المحاولة</button>
                                        </div>
                                    </body>
                                    </html>
                                """.trimIndent()
                                view?.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null)
                            }
                        }
                    }

                    setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                        onDownloadRequested(url, userAgent, contentDisposition, mimetype)
                    }

                    if (tab.isIncognito) {
                        clearCache(true)
                        clearHistory()
                    }

                    currentLoadedUrl = tab.url
                    loadUrl(tab.url)
                    webViewInstance = this
                }
            },
            update = { wv ->
                val cleanWvUrl = wv.url?.trimEnd('/') ?: ""
                val cleanTarget = tab.url.trimEnd('/')
                val cleanLoaded = currentLoadedUrl.trimEnd('/')

                // Only reload when tab.url has truly changed to a new address
                if (cleanTarget.isNotBlank() && cleanTarget != cleanLoaded && cleanTarget != cleanWvUrl) {
                    currentLoadedUrl = tab.url
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
