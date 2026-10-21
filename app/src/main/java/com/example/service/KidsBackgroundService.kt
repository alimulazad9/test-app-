package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ParentalControlRepository
import com.example.network.CloudSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KidsBackgroundService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var telemetryJob: Job? = null
    private var alertWatcherJob: Job? = null

    companion object {
        const val CHANNEL_ID = "airdroid_kids_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, KidsBackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, KidsBackgroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildOngoingNotification("Active Protection Running"))

        startTelemetryLoop()
        startAlertWatcher()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun startTelemetryLoop() {
        telemetryJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val batteryLevel = getBatteryLevel()
                    val isCharging = isDeviceCharging()
                    val profile = ParentalControlRepository.childProfile.value

                    // Sync to Render server / Firebase Realtime Database
                    CloudSyncManager.syncTelemetryToServer(
                        deviceId = profile.deviceId,
                        battery = batteryLevel,
                        isCharging = isCharging,
                        isLocked = profile.isDeviceLocked,
                        activeApp = profile.activeAppName,
                        activePackage = profile.activeAppPackage
                    )
                } catch (_: Exception) {
                }
                delay(10000) // 10 second telemetry heartbeat
            }
        }
    }

    private fun startAlertWatcher() {
        alertWatcherJob = serviceScope.launch {
            ParentalControlRepository.securityAlerts.collectLatest { alerts ->
                val unDismissed = alerts.filter { !it.isDismissed }
                if (unDismissed.isNotEmpty()) {
                    val latest = unDismissed.first()
                    // Immediately push newly intercepted alert to Render server
                    CloudSyncManager.sendSecurityAlertToServer(latest)
                }
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale)
        } else {
            ParentalControlRepository.childProfile.value.batteryLevel
        }
    }

    private fun isDeviceCharging(): Boolean {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AirDroid Kids Protection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing persistent service ensuring parental safety, MDM rules, and telemetry relay."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildOngoingNotification(statusText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AirDroid Kids Protection")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }
}
