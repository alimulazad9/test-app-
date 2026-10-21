package com.example.model

enum class AppMode {
    PARENT,
    CHILD
}

enum class ParentTab {
    DASHBOARD,
    LIVE_SCREEN,
    ALERTS,
    APP_LIMITS,
    PAIRING
}

enum class RiskLevel {
    HIGH,
    MEDIUM,
    LOW
}

enum class KeywordCategory(val displayName: String) {
    BULLYING("Cyberbullying"),
    EXPLICIT("Adult Content"),
    SELF_HARM("Self-Harm"),
    PREDATION("Dangerous Meeting"),
    CUSTOM("Custom Keywords")
}

data class KeywordRule(
    val id: String,
    val phrase: String,
    val category: KeywordCategory,
    val isEnabled: Boolean = true
)

data class SecurityAlert(
    val id: String,
    val timestamp: Long,
    val appSource: String, // e.g., "WhatsApp", "Instagram", "SMS"
    val sender: String,
    val messageText: String,
    val matchedKeyword: String,
    val riskLevel: RiskLevel,
    val isDismissed: Boolean = false
)

data class AppUsageRule(
    val packageName: String,
    val appName: String,
    val category: String,
    val dailyLimitMinutes: Int,
    val usedMinutesToday: Int,
    val isBlocked: Boolean = false,
    val iconKey: String
)

data class ChildDeviceProfile(
    val deviceId: String = "DEV-AIR-9812",
    val childName: String = "Leo's Pixel 8",
    val modelName: String = "Google Pixel 8",
    val androidVersion: String = "Android 14 (API 34)",
    val batteryLevel: Int = 84,
    val isCharging: Boolean = false,
    val isOnline: Boolean = true,
    val isDeviceLocked: Boolean = false,
    val lockReason: String = "Remote lock active by Parent",
    val activeAppPackage: String = "com.mock.social",
    val activeAppName: String = "Social Messenger",
    val lastSyncTime: Long = System.currentTimeMillis()
)

data class LiveScreenStream(
    val isStreaming: Boolean = true,
    val fps: Int = 28,
    val latencyMs: Int = 22,
    val resolution: String = "1080 x 2400 (60Hz)",
    val protocol: String = "WebRTC (H.264 / P2P Relay)",
    val simulatedScreenSnapshot: String = "Active Screen"
)

data class PairingInfo(
    val pairingCode: String = "942-817-305",
    val qrToken: String = "airdroid://pair?token=942817305&expire=1800",
    val expiresInSeconds: Int = 1140,
    val isPaired: Boolean = true
)
