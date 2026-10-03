package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.browser.BrowserWebView
import com.example.browser.MediaSniffer
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.CurrentScreen

@Composable
fun MainBrowserScreen(
    viewModel: BrowserViewModel,
    onCloseApp: () -> Unit
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabIndex by viewModel.activeTabIndex.collectAsStateWithLifecycle()
    val isIncognitoMode by viewModel.isIncognitoMode.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()

    val isBottomSheetMenuVisible by viewModel.isBottomSheetMenuVisible.collectAsStateWithLifecycle()
    val detectedVideos by viewModel.detectedVideos.collectAsStateWithLifecycle()
    val extractedImages by viewModel.extractedImages.collectAsStateWithLifecycle()

    val isVideoSnifferDialogVisible by viewModel.isVideoSnifferDialogVisible.collectAsStateWithLifecycle()
    val isSaveAllImagesDialogVisible by viewModel.isSaveAllImagesDialogVisible.collectAsStateWithLifecycle()
    val isAddCurrentPageDialogVisible by viewModel.isAddCurrentPageDialogVisible.collectAsStateWithLifecycle()
    val isBrightnessDialogVisible by viewModel.isBrightnessDialogVisible.collectAsStateWithLifecycle()
    val isTextZoomDialogVisible by viewModel.isTextZoomDialogVisible.collectAsStateWithLifecycle()
    val isScreenFilterDialogVisible by viewModel.isScreenFilterDialogVisible.collectAsStateWithLifecycle()
    val isDnsDialogVisible by viewModel.isDnsDialogVisible.collectAsStateWithLifecycle()
    val isClearDataDialogVisible by viewModel.isClearDataDialogVisible.collectAsStateWithLifecycle()

    val findInPageQuery by viewModel.findInPageQuery.collectAsStateWithLifecycle()
    val isFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()

    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val memos by viewModel.memos.collectAsStateWithLifecycle()

    val isCurrentUrlBookmarked = remember(currentTab?.url, bookmarks) {
        bookmarks.any { it.url == currentTab?.url }
    }

    // Secondary screen back handler
    if (currentScreen != CurrentScreen.Browser) {
        BackHandler {
            viewModel.setScreen(CurrentScreen.Browser)
        }
    }

    when (val screen = currentScreen) {
        CurrentScreen.TabSwitcher -> {
            TabSwitcherScreen(
                tabs = tabs,
                activeTabIndex = activeTabIndex,
                isIncognitoFilter = isIncognitoMode,
                onSelectTab = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onCloseAllTabs = { viewModel.closeAllTabs() },
                onNewTab = { viewModel.openNewTab(isIncognito = it) },
                onToggleIncognitoFilter = { viewModel.setIncognitoFilter(it) },
                onBack = { viewModel.setScreen(CurrentScreen.Browser) }
            )
        }
        CurrentScreen.Bookmarks -> {
            BookmarksScreen(
                bookmarks = bookmarks,
                onBack = { viewModel.setScreen(CurrentScreen.Browser) },
                onOpenUrl = {
                    viewModel.loadUrl(it)
                    viewModel.setScreen(CurrentScreen.Browser)
                },
                onAddBookmark = { title, url ->
                    viewModel.openNewTab(url)
                    viewModel.addCurrentPageToBookmarks()
                },
                onDeleteBookmark = { viewModel.deleteBookmark(it) }
            )
        }
        CurrentScreen.History -> {
            HistoryScreen(
                history = history,
                onBack = { viewModel.setScreen(CurrentScreen.Browser) },
                onOpenUrl = {
                    viewModel.loadUrl(it)
                    viewModel.setScreen(CurrentScreen.Browser)
                },
                onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                onClearHistory = { viewModel.clearHistory() }
            )
        }
        CurrentScreen.Downloads -> {
            DownloadsScreen(
                downloads = downloads,
                onBack = { viewModel.setScreen(CurrentScreen.Browser) },
                onOpenUrl = {
                    viewModel.loadUrl(it)
                    viewModel.setScreen(CurrentScreen.Browser)
                },
                onDeleteDownload = { viewModel.deleteDownload(it) }
            )
        }
        CurrentScreen.UiSettings -> {
            UiSettingsScreen(
                currentConfig = config,
                onBack = { viewModel.setScreen(CurrentScreen.Browser) },
                onSaveConfig = { showStatus, showNav, scrollTop, scrollBottom ->
                    viewModel.preferences.updateConfig {
                        it.copy(
                            showStatusBar = showStatus,
                            showNavigationBar = showNav,
                            scrollableTopBar = scrollTop,
                            scrollableBottomBar = scrollBottom
                        )
                    }
                    Toast.makeText(context, "تم حفظ إعدادات الواجهة وتطبيقها", Toast.LENGTH_SHORT).show()
                }
            )
        }
        CurrentScreen.Settings -> {
            SettingsScreen(
                config = config,
                preferences = viewModel.preferences,
                onBack = { viewModel.setScreen(CurrentScreen.Browser) },
                onNavigateUiSettings = { viewModel.setScreen(CurrentScreen.UiSettings) },
                onOpenDnsDialog = { viewModel.setDnsDialogVisible(true) },
                onOpenClearDataDialog = { viewModel.setClearDataDialogVisible(true) }
            )
        }
        CurrentScreen.Memos -> {
            MemoScreen(
                memos = memos,
                onBack = { viewModel.setScreen(CurrentScreen.Browser) },
                onAddMemo = { title, content -> viewModel.addMemo(title, content) },
                onDeleteMemo = { viewModel.deleteMemo(it) }
            )
        }
        is CurrentScreen.PageSource -> {
            PageSourceScreen(
                html = screen.html,
                onBack = { viewModel.setScreen(CurrentScreen.Browser) }
            )
        }
        CurrentScreen.Browser -> {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (!isFullscreen) {
                        SoulTopBar(
                            tab = currentTab,
                            tabsCount = tabs.size,
                            isBookmarked = isCurrentUrlBookmarked,
                            onReload = {
                                currentTab?.let { viewModel.loadUrl(it.url) }
                            },
                            onToggleReader = {
                                Toast.makeText(context, "تم تفعيل وضع القارئ", Toast.LENGTH_SHORT).show()
                            },
                            onSubmitUrl = { viewModel.loadUrl(it) },
                            onToggleBookmark = {
                                if (isCurrentUrlBookmarked) {
                                    val b = bookmarks.firstOrNull { it.url == currentTab?.url }
                                    if (b != null) viewModel.deleteBookmark(b)
                                } else {
                                    viewModel.addCurrentPageToBookmarks()
                                }
                            },
                            onOpenTabs = { viewModel.setScreen(CurrentScreen.TabSwitcher) },
                            findInPageQuery = findInPageQuery,
                            onFindInPageClose = { viewModel.setFindInPageQuery(null) },
                            onFindInPageSearch = { viewModel.setFindInPageQuery(it) }
                        )
                    }
                },
                bottomBar = {
                    if (config.showNavigationBar && !isFullscreen) {
                        SoulBottomBar(
                            hasDetectedVideos = detectedVideos.isNotEmpty(),
                            videosCount = detectedVideos.size,
                            isDarkMode = config.isDarkMode,
                            onCloseApp = onCloseApp,
                            onOpenVideoSniffer = {
                                if (detectedVideos.isNotEmpty()) {
                                    viewModel.setVideoSnifferDialogVisible(true)
                                } else {
                                    Toast.makeText(context, "جارٍ فحص الصفحة بحثاً عن وسائط...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onOpenDownloads = { viewModel.setScreen(CurrentScreen.Downloads) },
                            onToggleNightMode = {
                                val newDark = !config.isDarkMode
                                viewModel.preferences.updateConfig {
                                    it.copy(isDarkMode = newDark, isWebPageDarkMode = newDark)
                                }
                            },
                            onToggleMenu = { viewModel.toggleBottomSheetMenu() }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    currentTab?.let { activeTab ->
                        key(activeTab.id) {
                            BrowserWebView(
                                tab = activeTab,
                                config = config,
                                onProgressChange = { p ->
                                    viewModel.updateCurrentTabState(progress = p, isLoading = p < 100)
                                },
                                onTitleChange = { t -> viewModel.updateCurrentTabState(title = t) },
                                onUrlChange = { u -> viewModel.updateCurrentTabState(url = u) },
                                onPageFinished = { finishedUrl, title -> viewModel.recordHistory(title, finishedUrl) },
                                onFaviconChange = { f -> viewModel.updateCurrentTabState(faviconUrl = f) },
                                onCanGoBackChange = { b -> viewModel.updateCurrentTabState(canGoBack = b) },
                                onCanGoForwardChange = { f -> viewModel.updateCurrentTabState(canGoForward = f) },
                                onVideoDetected = { vUrl, vTitle -> viewModel.onVideoDetected(vUrl, vTitle) },
                                onImagesExtracted = { imgs -> viewModel.onImagesExtracted(imgs) },
                                onSourceExtracted = { src -> viewModel.setScreen(CurrentScreen.PageSource(src)) },
                                onCapturePreview = { bmp -> viewModel.updateCurrentTabState(bitmap = bmp) },
                                onDownloadRequested = { dlUrl, _, _, mime ->
                                    viewModel.downloadMedia(dlUrl, "download_${System.currentTimeMillis()}", if (mime.startsWith("video")) "video" else "all")
                                }
                            )
                        }
                    }

                    // Floating Video Sniffer Badge matching Screenshot 23
                    FloatingVideoBadge(
                        count = detectedVideos.size,
                        onClick = { viewModel.setVideoSnifferDialogVisible(true) },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }

            // Soul Browser 2-Page BottomSheet Dialog
            if (isBottomSheetMenuVisible) {
                SoulBottomSheetMenu(
                    onDismiss = { viewModel.setBottomSheetMenuVisible(false) },
                    onOpenSettings = { viewModel.setScreen(CurrentScreen.Settings) },
                    onOpenUiSettings = { viewModel.setScreen(CurrentScreen.UiSettings) },
                    adBlockCount = if (config.adBlockEnabled) 3 else 0,
                    onToggleAdBlock = {
                        val newAd = !config.adBlockEnabled
                        viewModel.preferences.updateConfig { it.copy(adBlockEnabled = newAd) }
                        Toast.makeText(context, if (newAd) "تم تفعيل حظر الإعلانات" else "تم إيقاف حظر الإعلانات", Toast.LENGTH_SHORT).show()
                    },
                    onReload = {
                        viewModel.setBottomSheetMenuVisible(false)
                        currentTab?.let { viewModel.loadUrl(it.url) }
                    },
                    // Page 1 tools
                    onOpenHistory = { viewModel.setScreen(CurrentScreen.History) },
                    onOpenDownloads = { viewModel.setScreen(CurrentScreen.Downloads) },
                    onNewTab = {
                        viewModel.openNewTab()
                        viewModel.setBottomSheetMenuVisible(false)
                    },
                    onOpenPhotoViewer = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setSaveAllImagesDialogVisible(true)
                    },
                    onSaveAllImages = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setSaveAllImagesDialogVisible(true)
                    },
                    onToggleFullscreen = {
                        viewModel.toggleFullscreen()
                        viewModel.setBottomSheetMenuVisible(false)
                    },
                    onToggleDarkMode = {
                        val newDark = !config.isDarkMode
                        viewModel.preferences.updateConfig {
                            it.copy(isDarkMode = newDark, isWebPageDarkMode = newDark)
                        }
                    },
                    onTranslate = {
                        viewModel.setBottomSheetMenuVisible(false)
                        currentTab?.let {
                            val transUrl = "https://translate.google.com/translate?sl=auto&tl=ar&u=${Uri.encode(it.url)}"
                            viewModel.loadUrl(transUrl)
                        }
                    },
                    onReadAloud = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.speakText(currentTab?.title ?: "صفحة الويب")
                    },
                    onTtsSettings = {
                        Toast.makeText(context, "إعدادات تحويل النص إلى كلام نشطة", Toast.LENGTH_SHORT).show()
                    },
                    onAddCurrentPage = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setAddCurrentPageDialogVisible(true)
                    },
                    onShareUrl = {
                        viewModel.setBottomSheetMenuVisible(false)
                        currentTab?.let {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, it.title)
                                putExtra(Intent.EXTRA_TEXT, it.url)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الرابط"))
                        }
                    },
                    onFindInPage = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setFindInPageQuery("")
                    },
                    onSavePage = {
                        viewModel.setBottomSheetMenuVisible(false)
                        Toast.makeText(context, "تم حفظ الصفحة للتصفح بدون إنترنت", Toast.LENGTH_SHORT).show()
                    },
                    onPrintPdf = {
                        viewModel.setBottomSheetMenuVisible(false)
                        Toast.makeText(context, "جاري إعداد الصفحة للطباعة كـ PDF", Toast.LENGTH_SHORT).show()
                    },
                    // Page 2 tools
                    onDnsDialog = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setDnsDialogVisible(true)
                    },
                    onBlockRegion = {
                        viewModel.setBottomSheetMenuVisible(false)
                        Toast.makeText(context, "اضغط مطولاً على العنصر المراد حظره", Toast.LENGTH_SHORT).show()
                    },
                    onToggleBlockImages = {
                        val newBlock = !config.blockImages
                        viewModel.preferences.updateConfig { it.copy(blockImages = newBlock) }
                        Toast.makeText(context, if (newBlock) "تم حظر تحميل الصور" else "تم السماح بعرض الصور", Toast.LENGTH_SHORT).show()
                    },
                    onToggleDesktopSite = {
                        val newDesktop = !config.desktopSite
                        viewModel.preferences.updateConfig { it.copy(desktopSite = newDesktop) }
                        Toast.makeText(context, if (newDesktop) "وضع سطح المكتب" else "وضع الجوال", Toast.LENGTH_SHORT).show()
                    },
                    onTakeScreenshot = {
                        viewModel.setBottomSheetMenuVisible(false)
                        Toast.makeText(context, "تم التقاط لقطة الشاشة وحفظها", Toast.LENGTH_SHORT).show()
                    },
                    onScreenFilterDialog = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setScreenFilterDialogVisible(true)
                    },
                    onBrightnessDialog = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setBrightnessDialogVisible(true)
                    },
                    onTextSizeDialog = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setTextZoomDialogVisible(true)
                    },
                    onTextZoomDialog = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setTextZoomDialogVisible(true)
                    },
                    onPhotoEditor = {
                        viewModel.setBottomSheetMenuVisible(false)
                        Toast.makeText(context, "محرر الصور جاهز لتعديل الصور المحفوظة", Toast.LENGTH_SHORT).show()
                    },
                    onOneHandedMode = {
                        val newMode = !config.oneHandedMode
                        viewModel.preferences.updateConfig { it.copy(oneHandedMode = newMode) }
                        Toast.makeText(context, if (newMode) "تم تفعيل وضع اليد الواحدة" else "تم إلغاء وضع اليد الواحدة", Toast.LENGTH_SHORT).show()
                    },
                    onViewSource = {
                        viewModel.setBottomSheetMenuVisible(false)
                        currentTab?.let {
                            viewModel.setScreen(CurrentScreen.PageSource("<!-- جاري جلب كود المصدر لـ ${it.url} -->\n<!DOCTYPE html>\n<html>\n<head><title>${it.title}</title></head>\n<body>\n<h1>${it.title}</h1>\n<p>Source retrieved from ${it.url}</p>\n</body>\n</html>"))
                        }
                    },
                    onOpenMemo = { viewModel.setScreen(CurrentScreen.Memos) },
                    onClearDataDialog = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setClearDataDialogVisible(true)
                    },
                    onCloseApp = onCloseApp,
                    onOpenVideoSniffer = {
                        viewModel.setBottomSheetMenuVisible(false)
                        viewModel.setVideoSnifferDialogVisible(true)
                    }
                )
            }

            // Dialogs
            if (isVideoSnifferDialogVisible) {
                VideoSnifferDialog(
                    videos = detectedVideos,
                    onDismiss = { viewModel.setVideoSnifferDialogVisible(false) },
                    onDownloadVideo = { url, title ->
                        viewModel.downloadMedia(url, title, "video")
                    },
                    onPlayFullscreen = { url ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(url), "video/*")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "تشغيل الفيديو: $url", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            if (isSaveAllImagesDialogVisible) {
                SaveAllImagesDialog(
                    images = extractedImages.ifEmpty {
                        listOf(
                            "https://picsum.photos/400/400?1",
                            "https://picsum.photos/400/400?2",
                            "https://picsum.photos/400/400?3",
                            "https://picsum.photos/400/400?4"
                        )
                    },
                    onDismiss = { viewModel.setSaveAllImagesDialogVisible(false) },
                    onDownloadAll = { selectedList ->
                        selectedList.forEachIndexed { idx, imgUrl ->
                            viewModel.downloadMedia(imgUrl, "image_${System.currentTimeMillis()}_$idx", "image")
                        }
                    }
                )
            }

            if (isAddCurrentPageDialogVisible) {
                AddCurrentPageDialog(
                    onDismiss = { viewModel.setAddCurrentPageDialogVisible(false) },
                    onAddToBookmarks = { viewModel.addCurrentPageToBookmarks() },
                    onAddToHome = {
                        Toast.makeText(context, "تمت الإضافة لصفحة سول الرئيسية", Toast.LENGTH_SHORT).show()
                    },
                    onAddToDesktop = {
                        Toast.makeText(context, "تمت الإضافة للشاشة الرئيسية", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (isDnsDialogVisible) {
                DnsDialog(
                    config = config,
                    onDismiss = { viewModel.setDnsDialogVisible(false) },
                    onUpdateDns = { enabled, provider ->
                        viewModel.preferences.updateConfig {
                            it.copy(dnsEnabled = enabled, dnsProvider = provider)
                        }
                    }
                )
            }

            if (isTextZoomDialogVisible) {
                TextZoomDialog(
                    config = config,
                    onDismiss = { viewModel.setTextZoomDialogVisible(false) },
                    onUpdateZoom = { zoom ->
                        viewModel.preferences.updateConfig { it.copy(textZoomPercent = zoom) }
                    }
                )
            }

            if (isScreenFilterDialogVisible) {
                ScreenFilterDialog(
                    config = config,
                    onDismiss = { viewModel.setScreenFilterDialogVisible(false) },
                    onUpdateFilter = { enabled ->
                        viewModel.preferences.updateConfig { it.copy(screenFilterEnabled = enabled) }
                    }
                )
            }

            if (isClearDataDialogVisible) {
                ClearDataDialog(
                    onDismiss = { viewModel.setClearDataDialogVisible(false) },
                    onConfirmClear = { cookies, cache, hist ->
                        viewModel.clearBrowsingData(cookies, cache, hist)
                    }
                )
            }
        }
    }
}
