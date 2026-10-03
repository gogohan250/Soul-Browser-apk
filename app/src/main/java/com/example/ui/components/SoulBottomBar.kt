package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SoulBottomBar(
    hasDetectedVideos: Boolean,
    videosCount: Int,
    isDarkMode: Boolean,
    onCloseApp: () -> Unit,
    onOpenVideoSniffer: () -> Unit,
    onOpenDownloads: () -> Unit,
    onToggleNightMode: () -> Unit,
    onToggleMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Power / Close
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onCloseApp() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "إغلاق",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        // 2. Video Player / Sniffer
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onOpenVideoSniffer() },
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (hasDetectedVideos && videosCount > 0) {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text(videosCount.toString(), fontSize = 10.sp)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.OndemandVideo,
                    contentDescription = "عارض الفيديوهات",
                    tint = if (hasDetectedVideos) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 3. Downloads
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onOpenDownloads() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "التنزيلات",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        // 4. Night Mode
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onToggleNightMode() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.DarkMode,
                contentDescription = "الوضع الليلي",
                tint = if (isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        // 5. Menu
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onToggleMenu() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SentimentSatisfied,
                contentDescription = "القائمة",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
