package com.example.browser

import android.content.Context
import android.content.SharedPreferences
import com.example.data.BrowserConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrowserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("soul_browser_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<BrowserConfig> = _config.asStateFlow()

    private fun loadConfig(): BrowserConfig {
        return BrowserConfig(
            showStatusBar = prefs.getBoolean("show_status_bar", true),
            showNavigationBar = prefs.getBoolean("show_nav_bar", true),
            scrollableTopBar = prefs.getBoolean("scrollable_top_bar", false),
            scrollableBottomBar = prefs.getBoolean("scrollable_bottom_bar", false),
            isDarkMode = prefs.getBoolean("is_dark_mode", false),
            isWebPageDarkMode = prefs.getBoolean("is_webpage_dark_mode", false),
            darkImageDimming = prefs.getBoolean("dark_image_dimming", true),
            darkImageBrightnessPercent = prefs.getInt("dark_image_brightness", 70),
            adBlockEnabled = prefs.getBoolean("ad_block_enabled", true),
            blockImages = prefs.getBoolean("block_images", false),
            desktopSite = prefs.getBoolean("desktop_site", false),
            screenFilterEnabled = prefs.getBoolean("screen_filter_enabled", false),
            screenFilterColorHex = prefs.getLong("screen_filter_color", 0x44FFB74D),
            textZoomPercent = prefs.getInt("text_zoom_percent", 100),
            dnsEnabled = prefs.getBoolean("dns_enabled", false),
            dnsProvider = prefs.getString("dns_provider", "Google Public DNS") ?: "Google Public DNS",
            searchEngine = prefs.getString("search_engine", "Google") ?: "Google",
            homeUrl = prefs.getString("home_url", "https://www.google.com") ?: "https://www.google.com",
            oneHandedMode = prefs.getBoolean("one_handed_mode", false),
            undoDeleteTab = prefs.getBoolean("undo_delete_tab", true),
            swipeToDismissTab = prefs.getBoolean("swipe_dismiss_tab", true)
        )
    }

    fun updateConfig(block: (BrowserConfig) -> BrowserConfig) {
        val newConfig = block(_config.value)
        _config.value = newConfig
        prefs.edit().apply {
            putBoolean("show_status_bar", newConfig.showStatusBar)
            putBoolean("show_nav_bar", newConfig.showNavigationBar)
            putBoolean("scrollable_top_bar", newConfig.scrollableTopBar)
            putBoolean("scrollable_bottom_bar", newConfig.scrollableBottomBar)
            putBoolean("is_dark_mode", newConfig.isDarkMode)
            putBoolean("is_webpage_dark_mode", newConfig.isWebPageDarkMode)
            putBoolean("dark_image_dimming", newConfig.darkImageDimming)
            putInt("dark_image_brightness", newConfig.darkImageBrightnessPercent)
            putBoolean("ad_block_enabled", newConfig.adBlockEnabled)
            putBoolean("block_images", newConfig.blockImages)
            putBoolean("desktop_site", newConfig.desktopSite)
            putBoolean("screen_filter_enabled", newConfig.screenFilterEnabled)
            putLong("screen_filter_color", newConfig.screenFilterColorHex)
            putInt("text_zoom_percent", newConfig.textZoomPercent)
            putBoolean("dns_enabled", newConfig.dnsEnabled)
            putString("dns_provider", newConfig.dnsProvider)
            putString("search_engine", newConfig.searchEngine)
            putString("home_url", newConfig.homeUrl)
            putBoolean("one_handed_mode", newConfig.oneHandedMode)
            putBoolean("undo_delete_tab", newConfig.undoDeleteTab)
            putBoolean("swipe_dismiss_tab", newConfig.swipeToDismissTab)
            apply()
        }
    }
}
