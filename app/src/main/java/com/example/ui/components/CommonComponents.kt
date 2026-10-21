package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppMode
import com.example.model.RiskLevel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.WarningAmber

@Composable
fun ModeSwitcherHeader(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mode_switcher_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "AirDroid Security",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AirDroid System Architecture",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "LIVE SYNC",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two-segment pill toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Parent Mode Tab
                val isParent = currentMode == AppMode.PARENT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isParent) EmeraldPrimary else Color.Transparent)
                        .clickable { onModeChange(AppMode.PARENT) }
                        .padding(vertical = 10.dp)
                        .testTag("mode_parent_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Parent Control",
                            tint = if (isParent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Parent Control",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isParent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isParent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Child Mode Tab
                val isChild = currentMode == AppMode.CHILD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isChild) SignalBlue else Color.Transparent)
                        .clickable { onModeChange(AppMode.CHILD) }
                        .padding(vertical = 10.dp)
                        .testTag("mode_child_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = "AirDroid Kids",
                            tint = if (isChild) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AirDroid Kids",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isChild) FontWeight.Bold else FontWeight.Medium,
                                color = if (isChild) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PulsingLiveDot(
    modifier: Modifier = Modifier,
    color: Color = AlertRed
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun RiskLevelChip(riskLevel: RiskLevel) {
    val (bg, textColor, label) = when (riskLevel) {
        RiskLevel.HIGH -> Triple(AlertRedContainer, AlertRed, "HIGH RISK")
        RiskLevel.MEDIUM -> Triple(Color(0xFFFFF3E0), WarningAmber, "MED RISK")
        RiskLevel.LOW -> Triple(Color(0xFFE8F5E9), EmeraldPrimary, "LOW RISK")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = Modifier.testTag("risk_chip_${riskLevel.name.lowercase()}")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DeviceTelemetryRow(
    batteryLevel: Int,
    isOnline: Boolean,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Online Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isOnline) EmeraldPrimary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) EmeraldPrimary else Color.Gray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) EmeraldPrimary else Color.Gray
                    )
                )
            }
        }

        // Battery
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = if (batteryLevel > 20) EmeraldPrimary else AlertRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Lock Status
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isLocked) AlertRedContainer else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Lock Status",
                    tint = if (isLocked) AlertRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isLocked) "Locked" else "Active",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLocked) AlertRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * Procedural QR Code Canvas generator creating an authentic 2D matrix pattern
 */
@Composable
fun StylizedQRCodeCanvas(
    modifier: Modifier = Modifier,
    sizeDp: Int = 200
) {
    Card(
        modifier = modifier.size(sizeDp.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size((sizeDp - 32).dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val gridSize = 19
                val cellSize = canvasWidth / gridSize
                val primaryColor = Color(0xFF1E2522)
                val accentColor = Color(0xFF00A86B)

                // Corner finder pattern helper
                fun drawFinder(startX: Float, startY: Float) {
                    // Outer 7x7 box
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(startX, startY),
                        size = Size(cellSize * 7, cellSize * 7),
                        cornerRadius = CornerRadius(cellSize * 1.2f, cellSize * 1.2f)
                    )
                    // Inner 5x5 white cutout
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(startX + cellSize, startY + cellSize),
                        size = Size(cellSize * 5, cellSize * 5),
                        cornerRadius = CornerRadius(cellSize * 0.8f, cellSize * 0.8f)
                    )
                    // Center 3x3 solid box
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(startX + cellSize * 2, startY + cellSize * 2),
                        size = Size(cellSize * 3, cellSize * 3),
                        cornerRadius = CornerRadius(cellSize * 0.5f, cellSize * 0.5f)
                    )
                }

                // 3 Standard QR Finder patterns
                drawFinder(0f, 0f)
                drawFinder(canvasWidth - (cellSize * 7), 0f)
                drawFinder(0f, canvasHeight - (cellSize * 7))

                // Algorithmic data matrix cells (pseudo-hash pattern)
                for (r in 0 until gridSize) {
                    for (c in 0 until gridSize) {
                        // Skip finder pattern zones
                        val inTopLeft = r < 7 && c < 7
                        val inTopRight = r < 7 && c >= gridSize - 7
                        val inBottomLeft = r >= gridSize - 7 && c < 7
                        if (inTopLeft || inTopRight || inBottomLeft) continue

                        // Procedural QR cell hash
                        val hash = (r * 31 + c * 17 + (r xor c)) % 11
                        if (hash < 6) {
                            val isAccent = (r + c) % 9 == 0
                            drawRoundRect(
                                color = if (isAccent) accentColor else primaryColor,
                                topLeft = Offset(c * cellSize + (cellSize * 0.1f), r * cellSize + (cellSize * 0.1f)),
                                size = Size(cellSize * 0.8f, cellSize * 0.8f),
                                cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
