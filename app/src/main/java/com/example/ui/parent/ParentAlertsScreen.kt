package com.example.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeywordCategory
import com.example.model.KeywordRule
import com.example.model.RiskLevel
import com.example.model.SecurityAlert
import com.example.ui.components.RiskLevelChip
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAlertsScreen(
    securityAlerts: List<SecurityAlert>,
    keywordRules: List<KeywordRule>,
    onDismissAlert: (String) -> Unit,
    onClearAllAlerts: () -> Unit,
    onAddKeyword: (String, KeywordCategory) -> Unit,
    onToggleKeyword: (String) -> Unit,
    onDeleteKeyword: (String) -> Unit,
    onLockDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Alerts Feed, 1: Keyword Rules
    var selectedRiskFilter by remember { mutableStateOf<RiskLevel?>(null) }
    var showAddKeywordSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab row: Alerts Feed vs Keyword Dictionary
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Alerts Feed (${securityAlerts.count { !it.isDismissed }})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Keyword Rules (${keywordRules.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }

        if (selectedSection == 0) {
            // Alerts Feed Section
            val activeAlerts = securityAlerts.filter { !it.isDismissed }
            val filteredAlerts = if (selectedRiskFilter == null) {
                activeAlerts
            } else {
                activeAlerts.filter { it.riskLevel == selectedRiskFilter }
            }

            // Risk Level Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRiskFilter == null,
                            onClick = { selectedRiskFilter = null },
                            label = { Text("All (${activeAlerts.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRiskFilter == RiskLevel.HIGH,
                            onClick = { selectedRiskFilter = RiskLevel.HIGH },
                            label = { Text("High Risk") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRiskFilter == RiskLevel.MEDIUM,
                            onClick = { selectedRiskFilter = RiskLevel.MEDIUM },
                            label = { Text("Medium") }
                        )
                    }
                }

                if (activeAlerts.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearAllAlerts,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Clear All", fontSize = 12.sp)
                    }
                }
            }

            if (filteredAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Active Risk Alerts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "AirDroid Kids Notification Listener is actively monitoring incoming chats for flagged safety keywords.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredAlerts, key = { it.id }) { alert ->
                        AlertCardItem(
                            alert = alert,
                            onDismiss = { onDismissAlert(alert.id) },
                            onLock = onLockDevice
                        )
                    }
                }
            }
        } else {
            // Keyword Rules Management Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Keyword Dictionary",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Button(
                    onClick = { showAddKeywordSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("add_keyword_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Keyword")
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(keywordRules, key = { it.id }) { rule ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rule.phrase,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (rule.isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                                    )
                                )
                                Text(
                                    text = rule.category.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = rule.isEnabled,
                                    onCheckedChange = { onToggleKeyword(rule.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmeraldPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteKeyword(rule.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete keyword",
                                        tint = AlertRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Keyword Modal Bottom Sheet
    if (showAddKeywordSheet) {
        val sheetState = rememberModalBottomSheetState()
        var newKeywordText by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(KeywordCategory.BULLYING) }

        ModalBottomSheet(
            onDismissRequest = { showAddKeywordSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Detection Keyword",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = newKeywordText,
                    onValueChange = { newKeywordText = it },
                    label = { Text("Keyword or Phrase") },
                    placeholder = { Text("e.g. meet me, don't tell") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_keyword_input")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeywordCategory.values().forEach { category ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedCategory == category) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = category }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedCategory == category) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (selectedCategory == category) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (newKeywordText.isNotBlank()) {
                            onAddKeyword(newKeywordText, selectedCategory)
                            showAddKeywordSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_keyword_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    enabled = newKeywordText.isNotBlank()
                ) {
                    Text("Save Keyword Rule")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AlertCardItem(
    alert: SecurityAlert,
    onDismiss: () -> Unit,
    onLock: () -> Unit
) {
    val timeFormatted = remember(alert.timestamp) {
        SimpleDateFormat("h:mm a • MMM d", Locale.getDefault()).format(Date(alert.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_item_${alert.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: App source & Risk chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = alert.appSource,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "From: ${alert.sender}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                RiskLevelChip(riskLevel = alert.riskLevel)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message text with highlighted matched keyword
            val annotatedMessage = buildAnnotatedString {
                val fullText = alert.messageText
                val keyword = alert.matchedKeyword
                val startIndex = fullText.indexOf(keyword, ignoreCase = true)

                if (startIndex != -1) {
                    val endIndex = startIndex + keyword.length
                    append(fullText.substring(0, startIndex))
                    withStyle(
                        style = SpanStyle(
                            background = AlertRedContainer,
                            color = AlertRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append(fullText.substring(startIndex, endIndex))
                    }
                    append(fullText.substring(endIndex))
                } else {
                    append(fullText)
                }
            }

            Text(
                text = annotatedMessage,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom bar: Timestamp & Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Dismiss", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onLock,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lock Phone", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
