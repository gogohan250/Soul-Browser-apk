package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BrowserConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCurrentPageDialog(
    onDismiss: () -> Unit,
    onAddToBookmarks: () -> Unit,
    onAddToHome: () -> Unit,
    onAddToDesktop: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            ListItem(
                headlineContent = { Text("إضافة إلى الإشارات المرجعية") },
                trailingContent = { Icon(Icons.Outlined.StarOutline, contentDescription = null) },
                modifier = Modifier.clickable {
                    onAddToBookmarks()
                    onDismiss()
                }
            )
            ListItem(
                headlineContent = { Text("إضافة إلى صفحة سول الرئيسية") },
                trailingContent = { Icon(Icons.Outlined.AddHome, contentDescription = null) },
                modifier = Modifier.clickable {
                    onAddToHome()
                    onDismiss()
                }
            )
            ListItem(
                headlineContent = { Text("إضافة إلى الشاشة الرئيسية للهاتف") },
                trailingContent = { Icon(Icons.Outlined.AddToHomeScreen, contentDescription = null) },
                modifier = Modifier.clickable {
                    onAddToDesktop()
                    onDismiss()
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إغلاق")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsDialog(
    config: BrowserConfig,
    onDismiss: () -> Unit,
    onUpdateDns: (enabled: Boolean, provider: String) -> Unit
) {
    var isEnabled by remember(config.dnsEnabled) { mutableStateOf(config.dnsEnabled) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("DNS", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("قد لا يكون مدعوماً في جميع المناطق.", fontSize = 13.sp, color = Color.Gray)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        isEnabled = it
                        onUpdateDns(it, config.dnsProvider)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("الخادم", fontSize = 13.sp, color = Color.Gray)
            Text(config.dnsProvider, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(12.dp))

            Text("زيارة الموقع", fontSize = 13.sp, color = Color.Gray)
            Text("https://developers.google.com/speed/public-dns", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إغلاق")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextZoomDialog(
    config: BrowserConfig,
    onDismiss: () -> Unit,
    onUpdateZoom: (Int) -> Unit
) {
    var zoomValue by remember(config.textZoomPercent) { mutableStateOf(config.textZoomPercent.toFloat()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("تكبير النص", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الحجم الافتراضي")
                Text("${zoomValue.toInt()}%", fontWeight = FontWeight.Bold)
            }

            Slider(
                value = zoomValue,
                onValueChange = { zoomValue = it },
                valueRange = 50f..250f,
                steps = 19
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = {
                    zoomValue = 100f
                    onUpdateZoom(100)
                }) {
                    Text("إعادة تعيين")
                }
                Button(onClick = {
                    onUpdateZoom(zoomValue.toInt())
                    onDismiss()
                }) {
                    Text("تطبيق")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenFilterDialog(
    config: BrowserConfig,
    onDismiss: () -> Unit,
    onUpdateFilter: (enabled: Boolean) -> Unit
) {
    var enabled by remember(config.screenFilterEnabled) { mutableStateOf(config.screenFilterEnabled) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("فلتر الشاشة (حماية العين)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Switch(checked = enabled, onCheckedChange = {
                    enabled = it
                    onUpdateFilter(it)
                })
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("لون الفلتر: أصفر دافئ", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    onUpdateFilter(enabled)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تطبيق")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearDataDialog(
    onDismiss: () -> Unit,
    onConfirmClear: (cookies: Boolean, cache: Boolean, history: Boolean) -> Unit
) {
    var clearHistory by remember { mutableStateOf(true) }
    var clearCookies by remember { mutableStateOf(true) }
    var clearCache by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("مسح البيانات", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { clearHistory = !clearHistory }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = clearHistory, onCheckedChange = { clearHistory = it })
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("السجل وعمليات البحث الأخيرة", fontWeight = FontWeight.Medium)
                    Text("سجل الصفحات التي تمت زيارتها", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { clearCookies = !clearCookies }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = clearCookies, onCheckedChange = { clearCookies = it })
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("ملفات تعريف الارتباط (Cookies)", fontWeight = FontWeight.Medium)
                    Text("بيانات تسجيل الدخول للمواقع", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { clearCache = !clearCache }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = clearCache, onCheckedChange = { clearCache = it })
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("ذاكرة التخزين المؤقت (Cache)", fontWeight = FontWeight.Medium)
                    Text("الملفات والصور المخزنة لتسريع التصفح", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("إلغاء")
                }
                Button(
                    onClick = { onConfirmClear(clearCookies, clearCache, clearHistory) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("حذف")
                }
            }
        }
    }
}
