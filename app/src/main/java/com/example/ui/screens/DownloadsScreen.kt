package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DownloadItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloads: List<DownloadItem>,
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onDeleteDownload: (DownloadItem) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("الكل") }
    var isDetailedInfo by remember { mutableStateOf(true) }
    var isSingleLineTitle by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "فيديو", "صورة", "صوت")

    val filteredList = downloads.filter { item ->
        when (selectedCategory) {
            "فيديو" -> item.category == "video" || item.fileName.endsWith(".mp4")
            "صورة" -> item.category == "image" || item.fileName.endsWith(".png") || item.fileName.endsWith(".jpg")
            "صوت" -> item.fileName.endsWith(".mp3")
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                title = {
                    // Category dropdown button
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCategoryMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCategory, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    trailingIcon = {
                                        if (selectedCategory == cat) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = cat
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "تحديث التنزيلات", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                    }
                    IconButton(onClick = { Toast.makeText(context, "بحث في التنزيلات", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Search, contentDescription = "بحث")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "المزيد")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("فرز") },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "تم الفرز حسب التاريخ", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("عرض المعلومات التفصيلية") },
                                trailingIcon = {
                                    Switch(checked = isDetailedInfo, onCheckedChange = { isDetailedInfo = it })
                                },
                                onClick = { isDetailedInfo = !isDetailedInfo }
                            )
                            DropdownMenuItem(
                                text = { Text("عنوان في سطر واحد") },
                                trailingIcon = {
                                    Switch(checked = isSingleLineTitle, onCheckedChange = { isSingleLineTitle = it })
                                },
                                onClick = { isSingleLineTitle = !isSingleLineTitle }
                            )
                            DropdownMenuItem(
                                text = { Text("تحديد الدليل") },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "مجلد التنزيلات: /Download/Soul", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Soul",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                            fontSize = 32.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد ملفات تم تنزيلها", color = Color.Gray, fontSize = 15.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList) { item ->
                        DownloadItemRow(
                            item = item,
                            isDetailed = isDetailedInfo,
                            isSingleLine = isSingleLineTitle,
                            onOpen = {
                                Toast.makeText(context, "فتح الملف: ${item.fileName}", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { onDeleteDownload(item) }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }

            // Floating "Visit Source Domain" chip matching Screenshot 3
            val activeDomain = filteredList.firstOrNull()?.sourceDomain ?: "google.com"
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenUrl("https://$activeDomain") },
                color = Color(0xFF2E3440),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Visit ${activeDomain.uppercase()}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItemRow(
    item: DownloadItem,
    isDetailed: Boolean,
    isSingleLine: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    var showItemMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 3-dots options
        Box {
            IconButton(onClick = { showItemMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "خيارات", tint = Color.Gray)
            }
            DropdownMenu(
                expanded = showItemMenu,
                onDismissRequest = { showItemMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("فتح") },
                    onClick = {
                        showItemMenu = false
                        onOpen()
                    }
                )
                DropdownMenuItem(
                    text = { Text("حذف") },
                    onClick = {
                        showItemMenu = false
                        onDelete()
                    }
                )
            }
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.fileName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = if (isSingleLine) 1 else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isDetailed) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.fileSizeFormatted,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = dateFormat.format(Date(item.timestamp)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Extension icon (APK android, MP4 film, PNG image, file)
        val icon = when {
            item.fileName.endsWith(".apk") -> Icons.Default.Android
            item.fileName.endsWith(".mp4") || item.category == "video" -> Icons.Default.Movie
            item.fileName.endsWith(".png") || item.fileName.endsWith(".jpg") -> Icons.Default.Image
            else -> Icons.Default.InsertDriveFile
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(28.dp)
        )
    }
}
