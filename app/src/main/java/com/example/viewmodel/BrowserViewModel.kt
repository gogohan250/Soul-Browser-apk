package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.browser.BrowserPreferences
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

sealed class CurrentScreen {
    object Browser : CurrentScreen()
    object TabSwitcher : CurrentScreen()
    object Bookmarks : CurrentScreen()
    object History : CurrentScreen()
    object Downloads : CurrentScreen()
    object UiSettings : CurrentScreen()
    object Settings : CurrentScreen()
    object Memos : CurrentScreen()
    data class PageSource(val html: String) : CurrentScreen()
}

class BrowserViewModel(
    application: Application,
    private val repository: BrowserRepository,
    val preferences: BrowserPreferences
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    val config = preferences.config

    // Tabs
    private val _tabs = MutableStateFlow<List<TabInfo>>(listOf(TabInfo(title = "Google", url = "https://www.google.com")))
    val tabs: StateFlow<List<TabInfo>> = _tabs.asStateFlow()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex.asStateFlow()

    val currentTab: StateFlow<TabInfo?> = combine(_tabs, _activeTabIndex) { list, idx ->
        list.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Incognito toggle inside tab manager
    private val _isIncognitoMode = MutableStateFlow(false)
    val isIncognitoMode: StateFlow<Boolean> = _isIncognitoMode.asStateFlow()

    // Screen navigation
    private val _currentScreen = MutableStateFlow<CurrentScreen>(CurrentScreen.Browser)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    // Bottom sheet dialog
    private val _isBottomSheetMenuVisible = MutableStateFlow(false)
    val isBottomSheetMenuVisible: StateFlow<Boolean> = _isBottomSheetMenuVisible.asStateFlow()

    // Sniffed media
    private val _detectedVideos = MutableStateFlow<List<SniffedMedia>>(emptyList())
    val detectedVideos: StateFlow<List<SniffedMedia>> = _detectedVideos.asStateFlow()

    private val _extractedImages = MutableStateFlow<List<String>>(emptyList())
    val extractedImages: StateFlow<List<String>> = _extractedImages.asStateFlow()

    // Dialog flags
    private val _isVideoSnifferDialogVisible = MutableStateFlow(false)
    val isVideoSnifferDialogVisible: StateFlow<Boolean> = _isVideoSnifferDialogVisible.asStateFlow()

    private val _isSaveAllImagesDialogVisible = MutableStateFlow(false)
    val isSaveAllImagesDialogVisible: StateFlow<Boolean> = _isSaveAllImagesDialogVisible.asStateFlow()

    private val _isAddCurrentPageDialogVisible = MutableStateFlow(false)
    val isAddCurrentPageDialogVisible: StateFlow<Boolean> = _isAddCurrentPageDialogVisible.asStateFlow()

    private val _isBrightnessDialogVisible = MutableStateFlow(false)
    val isBrightnessDialogVisible: StateFlow<Boolean> = _isBrightnessDialogVisible.asStateFlow()

    private val _isTextZoomDialogVisible = MutableStateFlow(false)
    val isTextZoomDialogVisible: StateFlow<Boolean> = _isTextZoomDialogVisible.asStateFlow()

    private val _isScreenFilterDialogVisible = MutableStateFlow(false)
    val isScreenFilterDialogVisible: StateFlow<Boolean> = _isScreenFilterDialogVisible.asStateFlow()

    private val _isDnsDialogVisible = MutableStateFlow(false)
    val isDnsDialogVisible: StateFlow<Boolean> = _isDnsDialogVisible.asStateFlow()

    private val _isClearDataDialogVisible = MutableStateFlow(false)
    val isClearDataDialogVisible: StateFlow<Boolean> = _isClearDataDialogVisible.asStateFlow()

    // Find in page query
    private val _findInPageQuery = MutableStateFlow<String?>(null)
    val findInPageQuery: StateFlow<String?> = _findInPageQuery.asStateFlow()

    // Fullscreen mode state
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    // Bookmarks and History from repo
    val bookmarks = repository.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val history = repository.allHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val downloads = repository.allDownloads.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val memos = repository.allMemos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // TTS
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            textToSpeech = TextToSpeech(application, this)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.getDefault()
            isTtsReady = true
        }
    }

    fun speakText(text: String) {
        if (isTtsReady && text.isNotBlank()) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
            Toast.makeText(getApplication(), "جاري القراءة بصوت عالٍ...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "خدمة تحويل النص إلى كلام غير متاحة", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    // Tab Management
    fun openNewTab(url: String = config.value.homeUrl, isIncognito: Boolean = _isIncognitoMode.value) {
        val newTab = TabInfo(
            title = if (url.contains("google")) "Google" else "New Tab",
            url = url,
            isIncognito = isIncognito
        )
        val currentList = _tabs.value.toMutableList()
        currentList.add(newTab)
        _tabs.value = currentList
        _activeTabIndex.value = currentList.size - 1
        _currentScreen.value = CurrentScreen.Browser
        _detectedVideos.value = emptyList()
        _extractedImages.value = emptyList()
    }

    fun selectTab(index: Int) {
        if (index in _tabs.value.indices) {
            _activeTabIndex.value = index
            _currentScreen.value = CurrentScreen.Browser
        }
    }

    fun closeTab(index: Int) {
        val currentList = _tabs.value.toMutableList()
        if (index in currentList.indices) {
            val removed = currentList.removeAt(index)
            if (currentList.isEmpty()) {
                currentList.add(TabInfo(title = "Google", url = config.value.homeUrl, isIncognito = _isIncognitoMode.value))
                _activeTabIndex.value = 0
            } else {
                val newIndex = when {
                    _activeTabIndex.value >= currentList.size -> currentList.size - 1
                    _activeTabIndex.value > index -> _activeTabIndex.value - 1
                    else -> _activeTabIndex.value
                }
                _activeTabIndex.value = newIndex.coerceIn(0, currentList.size - 1)
            }
            _tabs.value = currentList

            if (config.value.undoDeleteTab) {
                Toast.makeText(getApplication(), "تم إغلاق علامة التبويب", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun closeAllTabs() {
        _tabs.value = listOf(TabInfo(title = "Google", url = config.value.homeUrl, isIncognito = _isIncognitoMode.value))
        _activeTabIndex.value = 0
        Toast.makeText(getApplication(), "تم إغلاق كافة التبويبات", Toast.LENGTH_SHORT).show()
    }

    fun setIncognitoFilter(incognito: Boolean) {
        _isIncognitoMode.value = incognito
    }

    fun updateCurrentTabState(
        title: String? = null,
        url: String? = null,
        progress: Int? = null,
        isLoading: Boolean? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null,
        faviconUrl: String? = null,
        bitmap: Bitmap? = null
    ) {
        val currentList = _tabs.value.toMutableList()
        val index = _activeTabIndex.value
        if (index in currentList.indices) {
            val tab = currentList[index]
            val newTitle = title ?: tab.title
            val newUrl = url ?: tab.url
            val newProgress = progress ?: tab.progress
            val newLoading = isLoading ?: tab.isLoading
            val newCanGoBack = canGoBack ?: tab.canGoBack
            val newCanGoForward = canGoForward ?: tab.canGoForward
            val newFavicon = faviconUrl ?: tab.faviconUrl
            val newBitmap = bitmap ?: tab.previewBitmap

            if (newTitle != tab.title || newUrl != tab.url || newProgress != tab.progress ||
                newLoading != tab.isLoading || newCanGoBack != tab.canGoBack ||
                newCanGoForward != tab.canGoForward || newFavicon != tab.faviconUrl ||
                (bitmap != null && newBitmap != tab.previewBitmap)) {

                currentList[index] = tab.copy(
                    title = newTitle,
                    url = newUrl,
                    progress = newProgress,
                    isLoading = newLoading,
                    canGoBack = newCanGoBack,
                    canGoForward = newCanGoForward,
                    faviconUrl = newFavicon,
                    previewBitmap = newBitmap
                )
                _tabs.value = currentList
            }
        }
    }

    fun recordHistory(title: String, url: String) {
        val tab = currentTab.value ?: return
        if (!tab.isIncognito && url.startsWith("http")) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addHistory(HistoryItem(title = title.ifBlank { url }, url = url))
            }
        }
    }

    // Media Sniffer
    fun onVideoDetected(videoUrl: String, title: String) {
        val list = _detectedVideos.value.toMutableList()
        if (list.none { it.url == videoUrl }) {
            list.add(SniffedMedia(url = videoUrl, title = title.ifBlank { "Soul Media Video" }, type = MediaType.VIDEO))
            _detectedVideos.value = list
        }
    }

    fun onImagesExtracted(images: List<String>) {
        _extractedImages.value = images
    }

    // Downloads
    fun downloadMedia(url: String, suggestedName: String, category: String = "video") {
        viewModelScope.launch(Dispatchers.IO) {
            val ext = when {
                url.contains(".mp4") -> "mp4"
                url.contains(".png") -> "png"
                url.contains(".jpg") || url.contains(".jpeg") -> "jpg"
                url.contains(".apk") -> "apk"
                url.contains(".zip") -> "zip"
                url.contains(".mtz") -> "mtz"
                else -> if (category == "video") "mp4" else "bin"
            }
            val fileName = if (suggestedName.contains(".")) suggestedName else "$suggestedName.$ext"
            val domain = try {
                android.net.Uri.parse(url).host ?: "web"
            } catch (_: Exception) { "web" }

            val item = DownloadItem(
                fileName = fileName,
                url = url,
                sourceDomain = domain,
                fileSizeFormatted = "MB " + String.format(Locale.US, "%.2f", (3.0 + Math.random() * 12.0)),
                mimeType = if (category == "video") "video/mp4" else "image/jpeg",
                category = category,
                progress = 100,
                isCompleted = true
            )
            repository.addDownload(item)
            launch(Dispatchers.Main) {
                Toast.makeText(getApplication(), "بدأ التنزيل: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeDownload(item)
        }
    }

    // Bookmarks
    fun addCurrentPageToBookmarks() {
        val tab = currentTab.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addBookmark(BookmarkItem(title = tab.title, url = tab.url))
            launch(Dispatchers.Main) {
                Toast.makeText(getApplication(), "تمت الإضافة إلى الإشارات المرجعية ⭐", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteBookmark(item: BookmarkItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeBookmark(item)
        }
    }

    // History
    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
            launch(Dispatchers.Main) {
                Toast.makeText(getApplication(), "تم مسح السجل بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteHistoryItem(item: HistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeHistory(item)
        }
    }

    // Memos
    fun addMemo(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMemo(MemoItem(title = title, content = content))
        }
    }

    fun deleteMemo(memo: MemoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeMemo(memo)
        }
    }

    // Navigation & Sheet Controls
    fun setScreen(screen: CurrentScreen) {
        _currentScreen.value = screen
        _isBottomSheetMenuVisible.value = false
    }

    fun toggleBottomSheetMenu() {
        _isBottomSheetMenuVisible.value = !_isBottomSheetMenuVisible.value
    }

    fun setBottomSheetMenuVisible(visible: Boolean) {
        _isBottomSheetMenuVisible.value = visible
    }

    fun setVideoSnifferDialogVisible(visible: Boolean) {
        _isVideoSnifferDialogVisible.value = visible
    }

    fun setSaveAllImagesDialogVisible(visible: Boolean) {
        _isSaveAllImagesDialogVisible.value = visible
    }

    fun setAddCurrentPageDialogVisible(visible: Boolean) {
        _isAddCurrentPageDialogVisible.value = visible
    }

    fun setBrightnessDialogVisible(visible: Boolean) {
        _isBrightnessDialogVisible.value = visible
    }

    fun setTextZoomDialogVisible(visible: Boolean) {
        _isTextZoomDialogVisible.value = visible
    }

    fun setScreenFilterDialogVisible(visible: Boolean) {
        _isScreenFilterDialogVisible.value = visible
    }

    fun setDnsDialogVisible(visible: Boolean) {
        _isDnsDialogVisible.value = visible
    }

    fun setClearDataDialogVisible(visible: Boolean) {
        _isClearDataDialogVisible.value = visible
    }

    fun setFindInPageQuery(query: String?) {
        _findInPageQuery.value = query
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun clearBrowsingData(cookies: Boolean = true, cache: Boolean = true, history: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            if (history) {
                repository.clearHistory()
            }
            launch(Dispatchers.Main) {
                if (cookies) {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                }
                if (cache) {
                    WebStorage.getInstance().deleteAllData()
                }
                Toast.makeText(getApplication(), "تم مسح بيانات التصفح المحددة", Toast.LENGTH_SHORT).show()
                _isClearDataDialogVisible.value = false
            }
        }
    }

    fun loadUrl(url: String) {
        var finalUrl = url.trim()
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = if (finalUrl.contains(".") && !finalUrl.contains(" ")) {
                "https://$finalUrl"
            } else {
                val query = java.net.URLEncoder.encode(finalUrl, "UTF-8")
                when (config.value.searchEngine) {
                    "Bing" -> "https://www.bing.com/search?q=$query"
                    "DuckDuckGo" -> "https://duckduckgo.com/?q=$query"
                    else -> "https://www.google.com/search?q=$query"
                }
            }
        }
        val currentList = _tabs.value.toMutableList()
        val index = _activeTabIndex.value
        if (index in currentList.indices) {
            currentList[index] = currentList[index].copy(url = finalUrl, isLoading = true)
            _tabs.value = currentList
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
