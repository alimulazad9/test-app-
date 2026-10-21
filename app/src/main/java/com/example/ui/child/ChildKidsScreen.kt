package com.example.ui.child

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppUsageRule
import com.example.model.ChildDeviceProfile
import com.example.model.PairingInfo
import com.example.model.SecurityAlert
import com.example.ui.components.PulsingLiveDot
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import kotlinx.coroutines.launch
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.AppOpenResult

@Composable
fun ChildKidsScreen(
    childProfile: ChildDeviceProfile,
    appRules: List<AppUsageRule>,
    pairingInfo: PairingInfo,
    childScreenKey: String,
    onAttemptOpenApp: (String) -> AppOpenResult,
    onNavigateHome: () -> Unit,
    onSendMockMessage: (String, String, String) -> SecurityAlert?,
    onPairWithCode: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var renderServerInput by remember { mutableStateOf(com.example.network.CloudSyncManager.renderServerUrl) }
    var firebaseDbInput by remember { mutableStateOf(com.example.network.CloudSyncManager.firebaseDbUrl) }
    var isBackgroundServiceRunning by remember { mutableStateOf(true) }
    var syncStatusMessage by remember { mutableStateOf<String?>("Connected to Render cloud server") }
    var isSyncing by remember { mutableStateOf(false) }

    var blockedNotice by remember { mutableStateOf<String?>(null) }
    var notificationHeadsUp by remember { mutableStateOf<Pair<String, String>?>(null) }
    var customMsgText by remember { mutableStateOf("") }
    var pairInputCode by remember { mutableStateOf("") }

    // If Parent locked entire device
    if (childProfile.isDeviceLocked) {
        DeviceLockedCurtain(
            lockReason = childProfile.lockReason
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dedicated Child Phone Mode Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SignalBlue.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SignalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CHILD PHONE CLIENT (AirDroid Kids)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SignalBlue,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary
                ) {
                    Text(
                        text = "BACKGROUND ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Render Server & Firebase Realtime Database Configuration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cloud_server_settings_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Render & Firebase Cloud Relay",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Relays live telemetry & alerts to Parent Web and Firebase",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = renderServerInput,
                    onValueChange = {
                        renderServerInput = it
                        com.example.network.CloudSyncManager.renderServerUrl = it
                    },
                    label = { Text("Render Server URL (Web Dashboard)") },
                    placeholder = { Text("https://your-service.onrender.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("render_url_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = firebaseDbInput,
                    onValueChange = {
                        firebaseDbInput = it
                        com.example.network.CloudSyncManager.firebaseDbUrl = it
                    },
                    label = { Text("Firebase Realtime Database URL (Optional)") },
                    placeholder = { Text("https://project-id.firebaseio.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_db_url_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isSyncing = true
                            coroutineScope.launch {
                                val success = com.example.network.CloudSyncManager.syncTelemetryToServer(
                                    deviceId = childProfile.deviceId,
                                    battery = childProfile.batteryLevel,
                                    isCharging = childProfile.isCharging,
                                    isLocked = childProfile.isDeviceLocked,
                                    activeApp = childProfile.activeAppName,
                                    activePackage = childProfile.activeAppPackage
                                )
                                isSyncing = false
                                syncStatusMessage = if (success) {
                                    "Synced to Render & Firebase successfully at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())}"
                                } else {
                                    "Render server offline or waiting for deployment"
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sync_now_button"),
                        enabled = !isSyncing
                    ) {
                        Text(if (isSyncing) "Syncing..." else "Sync Cloud Now", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (isBackgroundServiceRunning) {
                                com.example.service.KidsBackgroundService.stopService(context)
                                isBackgroundServiceRunning = false
                            } else {
                                com.example.service.KidsBackgroundService.startService(context)
                                isBackgroundServiceRunning = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBackgroundServiceRunning) "Stop Service" else "Start Service",
                            fontSize = 12.sp
                        )
                    }
                }

                syncStatusMessage?.let { status ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (status.contains("successfully", ignoreCase = true) || status.contains("Connected", ignoreCase = true)) EmeraldPrimary else AlertRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
        // Foreground Service Status Card (AirDroid Kids MDM)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kids_foreground_service_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SignalBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = SignalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AirDroid Kids Protection",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "High-priority persistent foreground service",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PulsingLiveDot(color = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RUNNING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Privilege status pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PrivilegeChip(name = "Device Admin", active = true, modifier = Modifier.weight(1f))
                    PrivilegeChip(name = "Notifications", active = true, modifier = Modifier.weight(1f))
                    PrivilegeChip(name = "Accessibility", active = true, modifier = Modifier.weight(1f))
                }
            }
        }

        // Heads-up simulated incoming notification toast
        AnimatedVisibility(
            visible = notificationHeadsUp != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            notificationHeadsUp?.let { (title, body) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2622)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                ),
                                maxLines = 2
                            )
                        }
                        IconButton(onClick = { notificationHeadsUp = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Simulated Child Phone Environment Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("child_simulated_device_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F1A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with back navigation if inside an app
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHILD PHONE LAUNCHER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )

                    if (childScreenKey != "HOME") {
                        FilledTonalButton(
                            onClick = onNavigateHome,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Phone Home", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phone Display Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                            )
                        )
                        .padding(16.dp)
                ) {
                    if (childScreenKey == "HOME") {
                        // Home screen app grid
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Digital clock
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "10:42",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Light
                                    )
                                )
                                Text(
                                    text = "Wednesday, October 21",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            }

                            // App Icons Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ChildAppLauncherIcon(
                                    name = "ChatApp",
                                    icon = Icons.Default.Chat,
                                    bgColor = Color(0xFF0288D1),
                                    badge = "60m Limit",
                                    onClick = {
                                        handleAppLaunch(
                                            packageName = "com.mock.social",
                                            onAttemptOpenApp = onAttemptOpenApp,
                                            onBlocked = { blockedNotice = it }
                                        )
                                    }
                                )

                                ChildAppLauncherIcon(
                                    name = "TikVid",
                                    icon = Icons.Default.SmartDisplay,
                                    bgColor = Color(0xFF7C4DFF),
                                    badge = "40/45m",
                                    onClick = {
                                        handleAppLaunch(
                                            packageName = "com.mock.tiktok",
                                            onAttemptOpenApp = onAttemptOpenApp,
                                            onBlocked = { blockedNotice = it }
                                        )
                                    }
                                )

                                ChildAppLauncherIcon(
                                    name = "BlockCraft",
                                    icon = Icons.Default.Gamepad,
                                    bgColor = EmeraldPrimary,
                                    badge = "Game",
                                    onClick = {
                                        handleAppLaunch(
                                            packageName = "com.mock.game",
                                            onAttemptOpenApp = onAttemptOpenApp,
                                            onBlocked = { blockedNotice = it }
                                        )
                                    }
                                )

                                ChildAppLauncherIcon(
                                    name = "GhostChat",
                                    icon = Icons.Default.Block,
                                    bgColor = AlertRed,
                                    badge = "Blocked",
                                    onClick = {
                                        handleAppLaunch(
                                            packageName = "com.mock.secretchat",
                                            onAttemptOpenApp = onAttemptOpenApp,
                                            onBlocked = { blockedNotice = it }
                                        )
                                    }
                                )
                            }
                        }
                    } else {
                        // Running Child App screen view
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when (childScreenKey) {
                                    "com.mock.tiktok" -> Icons.Default.SmartDisplay
                                    "com.mock.game" -> Icons.Default.Gamepad
                                    "com.mock.social" -> Icons.Default.Chat
                                    else -> Icons.Default.Public
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Running: ${childProfile.activeAppName}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Parent can view this in Live Screen Mirroring via WebRTC.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onNavigateHome,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Exit to Phone Home", color = Color.White)
                            }
                        }
                    }

                    // Blocking overlay if child triggered a blocked/limit-exceeded app
                    if (blockedNotice != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.92f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(46.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "AirDroid Kids Enforcement",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = AlertRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = blockedNotice ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { blockedNotice = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Back to Home")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notification Listener & Keyword Interceptor Test Bench
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notification_simulator_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Notification Listener & Keyword Tester",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Simulate incoming chats to test keyword detection",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "One-Tap Preset Scenarios:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetMessageButton(
                        label = "Risk: Bullying (\"you are a loser\")",
                        app = "ChatApp Social",
                        sender = "@school_bully",
                        message = "You are such a loser, nobody in class likes you",
                        riskColor = AlertRed,
                        onClick = {
                            val alert = onSendMockMessage("ChatApp Social", "@school_bully", "You are such a loser, nobody in class likes you")
                            notificationHeadsUp = Pair("ChatApp: @school_bully", "You are such a loser, nobody in class likes you")
                        }
                    )

                    PresetMessageButton(
                        label = "Risk: Dangerous Meeting (\"don't tell parents\")",
                        app = "WhatsApp",
                        sender = "@stranger_24",
                        message = "Hey don't tell parents and meet alone at the abandoned park tonight",
                        riskColor = AlertRed,
                        onClick = {
                            val alert = onSendMockMessage("WhatsApp", "@stranger_24", "Hey don't tell parents and meet alone at the abandoned park tonight")
                            notificationHeadsUp = Pair("WhatsApp: @stranger_24", "Hey don't tell parents and meet alone at the abandoned park tonight")
                        }
                    )

                    PresetMessageButton(
                        label = "Safe Message (No Keyword Match)",
                        app = "SMS Messages",
                        sender = "Mom",
                        message = "Hi Leo, your dinner is on the kitchen table!",
                        riskColor = EmeraldPrimary,
                        onClick = {
                            onSendMockMessage("SMS Messages", "Mom", "Hi Leo, your dinner is on the kitchen table!")
                            notificationHeadsUp = Pair("SMS: Mom", "Hi Leo, your dinner is on the kitchen table!")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom message input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customMsgText,
                        onValueChange = { customMsgText = it },
                        placeholder = { Text("Type custom message (e.g. bully, secret)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_test_message_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (customMsgText.isNotBlank()) {
                                onSendMockMessage("Direct Chat", "@tester", customMsgText)
                                notificationHeadsUp = Pair("Direct Chat: @tester", customMsgText)
                                customMsgText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        enabled = customMsgText.isNotBlank(),
                        modifier = Modifier.testTag("send_custom_message_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

private fun handleAppLaunch(
    packageName: String,
    onAttemptOpenApp: (String) -> AppOpenResult,
    onBlocked: (String) -> Unit
) {
    when (val result = onAttemptOpenApp(packageName)) {
        is AppOpenResult.Allowed -> {
            // App opens
        }
        is AppOpenResult.BlockedByParent -> {
            onBlocked("Access Blocked: Your parent has blocked ${result.appName} using AirDroid Parental Control.")
        }
        is AppOpenResult.UsageLimitReached -> {
            onBlocked("Time's Up: You have reached the daily limit of ${result.limit} minutes for ${result.appName}.")
        }
        is AppOpenResult.DeviceLocked -> {
            onBlocked("Device Locked: ${result.reason}")
        }
    }
}

@Composable
fun ChildAppLauncherIcon(
    name: String,
    icon: ImageVector,
    bgColor: Color,
    badge: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("child_app_icon_${name.lowercase()}")
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bgColor,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
fun PrivilegeChip(
    name: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (active) EmeraldPrimary.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (active) EmeraldPrimary else Color.Gray,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (active) EmeraldPrimary else Color.Gray,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun PresetMessageButton(
    label: String,
    app: String,
    sender: String,
    message: String,
    riskColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                )
                Text(
                    text = "\"$message\"",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Trigger test",
                tint = EmeraldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DeviceLockedCurtain(lockReason: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1412))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = AlertRed.copy(alpha = 0.2f),
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PHONE IS LOCKED",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = lockReason,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "AirDroid Kids Device Policy Enforcement Active",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Emergency dialer placeholder */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Call Only")
            }
        }
    }
}
