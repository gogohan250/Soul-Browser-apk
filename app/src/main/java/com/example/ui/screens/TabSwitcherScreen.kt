package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TabInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    tabs: List<TabInfo>,
    activeTabIndex: Int,
    isIncognitoFilter: Boolean,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onCloseAllTabs: () -> Unit,
    onNewTab: (isIncognito: Boolean) -> Unit,
    onToggleIncognitoFilter: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val filteredTabs = tabs.mapIndexed { index, tab -> Pair(index, tab) }
        .filter { (_, tab) -> tab.isIncognito == isIncognitoFilter }
        .filter { (_, tab) ->
            if (searchQuery.isBlank()) true
            else tab.title.contains(searchQuery, ignoreCase = true) || tab.url.contains(searchQuery, ignoreCase = true)
        }

    val backgroundColor = if (isIncognitoFilter) Color(0xFF18181B) else MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isIncognitoFilter) Color(0xFF202024) else MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("بحث في التبويبات...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isIncognitoFilter) "التصفح المتخفي (${filteredTabs.size})" else "التبويبات (${filteredTabs.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(Icons.Outlined.Search, contentDescription = "بحث")
                    }
                    IconButton(onClick = onCloseAllTabs) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "حذف الكل")
                    }
                    IconButton(onClick = { onNewTab(isIncognitoFilter) }) {
                        Icon(Icons.Default.Add, contentDescription = "تبويب جديد")
                    }
                }
            )
        },
        bottomBar = {
            // Mode Switcher: Normal Tabs vs Incognito Tabs
            Surface(
                color = if (isIncognitoFilter) Color(0xFF27272A) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !isIncognitoFilter,
                        onClick = { onToggleIncognitoFilter(false) },
                        label = { Text("الوضع العادي") },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )

                    FilterChip(
                        selected = isIncognitoFilter,
                        onClick = { onToggleIncognitoFilter(true) },
                        label = { Text("التصفح المتخفي") },
                        leadingIcon = { Icon(Icons.Outlined.Shield, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (filteredTabs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isIncognitoFilter) Icons.Outlined.Shield else Icons.Default.Tab,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isIncognitoFilter) "لا توجد علامات تبويب للتصفح المتخفي" else "لا توجد علامات تبويب مفتوحة",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onNewTab(isIncognitoFilter) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("علامة تبويب جديدة")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredTabs) { _, (origIndex, tab) ->
                    val isActive = origIndex == activeTabIndex
                    TabCard(
                        tab = tab,
                        isActive = isActive,
                        onSelect = { onSelectTab(origIndex) },
                        onClose = { onCloseTab(origIndex) }
                    )
                }
            }
        }
    }
}

@Composable
fun TabCard(
    tab: TabInfo,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isActive) 2.5.dp else 0.5.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (tab.isIncognito) Color(0xFF27272A) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Card Header: Favicon/Icon, Title, Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(if (tab.isIncognito) Color(0xFF1E1E22) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (tab.isIncognito) Icons.Outlined.Shield else Icons.Default.Public,
                        contentDescription = null,
                        tint = if (tab.isIncognito) Color(0xFFBA68C8) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.title.ifBlank { "علامة تبويب" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Thumbnail Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(if (tab.isIncognito) Color(0xFF121214) else Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (tab.previewBitmap != null) {
                    Image(
                        bitmap = tab.previewBitmap!!.asImageBitmap(),
                        contentDescription = "معاينة التبويب",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tab.url,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
