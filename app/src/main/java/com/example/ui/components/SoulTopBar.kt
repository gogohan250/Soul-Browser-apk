package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TabInfo

@Composable
fun SoulTopBar(
    tab: TabInfo?,
    tabsCount: Int,
    isBookmarked: Boolean,
    onReload: () -> Unit,
    onToggleReader: () -> Unit,
    onSubmitUrl: (String) -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenTabs: () -> Unit,
    findInPageQuery: String?,
    onFindInPageClose: () -> Unit,
    onFindInPageSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var urlInput by remember(tab?.url) { mutableStateOf(tab?.url ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Find in page bar if active
        AnimatedVisibility(visible = findInPageQuery != null) {
            var queryText by remember { mutableStateOf(findInPageQuery ?: "") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "بحث", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = queryText,
                    onValueChange = {
                        queryText = it
                        onFindInPageSearch(it)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                )
                IconButton(onClick = onFindInPageClose) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
        }

        // Main URL Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Reload with red notification dot
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onReload) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, end = 10.dp)
                        .size(6.dp)
                        .background(Color.Red, CircleShape)
                )
            }

            // Reader Mode icon
            IconButton(onClick = onToggleReader) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = "وضع القارئ",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // URL Address Pill
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (tab?.isIncognito == true) Color(0xFF2A2A2A)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lock / Incognito icon
                if (tab?.isIncognito == true) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "تصفح متخفي",
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "آمن",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Address field
                Box(modifier = Modifier.weight(1f)) {
                    if (urlInput.isEmpty() && !isEditing) {
                        Text(
                            text = "ابحث أو اكتب عنوان URL",
                            style = TextStyle(fontSize = 14.sp, color = Color.Gray),
                            maxLines = 1
                        )
                    }

                    BasicTextField(
                        value = if (isEditing) urlInput else (tab?.title ?: urlInput),
                        onValueChange = {
                            urlInput = it
                            isEditing = true
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            focusManager.clearFocus()
                            isEditing = false
                            onSubmitUrl(urlInput)
                        }),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isEditing && urlInput.isNotEmpty()) {
                    IconButton(
                        onClick = { urlInput = "" },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Bookmark Star Icon
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "الإشارات المرجعية",
                    tint = if (isBookmarked) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface
                )
            }

            // Tab counter button (shows number of tabs e.g. "3")
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onOpenTabs() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabsCount.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Web loading progress bar
        if (tab?.isLoading == true && tab.progress in 1..99) {
            LinearProgressIndicator(
                progress = { tab.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }
    }
}
