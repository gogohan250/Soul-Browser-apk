package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.BrowserPreferences
import com.example.data.BrowserConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    config: BrowserConfig,
    preferences: BrowserPreferences,
    onBack: () -> Unit,
    onNavigateUiSettings: () -> Unit,
    onOpenDnsDialog: () -> Unit,
    onOpenClearDataDialog: () -> Unit
) {
    val context = LocalContext.current
    var showSearchEngineDialog by remember { mutableStateOf(false) }

    if (showSearchEngineDialog) {
        val engines = listOf("Google", "Bing", "DuckDuckGo")
        AlertDialog(
            onDismissRequest = { showSearchEngineDialog = false },
            title = { Text("محرك البحث") },
            text = {
                Column {
                    engines.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    preferences.updateConfig { it.copy(searchEngine = engine) }
                                    showSearchEngineDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.searchEngine == engine,
                                onClick = {
                                    preferences.updateConfig { it.copy(searchEngine = engine) }
                                    showSearchEngineDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(engine, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                title = { Text("الإعدادات", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // General
            SettingsSectionHeader("عام")
            SettingsClickableItem(
                title = "محرك البحث",
                subtitle = config.searchEngine,
                icon = Icons.Outlined.Search,
                onClick = { showSearchEngineDialog = true }
            )
            SettingsClickableItem(
                title = "التخطيط وعناصر الواجهة",
                subtitle = "شريط الحالة، شريط التنقل، التمرير",
                icon = Icons.Outlined.DashboardCustomize,
                onClick = onNavigateUiSettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Web cleaner
            SettingsSectionHeader("منظف الويب والأمان")
            SettingsToggleItem(
                title = "حظر الإعلانات",
                subtitle = "حظر الإعلانات المزعجة وعناصر التتبع",
                icon = Icons.Outlined.Shield,
                checked = config.adBlockEnabled,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(adBlockEnabled = chk) }
                }
            )
            SettingsToggleItem(
                title = "حظر الصور",
                subtitle = "توفير استهلاك البيانات",
                icon = Icons.Outlined.HideImage,
                checked = config.blockImages,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(blockImages = chk) }
                }
            )
            SettingsClickableItem(
                title = "DNS الخاص",
                subtitle = if (config.dnsEnabled) config.dnsProvider else "معطل",
                icon = Icons.Outlined.VpnKey,
                onClick = onOpenDnsDialog
            )
            SettingsClickableItem(
                title = "مسح بيانات التصفح",
                subtitle = "الكوكيز، الذاكرة المؤقتة، السجل",
                icon = Icons.Outlined.DeleteSweep,
                onClick = onOpenClearDataDialog
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display
            SettingsSectionHeader("العرض والألوان")
            SettingsToggleItem(
                title = "الوضع الداكن للتطبيق (UI)",
                subtitle = "السمة الداكنة لجميع القوائم",
                icon = Icons.Outlined.DarkMode,
                checked = config.isDarkMode,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(isDarkMode = chk) }
                }
            )
            SettingsToggleItem(
                title = "الوضع الداكن لصفحات الويب",
                subtitle = "عكس الألوان تلقائياً في المواقع",
                icon = Icons.Outlined.InvertColors,
                checked = config.isWebPageDarkMode,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(isWebPageDarkMode = chk) }
                }
            )
            SettingsToggleItem(
                title = "صورة داكنة (${config.darkImageBrightnessPercent}%)",
                subtitle = "تعتيم الصور الساطعة في الوضع الليلي",
                icon = Icons.Outlined.Brightness4,
                checked = config.darkImageDimming,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(darkImageDimming = chk) }
                }
            )
            SettingsToggleItem(
                title = "فلتر الشاشة (ضوء ليلي)",
                subtitle = "فلتر أصفر مريح للعين",
                icon = Icons.Outlined.Tonality,
                checked = config.screenFilterEnabled,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(screenFilterEnabled = chk) }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced
            SettingsSectionHeader("متقدم")
            SettingsToggleItem(
                title = "موقع سطح المكتب الافتراضي",
                subtitle = "طلب إصدار سطح المكتب للمواقع",
                icon = Icons.Outlined.DesktopWindows,
                checked = config.desktopSite,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(desktopSite = chk) }
                }
            )
            SettingsToggleItem(
                title = "السحب لإغلاق التبويب",
                subtitle = "إغلاق التبويب من خلال السحب في الشبكة",
                icon = Icons.Outlined.Swipe,
                checked = config.swipeToDismissTab,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(swipeToDismissTab = chk) }
                }
            )
            SettingsToggleItem(
                title = "إلغاء حذف التبويب",
                subtitle = "إمكانية استرجاع التبويب المغلق فوراً",
                icon = Icons.Outlined.Undo,
                checked = config.undoDeleteTab,
                onCheckedChange = { chk ->
                    preferences.updateConfig { it.copy(undoDeleteTab = chk) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp) },
        supportingContent = if (subtitle != null) { { Text(subtitle, fontSize = 13.sp, color = Color.Gray) } } else null,
        leadingContent = if (icon != null) {
            { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray) },
        modifier = Modifier.clickable { onClick() }
    )
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp) },
        supportingContent = if (subtitle != null) { { Text(subtitle, fontSize = 13.sp, color = Color.Gray) } } else null,
        leadingContent = if (icon != null) {
            { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}
