package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BrowserConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiSettingsScreen(
    currentConfig: BrowserConfig,
    onBack: () -> Unit,
    onSaveConfig: (showStatusBar: Boolean, showNavBar: Boolean, scrollableTop: Boolean, scrollableBottom: Boolean) -> Unit
) {
    var showStatusBar by remember { mutableStateOf(currentConfig.showStatusBar) }
    var showNavBar by remember { mutableStateOf(currentConfig.showNavigationBar) }
    var scrollableTopBar by remember { mutableStateOf(currentConfig.scrollableTopBar) }
    var scrollableBottomBar by remember { mutableStateOf(currentConfig.scrollableBottomBar) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تخصيص الواجهة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interactive Phone Mockup (Matching Screenshot 10)
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(340.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(3.dp, Color.Black, RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mock Status Bar
                    if (showStatusBar) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("12:00", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("📶 🔋", fontSize = 8.sp)
                        }
                    }

                    // Mock URL Top Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(13.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("www.google.com", fontSize = 9.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mock Browser Content (Cute dog preview)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mockup_preview_dog_1791052428250),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mock Bottom Navigation Bar
                    if (showNavBar) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏻", fontSize = 10.sp)
                            Text("▶", fontSize = 10.sp)
                            Text("⬇", fontSize = 10.sp)
                            Text("☾", fontSize = 10.sp)
                            Text("☺", fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Toggles matching Screenshot 10
            SettingToggleItem(
                title = "إظهار شريط الحالة",
                checked = showStatusBar,
                onCheckedChange = { showStatusBar = it }
            )

            SettingToggleItem(
                title = "إظهار شريط التنقل",
                checked = showNavBar,
                onCheckedChange = { showNavBar = it }
            )

            SettingToggleItem(
                title = "الشريط العلوي قابل للتمرير",
                checked = scrollableTopBar,
                onCheckedChange = { scrollableTopBar = it }
            )

            SettingToggleItem(
                title = "الشريط السفلي قابل للتمرير",
                checked = scrollableBottomBar,
                onCheckedChange = { scrollableBottomBar = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Apply button
            Button(
                onClick = {
                    onSaveConfig(showStatusBar, showNavBar, scrollableTopBar, scrollableBottomBar)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("تطبيق", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
