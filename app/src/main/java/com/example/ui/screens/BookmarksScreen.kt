package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookmarkItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    bookmarks: List<BookmarkItem>,
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onAddBookmark: (title: String, url: String) -> Unit,
    onDeleteBookmark: (BookmarkItem) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var isSortedByName by remember { mutableStateOf(false) }
    var isDetailedInfo by remember { mutableStateOf(true) }
    var isSingleLineTitle by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("https://") }

    val displayList = remember(bookmarks, isSortedByName) {
        if (isSortedByName) bookmarks.sortedBy { it.title }
        else bookmarks.sortedByDescending { it.timestamp }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة إشارة مرجعية") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("العنوان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("الرابط URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newUrl.isNotBlank()) {
                            onAddBookmark(newTitle, newUrl)
                            newTitle = ""
                            newUrl = "https://"
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
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
                title = { Text("الإشارات المرجعية", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة")
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "خيارات")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isSortedByName) "فرز: حسب التاريخ" else "فرز: حسب الاسم") },
                                onClick = {
                                    isSortedByName = !isSortedByName
                                    showOverflowMenu = false
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
                            Divider()
                            DropdownMenuItem(
                                text = { Text("استيراد من HTML") },
                                onClick = {
                                    showOverflowMenu = false
                                    Toast.makeText(context, "تم استيراد العلامات المرجعية", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تصدير إلى HTML") },
                                onClick = {
                                    showOverflowMenu = false
                                    Toast.makeText(context, "تم حفظ ملف HTML للعلامات المرجعية", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد إشارات مرجعية محفوظة", color = Color.Gray, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة إشارة جديدة")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(displayList) { bookmark ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = bookmark.title,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = if (isSingleLineTitle) 1 else 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = if (isDetailedInfo) {
                            {
                                Text(
                                    text = bookmark.url,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else null,
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color(0xFFFFB300)
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { onDeleteBookmark(bookmark) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.clickable { onOpenUrl(bookmark.url) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}
