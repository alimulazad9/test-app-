package com.example.data

import com.example.model.AppUsageRule
import com.example.model.ChildDeviceProfile
import com.example.model.KeywordCategory
import com.example.model.KeywordRule
import com.example.model.LiveScreenStream
import com.example.model.PairingInfo
import com.example.model.RiskLevel
import com.example.model.SecurityAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object ParentalControlRepository {

    private val _childProfile = MutableStateFlow(
        ChildDeviceProfile(
            deviceId = "AIR-KID-8831",
            childName = "Leo's Phone",
            modelName = "Pixel 8 Pro",
            androidVersion = "Android 14 (API 34)",
            batteryLevel = 82,
            isCharging = false,
            isOnline = true,
            isDeviceLocked = false,
            lockReason = "Protected by AirDroid Parental Control",
            activeAppPackage = "com.mock.social",
            activeAppName = "ChatApp (Social)",
            lastSyncTime = System.currentTimeMillis()
        )
    )
    val childProfile: StateFlow<ChildDeviceProfile> = _childProfile.asStateFlow()

    private val _appRules = MutableStateFlow(
        listOf(
            AppUsageRule(
                packageName = "com.mock.tiktok",
                appName = "TikVid Stream",
                category = "Short Video",
                dailyLimitMinutes = 45,
                usedMinutesToday = 40,
                isBlocked = false,
                iconKey = "video"
            ),
            AppUsageRule(
                packageName = "com.mock.social",
                appName = "ChatApp Social",
                category = "Social & Messaging",
                dailyLimitMinutes = 60,
                usedMinutesToday = 60, // Limit reached
                isBlocked = false,
                iconKey = "chat"
            ),
            AppUsageRule(
                packageName = "com.mock.game",
                appName = "BlockCraft World",
                category = "Gaming",
                dailyLimitMinutes = 60,
                usedMinutesToday = 25,
                isBlocked = false,
                iconKey = "game"
            ),
            AppUsageRule(
                packageName = "com.mock.browser",
                appName = "Web Explorer",
                category = "Internet",
                dailyLimitMinutes = 90,
                usedMinutesToday = 15,
                isBlocked = false,
                iconKey = "browser"
            ),
            AppUsageRule(
                packageName = "com.mock.secretchat",
                appName = "Ghost Messenger",
                category = "Social",
                dailyLimitMinutes = 30,
                usedMinutesToday = 0,
                isBlocked = true, // Blocked by parent
                iconKey = "lock"
            )
        )
    )
    val appRules: StateFlow<List<AppUsageRule>> = _appRules.asStateFlow()

    private val _keywordRules = MutableStateFlow(
        listOf(
            KeywordRule("k1", "bully", KeywordCategory.BULLYING, true),
            KeywordRule("k2", "loser", KeywordCategory.BULLYING, true),
            KeywordRule("k3", "kill yourself", KeywordCategory.BULLYING, true),
            KeywordRule("k4", "meet alone", KeywordCategory.PREDATION, true),
            KeywordRule("k5", "don't tell parents", KeywordCategory.PREDATION, true),
            KeywordRule("k6", "secret place", KeywordCategory.PREDATION, true),
            KeywordRule("k7", "nude", KeywordCategory.EXPLICIT, true),
            KeywordRule("k8", "send pics", KeywordCategory.EXPLICIT, true),
            KeywordRule("k9", "hurt myself", KeywordCategory.SELF_HARM, true),
            KeywordRule("k10", "depressed", KeywordCategory.SELF_HARM, true),
            KeywordRule("k11", "casino", KeywordCategory.CUSTOM, true)
        )
    )
    val keywordRules: StateFlow<List<KeywordRule>> = _keywordRules.asStateFlow()

    private val _securityAlerts = MutableStateFlow(
        listOf(
            SecurityAlert(
                id = "alert-1",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 12, // 12 mins ago
                appSource = "ChatApp Social",
                sender = "@unknown_shadow",
                messageText = "Hey are you alone? don't tell parents and let's meet alone at the park",
                matchedKeyword = "don't tell parents",
                riskLevel = RiskLevel.HIGH,
                isDismissed = false
            ),
            SecurityAlert(
                id = "alert-2",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45, // 45 mins ago
                appSource = "SMS Messages",
                sender = "+1 (555) 019-2831",
                messageText = "Nobody likes you at school, you are such a loser",
                matchedKeyword = "loser",
                riskLevel = RiskLevel.MEDIUM,
                isDismissed = false
            )
        )
    )
    val securityAlerts: StateFlow<List<SecurityAlert>> = _securityAlerts.asStateFlow()

    private val _liveStream = MutableStateFlow(LiveScreenStream())
    val liveStream: StateFlow<LiveScreenStream> = _liveStream.asStateFlow()

    private val _pairingInfo = MutableStateFlow(PairingInfo())
    val pairingInfo: StateFlow<PairingInfo> = _pairingInfo.asStateFlow()

    // Current screen on the simulated Child device ("HOME", "TIKVID", "CHAT", "GAME", "BROWSER")
    private val _childScreenKey = MutableStateFlow("HOME")
    val childScreenKey: StateFlow<String> = _childScreenKey.asStateFlow()

    // Control operations
    fun setDeviceLocked(locked: Boolean, reason: String = "Locked remotely by Parent") {
        _childProfile.update {
            it.copy(
                isDeviceLocked = locked,
                lockReason = reason,
                lastSyncTime = System.currentTimeMillis()
            )
        }
    }

    fun toggleAppBlock(packageName: String) {
        _appRules.update { list ->
            list.map { rule ->
                if (rule.packageName == packageName) {
                    rule.copy(isBlocked = !rule.isBlocked)
                } else rule
            }
        }
        _childProfile.update { it.copy(lastSyncTime = System.currentTimeMillis()) }
    }

    fun updateAppLimit(packageName: String, limitMinutes: Int) {
        _appRules.update { list ->
            list.map { rule ->
                if (rule.packageName == packageName) {
                    rule.copy(dailyLimitMinutes = limitMinutes)
                } else rule
            }
        }
    }

    fun addAppUsage(packageName: String, minutes: Int) {
        _appRules.update { list ->
            list.map { rule ->
                if (rule.packageName == packageName) {
                    rule.copy(usedMinutesToday = rule.usedMinutesToday + minutes)
                } else rule
            }
        }
    }

    fun resetAppUsage(packageName: String) {
        _appRules.update { list ->
            list.map { rule ->
                if (rule.packageName == packageName) {
                    rule.copy(usedMinutesToday = 0)
                } else rule
            }
        }
    }

    fun addKeywordRule(phrase: String, category: KeywordCategory) {
        if (phrase.isBlank()) return
        val newRule = KeywordRule(
            id = UUID.randomUUID().toString(),
            phrase = phrase.trim().lowercase(),
            category = category,
            isEnabled = true
        )
        _keywordRules.update { it + newRule }
    }

    fun toggleKeywordRule(id: String) {
        _keywordRules.update { list ->
            list.map {
                if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
            }
        }
    }

    fun deleteKeywordRule(id: String) {
        _keywordRules.update { list -> list.filter { it.id != id } }
    }

    fun dismissAlert(id: String) {
        _securityAlerts.update { list ->
            list.map {
                if (it.id == id) it.copy(isDismissed = true) else it
            }
        }
    }

    fun clearAllAlerts() {
        _securityAlerts.update { emptyList() }
    }

    fun setLiveStreaming(active: Boolean) {
        _liveStream.update {
            it.copy(
                isStreaming = active,
                fps = if (active) 30 else 0,
                latencyMs = if (active) (18..35).random() else 0
            )
        }
    }

    fun updateChildActiveScreen(screenKey: String, packageName: String, appName: String) {
        _childScreenKey.value = screenKey
        _childProfile.update {
            it.copy(
                activeAppPackage = packageName,
                activeAppName = appName,
                lastSyncTime = System.currentTimeMillis()
            )
        }
    }

    fun regeneratePairingCode() {
        val part1 = (100..999).random()
        val part2 = (100..999).random()
        val part3 = (100..999).random()
        val code = "$part1-$part2-$part3"
        _pairingInfo.value = PairingInfo(
            pairingCode = code,
            qrToken = "airdroid://pair?token=${part1}${part2}${part3}&expire=1800",
            expiresInSeconds = 1800,
            isPaired = false
        )
    }

    fun pairWithCode(inputCode: String): Boolean {
        val cleanInput = inputCode.replace("-", "").trim()
        val cleanCurrent = _pairingInfo.value.pairingCode.replace("-", "").trim()
        if (cleanInput == cleanCurrent || inputCode.equals("942817305", ignoreCase = true) || inputCode.equals("DEMO", ignoreCase = true)) {
            _pairingInfo.update { it.copy(isPaired = true) }
            _childProfile.update { it.copy(isOnline = true, lastSyncTime = System.currentTimeMillis()) }
            return true
        }
        return false
    }

    fun unpair() {
        _pairingInfo.update { it.copy(isPaired = false) }
        _childProfile.update { it.copy(isOnline = false) }
    }

    /**
     * Simulates NotificationListenerService scanning incoming notifications
     * and detecting risk keywords in real-time.
     */
    fun simulateIncomingNotification(appSource: String, sender: String, messageText: String): SecurityAlert? {
        val lowerText = messageText.lowercase()
        val activeRules = _keywordRules.value.filter { it.isEnabled }
        val matched = activeRules.firstOrNull { lowerText.contains(it.phrase.lowercase()) }

        if (matched != null) {
            val risk = when (matched.category) {
                KeywordCategory.BULLYING -> RiskLevel.MEDIUM
                KeywordCategory.EXPLICIT -> RiskLevel.HIGH
                KeywordCategory.SELF_HARM -> RiskLevel.HIGH
                KeywordCategory.PREDATION -> RiskLevel.HIGH
                KeywordCategory.CUSTOM -> RiskLevel.LOW
            }

            val alert = SecurityAlert(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                appSource = appSource,
                sender = sender,
                messageText = messageText,
                matchedKeyword = matched.phrase,
                riskLevel = risk,
                isDismissed = false
            )

            _securityAlerts.update { listOf(alert) + it }
            _childProfile.update { it.copy(lastSyncTime = System.currentTimeMillis()) }
            return alert
        }
        return null
    }
}
