package com.example.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppUsageRule
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.WarningAmber

@Composable
fun ParentAppLimitsScreen(
    appRules: List<AppUsageRule>,
    onToggleBlock: (String) -> Unit,
    onUpdateLimit: (String, Int) -> Unit,
    onAddUsage: (String, Int) -> Unit,
    onResetUsage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDowntimeActive by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scheduled Downtime Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scheduled_downtime_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF673AB7).copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = Color(0xFF673AB7),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Nighttime Downtime",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Daily: 9:00 PM – 7:00 AM (All non-essential apps locked)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isDowntimeActive,
                        onCheckedChange = { isDowntimeActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF673AB7)
                        ),
                        modifier = Modifier.testTag("downtime_switch")
                    )
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Application Quotas & Access Rules",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${appRules.count { it.isBlocked }} blocked",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AlertRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // App Rule Items
        items(appRules, key = { it.packageName }) { rule ->
            AppRuleCard(
                rule = rule,
                onToggleBlock = { onToggleBlock(rule.packageName) },
                onUpdateLimit = { limit -> onUpdateLimit(rule.packageName, limit) },
                onAddUsage = { onAddUsage(rule.packageName, 15) },
                onResetUsage = { onResetUsage(rule.packageName) }
            )
        }

        // MDM Architectural Enforcement Explanation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Device Administration & App Enforcement Flow",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Accessibility & UsageStatsManager detect child app launches in real-time.\n• If quota is exceeded or marked blocked, AirDroid Kids immediately overlays an un-dismissible blocking window.\n• Device Admin privileges prevent the child from force-stopping or uninstalling protection without parental passcode.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AppRuleCard(
    rule: AppUsageRule,
    onToggleBlock: () -> Unit,
    onUpdateLimit: (Int) -> Unit,
    onAddUsage: () -> Unit,
    onResetUsage: () -> Unit
) {
    val isLimitExceeded = rule.usedMinutesToday >= rule.dailyLimitMinutes
    val progress = if (rule.dailyLimitMinutes > 0) {
        (rule.usedMinutesToday.toFloat() / rule.dailyLimitMinutes).coerceIn(0f, 1f)
    } else 1f

    val appIcon: ImageVector = when (rule.iconKey) {
        "video" -> Icons.Default.SmartDisplay
        "chat" -> Icons.Default.Chat
        "game" -> Icons.Default.Gamepad
        "browser" -> Icons.Default.Public
        else -> Icons.Default.Block
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_rule_card_${rule.packageName}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // App Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (rule.isBlocked) AlertRedContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = appIcon,
                                contentDescription = null,
                                tint = if (rule.isBlocked) AlertRed else EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = rule.appName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${rule.category} • ${rule.packageName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Block toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (rule.isBlocked) "Blocked" else "Allowed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (rule.isBlocked) AlertRed else EmeraldPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = !rule.isBlocked,
                        onCheckedChange = { onToggleBlock() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary,
                            uncheckedTrackColor = AlertRedContainer,
                            uncheckedThumbColor = AlertRed
                        ),
                        modifier = Modifier.testTag("block_switch_${rule.packageName}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Usage & Limit indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Usage Today: ${rule.usedMinutesToday}m / ${rule.dailyLimitMinutes}m limit",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLimitExceeded) AlertRed else MaterialTheme.colorScheme.onSurface
                    )
                )

                if (isLimitExceeded) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AlertRedContainer
                    ) {
                        Text(
                            text = "LIMIT REACHED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AlertRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isLimitExceeded) AlertRed else if (progress > 0.8f) WarningAmber else EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Slider for setting daily limit
            Text(
                text = "Adjust Daily Quota: ${rule.dailyLimitMinutes} mins",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Slider(
                value = rule.dailyLimitMinutes.toFloat(),
                onValueChange = { onUpdateLimit(it.toInt()) },
                valueRange = 15f..180f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = EmeraldPrimary,
                    activeTrackColor = EmeraldPrimary
                ),
                modifier = Modifier.testTag("slider_limit_${rule.packageName}")
            )

            // Test simulation actions: Add usage time & reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onResetUsage,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Time", fontSize = 11.sp)
                }

                Button(
                    onClick = onAddUsage,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("+15m Usage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
