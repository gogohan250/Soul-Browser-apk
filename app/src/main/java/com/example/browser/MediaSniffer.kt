package com.example.browser

import android.webkit.JavascriptInterface

class MediaSniffer(
    private val onVideoDetected: (url: String, title: String) -> Unit,
    private val onImagesExtracted: (List<String>) -> Unit,
    private val onSourceExtracted: (String) -> Unit
) {
    @JavascriptInterface
    fun postVideo(url: String, title: String) {
        if (url.isNotBlank() && !url.startsWith("blob:") && (url.contains("http://") || url.contains("https://"))) {
            onVideoDetected(url, title)
        }
    }

    @JavascriptInterface
    fun postImages(imagesCsv: String) {
        val list = imagesCsv.split(";;;").filter { it.isNotBlank() && it.startsWith("http") }
        onImagesExtracted(list)
    }

    @JavascriptInterface
    fun postSource(html: String) {
        onSourceExtracted(html)
    }

    companion object {
        const val JS_INTERFACE_NAME = "SoulMediaSniffer"

        val SNIFFER_SCRIPT = """
            (function() {
                function checkVideos() {
                    var videos = document.querySelectorAll('video');
                    videos.forEach(function(v) {
                        var src = v.currentSrc || v.src;
                        if (!src) {
                            var source = v.querySelector('source');
                            if (source) src = source.src;
                        }
                        if (src && window.$JS_INTERFACE_NAME) {
                            var title = document.title || 'Video';
                            window.$JS_INTERFACE_NAME.postVideo(src, title);
                        }
                    });
                }
                checkVideos();
                setInterval(checkVideos, 2500);
            })();
        """.trimIndent()

        val EXTRACT_IMAGES_SCRIPT = """
            (function() {
                var imgs = document.querySelectorAll('img');
                var urls = [];
                imgs.forEach(function(img) {
                    var src = img.src || img.getAttribute('data-src');
                    if (src && src.startsWith('http') && urls.indexOf(src) === -1) {
                        urls.push(src);
                    }
                });
                if (window.$JS_INTERFACE_NAME) {
                    window.$JS_INTERFACE_NAME.postImages(urls.join(';;;'));
                }
            })();
        """.trimIndent()

        val EXTRACT_SOURCE_SCRIPT = """
            (function() {
                if (window.$JS_INTERFACE_NAME) {
                    window.$JS_INTERFACE_NAME.postSource(document.documentElement.outerHTML);
                }
            })();
        """.trimIndent()

        fun isVideoUrl(url: String): Boolean {
            val lower = url.lowercase()
            return lower.endsWith(".mp4") ||
                   lower.endsWith(".m3u8") ||
                   lower.endsWith(".webm") ||
                   lower.endsWith(".mkv") ||
                   lower.contains(".mp4?") ||
                   lower.contains(".m3u8?") ||
                   lower.contains("googlevideo.com/videoplayback") ||
                   lower.contains("/video/") ||
                   lower.contains("v.redd.it")
        }
    }
}
