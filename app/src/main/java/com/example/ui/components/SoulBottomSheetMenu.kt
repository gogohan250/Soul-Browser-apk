package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class SoulMenuItem(
    val title: String,
    val icon: ImageVector,
    val isHighlighted: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoulBottomSheetMenu(
    onDismiss: () -> Unit,
    // Top mini bar actions
    onOpenSettings: () -> Unit,
    onOpenUiSettings: () -> Unit,
    adBlockCount: Int,
    onToggleAdBlock: () -> Unit,
    onReload: () -> Unit,
    // Page 1 actions
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onNewTab: () -> Unit,
    onOpenPhotoViewer: () -> Unit,
    onSaveAllImages: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onTranslate: () -> Unit,
    onReadAloud: () -> Unit,
    onTtsSettings: () -> Unit,
    onAddCurrentPage: () -> Unit,
    onShareUrl: () -> Unit,
    onFindInPage: () -> Unit,
    onSavePage: () -> Unit,
    onPrintPdf: () -> Unit,
    // Page 2 actions
    onDnsDialog: () -> Unit,
    onBlockRegion: () -> Unit,
    onToggleBlockImages: () -> Unit,
    onToggleDesktopSite: () -> Unit,
    onTakeScreenshot: () -> Unit,
    onScreenFilterDialog: () -> Unit,
    onBrightnessDialog: () -> Unit,
    onTextSizeDialog: () -> Unit,
    onTextZoomDialog: () -> Unit,
    onPhotoEditor: () -> Unit,
    onOneHandedMode: () -> Unit,
    onViewSource: () -> Unit,
    onOpenMemo: () -> Unit,
    onClearDataDialog: () -> Unit,
    // Bottom icons
    onCloseApp: () -> Unit,
    onOpenVideoSniffer: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 12.dp)
        ) {
            // 1. Top Mini Bar inside bottom sheet (Settings, Layout, AdBlock Badge, Reload)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenSettings, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Settings, contentDescription = "الإعدادات", modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onOpenUiSettings, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.DashboardCustomize, contentDescription = "التخطيط", modifier = Modifier.size(20.dp))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ad Block shield badge with count
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleAdBlock() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = adBlockCount.toString(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Outlined.Shield,
                            contentDescription = "حظر الإعلانات",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onReload, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "إعادة تحميل", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Page indicator line (2 pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f))
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f))
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                )
            }

            // 2. Horizontal Pager for Page 1 & Page 2 (15 tools each)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
            ) { page ->
                if (page == 0) {
                    // Page 1 tools grid (Screenshot 7)
                    ToolsGrid(
                        items = listOf(
                            SoulMenuItem("السجل", Icons.Outlined.History, onClick = onOpenHistory),
                            SoulMenuItem("التنزيلات", Icons.Outlined.FileDownload, onClick = onOpenDownloads),
                            SoulMenuItem("علامة تبويب جديدة", Icons.Outlined.AddBox, onClick = onNewTab),
                            SoulMenuItem("عارض الصور", Icons.Outlined.Image, onClick = onOpenPhotoViewer),
                            SoulMenuItem("حفظ جميع الصور", Icons.Outlined.CloudDownload, onClick = onSaveAllImages),

                            SoulMenuItem("ملء الشاشة", Icons.Outlined.Fullscreen, onClick = onToggleFullscreen),
                            SoulMenuItem("الوضع الداكن", Icons.Outlined.InvertColors, onClick = onToggleDarkMode),
                            SoulMenuItem("ترجمة", Icons.Outlined.Translate, onClick = onTranslate),
                            SoulMenuItem("القراءة بصوت عالٍ", Icons.Outlined.RecordVoiceOver, onClick = onReadAloud),
                            SoulMenuItem("تحويل النص إلى كلام", Icons.Outlined.TextSnippet, onClick = onTtsSettings),

                            SoulMenuItem("إضافة الصفحة الحالية", Icons.Outlined.BookmarkAdd, onClick = onAddCurrentPage),
                            SoulMenuItem("مشاركة عنوان URL", Icons.Outlined.Share, onClick = onShareUrl),
                            SoulMenuItem("بحث في الصفحة", Icons.Outlined.FindInPage, onClick = onFindInPage),
                            SoulMenuItem("حفظ الصفحة", Icons.Outlined.SaveAlt, onClick = onSavePage),
                            SoulMenuItem("طباعة / PDF", Icons.Outlined.Print, onClick = onPrintPdf)
                        )
                    )
                } else {
                    // Page 2 tools grid (Screenshot 8)
                    ToolsGrid(
                        items = listOf(
                            SoulMenuItem("DNS", Icons.Outlined.VpnKey, onClick = onDnsDialog),
                            SoulMenuItem("حظر الإعلانات", Icons.Outlined.Security, onClick = onToggleAdBlock),
                            SoulMenuItem("حظر المنطقة", Icons.Outlined.CropFree, onClick = onBlockRegion),
                            SoulMenuItem("حظر الصور", Icons.Outlined.HideImage, onClick = onToggleBlockImages),
                            SoulMenuItem("سطح المكتب", Icons.Outlined.DesktopWindows, onClick = onToggleDesktopSite),

                            SoulMenuItem("لقطة شاشة", Icons.Outlined.Crop, onClick = onTakeScreenshot),
                            SoulMenuItem("فلتر الشاشة", Icons.Outlined.Tonality, onClick = onScreenFilterDialog),
                            SoulMenuItem("السطوع", Icons.Outlined.BrightnessMedium, onClick = onBrightnessDialog),
                            SoulMenuItem("حجم النص", Icons.Outlined.FormatSize, onClick = onTextSizeDialog),
                            SoulMenuItem("تكبير النص", Icons.Outlined.ZoomIn, onClick = onTextZoomDialog),

                            SoulMenuItem("محرر الصور", Icons.Outlined.Palette, onClick = onPhotoEditor),
                            SoulMenuItem("وضع اليد الواحدة", Icons.Outlined.PanTool, onClick = onOneHandedMode),
                            SoulMenuItem("مصدر الصفحة", Icons.Outlined.Code, onClick = onViewSource),
                            SoulMenuItem("مذكرة", Icons.Outlined.NoteAlt, onClick = onOpenMemo),
                            SoulMenuItem("مسح البيانات", Icons.Outlined.DeleteSweep, onClick = onClearDataDialog)
                        )
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 3. Bottom controls bar matching bottom of screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseApp) {
                    Icon(Icons.Outlined.PowerSettingsNew, contentDescription = "إغلاق")
                }
                IconButton(onClick = onOpenVideoSniffer) {
                    Icon(Icons.Outlined.OndemandVideo, contentDescription = "فيديو")
                }
                IconButton(onClick = onOpenDownloads) {
                    Icon(Icons.Outlined.FileDownload, contentDescription = "تنزيلات")
                }
                IconButton(onClick = onToggleDarkMode) {
                    Icon(Icons.Outlined.DarkMode, contentDescription = "وضع الليل")
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.SentimentSatisfied, contentDescription = "إغلاق القائمة")
                }
            }
        }
    }
}

@Composable
private fun ToolsGrid(items: List<SoulMenuItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (rowIndex in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (colIndex in 0 until 5) {
                    val itemIndex = rowIndex * 5 + colIndex
                    if (itemIndex in items.indices) {
                        val item = items[itemIndex]
                        ToolItemCell(item = item, modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolItemCell(item: SoulMenuItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { item.onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (item.isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
        )
    }
}
