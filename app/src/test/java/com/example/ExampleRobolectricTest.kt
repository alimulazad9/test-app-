package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ParentalControlRepository
import com.example.model.KeywordCategory
import com.example.model.RiskLevel
import com.example.viewmodel.AppOpenResult
import com.example.viewmodel.ParentalControlViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @org.junit.Before
    fun setup() {
        ParentalControlRepository.setDeviceLocked(false)
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AirDroid Control", appName)
    }

    @Test
    fun `test keyword detection catches high-risk predatory phrases`() {
        val repo = ParentalControlRepository
        val alert = repo.simulateIncomingNotification(
            appSource = "WhatsApp",
            sender = "@anonymous",
            messageText = "Hey don't tell parents about this"
        )
        assertNotNull(alert)
        assertEquals("don't tell parents", alert?.matchedKeyword)
        assertEquals(RiskLevel.HIGH, alert?.riskLevel)
    }

    @Test
    fun `test keyword detection ignores safe messages`() {
        val repo = ParentalControlRepository
        val alert = repo.simulateIncomingNotification(
            appSource = "SMS",
            sender = "Mom",
            messageText = "Remember your math textbook today"
        )
        assertNull(alert)
    }

    @Test
    fun `test app limits enforcement triggers block when limit reached`() {
        val vm = ParentalControlViewModel()
        // ChatApp Social is configured with 60m used out of 60m daily limit
        val result = vm.attemptOpenChildApp("com.mock.social")
        assertTrue(result is AppOpenResult.UsageLimitReached)
    }

    @Test
    fun `test device lock blocks all child applications`() {
        val vm = ParentalControlViewModel()
        vm.setDeviceLocked(true, "Parent manual lock")
        val result = vm.attemptOpenChildApp("com.mock.game")
        assertTrue(result is AppOpenResult.DeviceLocked)
    }

    @Test
    fun `test pairing code validation`() {
        val repo = ParentalControlRepository
        repo.regeneratePairingCode()
        val currentCode = repo.pairingInfo.value.pairingCode
        val pairSuccess = repo.pairWithCode(currentCode)
        assertTrue(pairSuccess)
        assertTrue(repo.pairingInfo.value.isPaired)
    }
}
