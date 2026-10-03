package com.example.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val favicon: String? = null,
    val folder: String = "Root",
    val isFolder: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val url: String,
    val sourceDomain: String = "",
    val filePath: String = "",
    val fileSizeFormatted: String = "0 MB",
    val fileSizeBytes: Long = 0L,
    val mimeType: String = "",
    val category: String = "all", // all, video, image, document
    val progress: Int = 100,
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "memos")
data class MemoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TabInfo(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "Google",
    var url: String = "https://www.google.com",
    var isIncognito: Boolean = false,
    var faviconUrl: String? = null,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var progress: Int = 0,
    var isLoading: Boolean = false,
    var previewBitmap: Bitmap? = null
)

data class SniffedMedia(
    val url: String,
    val title: String = "Video",
    val type: MediaType = MediaType.VIDEO,
    val sizeEstimate: String = "HD",
    val thumbnail: String? = null
)

enum class MediaType {
    VIDEO,
    IMAGE,
    AUDIO
}

data class BrowserConfig(
    val showStatusBar: Boolean = true,
    val showNavigationBar: Boolean = true,
    val scrollableTopBar: Boolean = false,
    val scrollableBottomBar: Boolean = false,
    val isDarkMode: Boolean = false,
    val isWebPageDarkMode: Boolean = false,
    val darkImageDimming: Boolean = true,
    val darkImageBrightnessPercent: Int = 70,
    val adBlockEnabled: Boolean = true,
    val blockImages: Boolean = false,
    val desktopSite: Boolean = false,
    val screenFilterEnabled: Boolean = false,
    val screenFilterColorHex: Long = 0x44FFB74D, // warm amber tint
    val textZoomPercent: Int = 100,
    val dnsEnabled: Boolean = false,
    val dnsProvider: String = "Google Public DNS",
    val searchEngine: String = "Google",
    val homeUrl: String = "https://www.google.com",
    val oneHandedMode: Boolean = false,
    val undoDeleteTab: Boolean = true,
    val swipeToDismissTab: Boolean = true
)
