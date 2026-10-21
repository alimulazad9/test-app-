package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ScreenShare
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppMode
import com.example.model.ParentTab
import com.example.model.SecurityAlert
import com.example.ui.child.ChildKidsScreen
import com.example.ui.components.ModeSwitcherHeader
import com.example.ui.components.PulsingLiveDot
import com.example.ui.parent.ParentAlertsScreen
import com.example.ui.parent.ParentAppLimitsScreen
import com.example.ui.parent.ParentDashboardScreen
import com.example.ui.parent.ParentLiveScreenView
import com.example.ui.parent.ParentPairingScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SignalBlue
import com.example.viewmodel.ParentalControlViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            com.example.service.KidsBackgroundService.startService(this)
        } catch (_: Exception) {}
        setContent {
            MyApplicationTheme {
                AirDroidMainApp()
            }
        }
    }
}

@Composable
fun AirDroidMainApp(viewModel: ParentalControlViewModel = viewModel()) {
    val appMode by viewModel.appMode.collectAsState()
    val parentTab by viewModel.parentTab.collectAsState()
    val childProfile by viewModel.childProfile.collectAsState()
    val appRules by viewModel.appRules.collectAsState()
    val securityAlerts by viewModel.securityAlerts.collectAsState()
    val keywordRules by viewModel.keywordRules.collectAsState()
    val liveStream by viewModel.liveStream.collectAsState()
    val pairingInfo by viewModel.pairingInfo.collectAsState()
    val childScreenKey by viewModel.childScreenKey.collectAsState()
    val parentBanner by viewModel.parentAlertBanner.collectAsState()

    val unreadAlertsCount = securityAlerts.count { !it.isDismissed }

    // System Back navigation handler
    BackHandler(enabled = appMode == AppMode.PARENT && parentTab != ParentTab.DASHBOARD) {
        viewModel.setParentTab(ParentTab.DASHBOARD)
    }

    BackHandler(enabled = appMode == AppMode.CHILD && childScreenKey != "HOME") {
        viewModel.navigateChildHome()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ModeSwitcherHeader(
                    currentMode = appMode,
                    onModeChange = { viewModel.setAppMode(it) }
                )
            }
        },
        bottomBar = {
            if (appMode == AppMode.PARENT) {
                ParentBottomNavBar(
                    currentTab = parentTab,
                    unreadAlertsCount = unreadAlertsCount,
                    onSelectTab = { viewModel.setParentTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content depending on mode & tab
            when (appMode) {
                AppMode.PARENT -> {
                    when (parentTab) {
                        ParentTab.DASHBOARD -> {
                            ParentDashboardScreen(
                                childProfile = childProfile,
                                appRules = appRules,
                                securityAlerts = securityAlerts,
                                onNavigateTab = { viewModel.setParentTab(it) },
                                onToggleLock = { viewModel.toggleDeviceLock() }
                            )
                        }
                        ParentTab.LIVE_SCREEN -> {
                            ParentLiveScreenView(
                                childProfile = childProfile,
                                liveStream = liveStream,
                                childScreenKey = childScreenKey,
                                onToggleStreaming = { viewModel.toggleLiveStream() },
                                onToggleLock = { viewModel.toggleDeviceLock() }
                            )
                        }
                        ParentTab.ALERTS -> {
                            ParentAlertsScreen(
                                securityAlerts = securityAlerts,
                                keywordRules = keywordRules,
                                onDismissAlert = { viewModel.dismissAlert(it) },
                                onClearAllAlerts = { viewModel.clearAllAlerts() },
                                onAddKeyword = { phrase, cat -> viewModel.addKeywordRule(phrase, cat) },
                                onToggleKeyword = { viewModel.toggleKeywordRule(it) },
                                onDeleteKeyword = { viewModel.deleteKeywordRule(it) },
                                onLockDevice = { viewModel.setDeviceLocked(true, "Locked due to safety risk alert") }
                            )
                        }
                        ParentTab.APP_LIMITS -> {
                            ParentAppLimitsScreen(
                                appRules = appRules,
                                onToggleBlock = { viewModel.toggleAppBlock(it) },
                                onUpdateLimit = { pkg, limit -> viewModel.updateAppLimit(pkg, limit) },
                                onAddUsage = { pkg, mins -> viewModel.addAppUsage(pkg, mins) },
                                onResetUsage = { pkg -> viewModel.resetAppUsage(pkg) }
                            )
                        }
                        ParentTab.PAIRING -> {
                            ParentPairingScreen(
                                pairingInfo = pairingInfo,
                                childName = childProfile.childName,
                                onRegenerateCode = { viewModel.regeneratePairingCode() },
                                onUnpair = { viewModel.unpairDevice() }
                            )
                        }
                    }
                }
                AppMode.CHILD -> {
                    ChildKidsScreen(
                        childProfile = childProfile,
                        appRules = appRules,
                        pairingInfo = pairingInfo,
                        childScreenKey = childScreenKey,
                        onAttemptOpenApp = { viewModel.attemptOpenChildApp(it) },
                        onNavigateHome = { viewModel.navigateChildHome() },
                        onSendMockMessage = { source, sender, text ->
                            viewModel.sendSimulatedMessage(source, sender, text)
                        },
                        onPairWithCode = { viewModel.pairWithCode(it) }
                    )
                }
            }

            // Real-time Push Alert Banner on Parent when Child triggers risky content
            AnimatedVisibility(
                visible = parentBanner != null && appMode == AppMode.PARENT,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                parentBanner?.let { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setParentTab(ParentTab.ALERTS)
                                viewModel.clearParentBanner()
                            }
                            .testTag("push_alert_popup"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AlertRed),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PulsingLiveDot(color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AIRDROID RISK ALERT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Detected \"${alert.matchedKeyword}\" in ${alert.appSource}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            IconButton(onClick = { viewModel.clearParentBanner() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentBottomNavBar(
    currentTab: ParentTab,
    unreadAlertsCount: Int,
    onSelectTab: (ParentTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("parent_bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentTab == ParentTab.DASHBOARD,
            onClick = { onSelectTab(ParentTab.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (currentTab == ParentTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_dashboard")
        )

        NavigationBarItem(
            selected = currentTab == ParentTab.LIVE_SCREEN,
            onClick = { onSelectTab(ParentTab.LIVE_SCREEN) },
            icon = {
                Icon(
                    imageVector = if (currentTab == ParentTab.LIVE_SCREEN) Icons.Filled.ScreenShare else Icons.Outlined.ScreenShare,
                    contentDescription = "Live Mirror"
                )
            },
            label = { Text("Live Mirror", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SignalBlue,
                selectedTextColor = SignalBlue,
                indicatorColor = SignalBlue.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_live_screen")
        )

        NavigationBarItem(
            selected = currentTab == ParentTab.ALERTS,
            onClick = { onSelectTab(ParentTab.ALERTS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadAlertsCount > 0) {
                            Badge(containerColor = AlertRed) {
                                Text("$unreadAlertsCount", color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == ParentTab.ALERTS) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                        contentDescription = "Alerts"
                    )
                }
            },
            label = { Text("Alerts", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AlertRed,
                selectedTextColor = AlertRed,
                indicatorColor = AlertRedContainer
            ),
            modifier = Modifier.testTag("nav_item_alerts")
        )

        NavigationBarItem(
            selected = currentTab == ParentTab.APP_LIMITS,
            onClick = { onSelectTab(ParentTab.APP_LIMITS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == ParentTab.APP_LIMITS) Icons.Filled.HourglassTop else Icons.Outlined.HourglassTop,
                    contentDescription = "Limits"
                )
            },
            label = { Text("Limits", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_limits")
        )

        NavigationBarItem(
            selected = currentTab == ParentTab.PAIRING,
            onClick = { onSelectTab(ParentTab.PAIRING) },
            icon = {
                Icon(
                    imageVector = if (currentTab == ParentTab.PAIRING) Icons.Filled.QrCode else Icons.Outlined.QrCode,
                    contentDescription = "Pairing"
                )
            },
            label = { Text("Pairing", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_item_pairing")
        )
    }
}
