package com.example.browser

import android.net.Uri

object AdBlocker {
    private val AD_DOMAINS = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleads.g.doubleclick.net",
        "adservice.google.com",
        "adnxs.com",
        "popads.net",
        "adcolony.com",
        "unityads.unity3d.com",
        "applovin.com",
        "vungle.com",
        "taboola.com",
        "outbrain.com",
        "bidswitch.net",
        "criteo.com",
        "rubiconproject.com",
        "pubmatic.com"
    )

    fun isAd(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: return false
            AD_DOMAINS.any { host.contains(it) }
        } catch (_: Exception) {
            false
        }
    }

    const val AD_BLOCK_CSS_JS = """
        (function() {
            var css = '.ad, .ads, .ad-banner, .adsbygoogle, [id^="google_ads_"], [class*="advertisement"], [class*="banner-ad"] { display: none !important; height: 0 !important; visibility: hidden !important; }';
            var head = document.head || document.getElementsByTagName('head')[0];
            var style = document.createElement('style');
            style.type = 'text/css';
            style.appendChild(document.createTextNode(css));
            if (head) { head.appendChild(style); }
        })();
    """
}
