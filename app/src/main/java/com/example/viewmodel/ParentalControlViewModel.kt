package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ParentalControlRepository
import com.example.model.AppMode
import com.example.model.AppUsageRule
import com.example.model.ChildDeviceProfile
import com.example.model.KeywordCategory
import com.example.model.KeywordRule
import com.example.model.LiveScreenStream
import com.example.model.PairingInfo
import com.example.model.ParentTab
import com.example.model.SecurityAlert
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AppOpenResult {
    object Allowed : AppOpenResult()
    data class BlockedByParent(val appName: String) : AppOpenResult()
    data class UsageLimitReached(val appName: String, val limit: Int, val used: Int) : AppOpenResult()
    data class DeviceLocked(val reason: String) : AppOpenResult()
}

class ParentalControlViewModel : ViewModel() {

    private val repository = ParentalControlRepository

    private val _appMode = MutableStateFlow(AppMode.PARENT)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _parentTab = MutableStateFlow(ParentTab.DASHBOARD)
    val parentTab: StateFlow<ParentTab> = _parentTab.asStateFlow()

    val childProfile: StateFlow<ChildDeviceProfile> = repository.childProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, ChildDeviceProfile())

    val appRules: StateFlow<List<AppUsageRule>> = repository.appRules
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val keywordRules: StateFlow<List<KeywordRule>> = repository.keywordRules
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val securityAlerts: StateFlow<List<SecurityAlert>> = repository.securityAlerts
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val liveStream: StateFlow<LiveScreenStream> = repository.liveStream
        .stateIn(viewModelScope, SharingStarted.Eagerly, LiveScreenStream())

    val pairingInfo: StateFlow<PairingInfo> = repository.pairingInfo
        .stateIn(viewModelScope, SharingStarted.Eagerly, PairingInfo())

    val childScreenKey: StateFlow<String> = repository.childScreenKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, "HOME")

    // Flash notification message banner on Parent when Child triggers an alert
    private val _parentAlertBanner = MutableStateFlow<SecurityAlert?>(null)
    val parentAlertBanner: StateFlow<SecurityAlert?> = _parentAlertBanner.asStateFlow()

    init {
        // Heartbeat simulator for streaming latency & telemetry
        viewModelScope.launch {
            while (true) {
                delay(3000)
                if (liveStream.value.isStreaming) {
                    repository.setLiveStreaming(true)
                }
            }
        }
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun setParentTab(tab: ParentTab) {
        _parentTab.value = tab
    }

    fun toggleDeviceLock() {
        val currentLocked = childProfile.value.isDeviceLocked
        repository.setDeviceLocked(!currentLocked, "Remote Lock Activated by AirDroid Parent")
    }

    fun setDeviceLocked(locked: Boolean, reason: String = "Remote Lock Activated by AirDroid Parent") {
        repository.setDeviceLocked(locked, reason)
    }

    fun toggleAppBlock(packageName: String) {
        repository.toggleAppBlock(packageName)
    }

    fun updateAppLimit(packageName: String, limitMinutes: Int) {
        repository.updateAppLimit(packageName, limitMinutes)
    }

    fun addAppUsage(packageName: String, minutes: Int) {
        repository.addAppUsage(packageName, minutes)
    }

    fun resetAppUsage(packageName: String) {
        repository.resetAppUsage(packageName)
    }

    fun addKeywordRule(phrase: String, category: KeywordCategory) {
        repository.addKeywordRule(phrase, category)
    }

    fun toggleKeywordRule(id: String) {
        repository.toggleKeywordRule(id)
    }

    fun deleteKeywordRule(id: String) {
        repository.deleteKeywordRule(id)
    }

    fun dismissAlert(id: String) {
        repository.dismissAlert(id)
    }

    fun clearAllAlerts() {
        repository.clearAllAlerts()
    }

    fun clearParentBanner() {
        _parentAlertBanner.value = null
    }

    fun toggleLiveStream() {
        val active = !liveStream.value.isStreaming
        repository.setLiveStreaming(active)
    }

    fun regeneratePairingCode() {
        repository.regeneratePairingCode()
    }

    fun pairWithCode(code: String): Boolean {
        return repository.pairWithCode(code)
    }

    fun unpairDevice() {
        repository.unpair()
    }

    /**
     * Checks if Child can launch an app according to rules enforced by AirDroid Kids
     */
    fun attemptOpenChildApp(packageName: String): AppOpenResult {
        if (childProfile.value.isDeviceLocked) {
            return AppOpenResult.DeviceLocked(childProfile.value.lockReason)
        }

        val rule = appRules.value.find { it.packageName == packageName }
        if (rule != null) {
            if (rule.isBlocked) {
                return AppOpenResult.BlockedByParent(rule.appName)
            }
            if (rule.usedMinutesToday >= rule.dailyLimitMinutes) {
                return AppOpenResult.UsageLimitReached(
                    appName = rule.appName,
                    limit = rule.dailyLimitMinutes,
                    used = rule.usedMinutesToday
                )
            }
            repository.updateChildActiveScreen(
                screenKey = rule.packageName,
                packageName = rule.packageName,
                appName = rule.appName
            )
            return AppOpenResult.Allowed
        }

        repository.updateChildActiveScreen("APP", packageName, "App")
        return AppOpenResult.Allowed
    }

    fun navigateChildHome() {
        repository.updateChildActiveScreen("HOME", "com.mock.launcher", "Home Screen")
    }

    /**
     * Simulates child receiving a message, scanning it with NotificationListenerService
     */
    fun sendSimulatedMessage(appSource: String, sender: String, messageText: String): SecurityAlert? {
        val alert = repository.simulateIncomingNotification(appSource, sender, messageText)
        if (alert != null) {
            _parentAlertBanner.value = alert
        }
        return alert
    }
}
