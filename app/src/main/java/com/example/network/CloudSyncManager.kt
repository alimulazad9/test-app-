package com.example.network

import android.util.Log
import com.example.data.ParentalControlRepository
import com.example.model.SecurityAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CloudSyncManager {
    private const val TAG = "CloudSyncManager"

    // Default or user-configured Render server URL
    var renderServerUrl: String = "https://airdroid-control-server.onrender.com"
    var firebaseDbUrl: String = "" // Optional: https://<project>.firebaseio.com

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    suspend fun syncTelemetryToServer(
        deviceId: String,
        battery: Int,
        isCharging: Boolean,
        isLocked: Boolean,
        activeApp: String,
        activePackage: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("deviceId", deviceId)
                put("battery", battery)
                put("isCharging", isCharging)
                put("isLocked", isLocked)
                put("activeApp", activeApp)
                put("activePackage", activePackage)
                put("childName", "Leo's Phone")
                put("modelName", "Google Pixel 8")
                put("timestamp", System.currentTimeMillis())
            }

            // 1. Send to Render Server
            val cleanUrl = renderServerUrl.trimEnd('/')
            val request = Request.Builder()
                .url("$cleanUrl/api/child/telemetry")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    handleServerCommands(bodyStr)
                }
            }

            // 2. Direct Firebase Realtime Database REST sync (if configured)
            if (firebaseDbUrl.isNotBlank()) {
                val cleanFb = firebaseDbUrl.trimEnd('/')
                val fbRequest = Request.Builder()
                    .url("$cleanFb/devices/$deviceId.json")
                    .put(json.toString().toRequestBody(JSON_MEDIA))
                    .build()
                client.newCall(fbRequest).execute().close()
            }

            true
        } catch (e: Exception) {
            Log.w(TAG, "Render server sync error: ${e.message}")
            false
        }
    }

    suspend fun sendSecurityAlertToServer(alert: SecurityAlert): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", alert.id)
                put("timestamp", alert.timestamp)
                put("appSource", alert.appSource)
                put("sender", alert.sender)
                put("messageText", alert.messageText)
                put("matchedKeyword", alert.matchedKeyword)
                put("riskLevel", alert.riskLevel.name)
                put("isDismissed", alert.isDismissed)
            }

            val cleanUrl = renderServerUrl.trimEnd('/')
            val request = Request.Builder()
                .url("$cleanUrl/api/child/alert")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            client.newCall(request).execute().close()

            if (firebaseDbUrl.isNotBlank()) {
                val cleanFb = firebaseDbUrl.trimEnd('/')
                val fbRequest = Request.Builder()
                    .url("$cleanFb/alerts/${alert.id}.json")
                    .put(json.toString().toRequestBody(JSON_MEDIA))
                    .build()
                client.newCall(fbRequest).execute().close()
            }

            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send alert to server: ${e.message}")
            false
        }
    }

    private fun handleServerCommands(responseJsonStr: String) {
        try {
            val resObj = JSONObject(responseJsonStr)
            
            // Check if server set lock status
            if (resObj.has("isLocked")) {
                val serverLocked = resObj.getBoolean("isLocked")
                if (serverLocked != ParentalControlRepository.childProfile.value.isDeviceLocked) {
                    ParentalControlRepository.setDeviceLocked(serverLocked, "Remote Lock Activated from Parent Web Dashboard on Render")
                }
            }

            // Execute any queued pending commands
            if (resObj.has("pendingCommands")) {
                val commandsArr = resObj.getJSONArray("pendingCommands")
                for (i in 0 until commandsArr.length()) {
                    val cmd = commandsArr.getJSONObject(i)
                    val action = cmd.optString("action")
                    when (action) {
                        "LOCK_DEVICE" -> {
                            ParentalControlRepository.setDeviceLocked(true, "Locked remotely via Render Web Dashboard")
                        }
                        "UNLOCK_DEVICE" -> {
                            ParentalControlRepository.setDeviceLocked(false)
                        }
                        "BLOCK_APP" -> {
                            val params = cmd.optJSONObject("params")
                            val pkg = params?.optString("packageName")
                            if (!pkg.isNullOrBlank()) {
                                ParentalControlRepository.toggleAppBlock(pkg)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing server commands: ${e.message}")
        }
    }
}
