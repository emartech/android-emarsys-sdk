package com.emarsys

import android.Manifest
import android.app.Application
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import androidx.test.rule.GrantPermissionRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.di.emarsys
import com.emarsys.mobileengage.api.geofence.Trigger
import com.emarsys.mobileengage.api.geofence.TriggerType
import com.emarsys.mobileengage.api.inbox.Message
import com.emarsys.testUtil.*
import com.emarsys.testUtil.E2ETestUtils.retry
import com.emarsys.testUtil.rules.ConnectionRule
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import com.emarsys.testUtil.rules.RetryRule
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.After
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.emarsys.mobileengage.api.geofence.Geofence as MEGeofence


class EmarsysE2ETests {

    companion object {
        private const val OLD_APPLICATION_CODE = "14C19-A121F"
        private const val APPLICATION_CODE = "EMS11-C3FD3"
        private const val CONTACT_FIELD_ID = 2575
        private const val CONTACT_FIELD_VALUE = "test@test.com"
        private const val TRIGGER_INBOX_EVENT = "emarsys-sdk-e2e-inbox-test"
        private const val TEST_TAG = "test_tag"
    }

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.longTimeoutRule

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    @Rule
    @JvmField
    val connectionRule = ConnectionRule(application)

    @After
    fun tearDown() {
        E2ETestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testChangeApplicationCode() {
        setup(OLD_APPLICATION_CODE)

        changeApplicationCode(APPLICATION_CODE)

        setContact()

        val title = "Android - changeApplicationCode"
        val timestamp = System.currentTimeMillis()

        trackCustomEvent(title, timestamp)
        retry(10, 2000) {
            val message = fetchMessage(title, timestamp)
            message shouldNotBe null
        }
    }

    @Test
    fun testChangeApplicationCodeFromNull() {
        setup(null)

        changeApplicationCode(APPLICATION_CODE)

        setContact()

        val title = "Android - enable ME feature"
        val timestamp = System.currentTimeMillis()
        trackCustomEvent(title, timestamp)

        retry(10, 2000) {
            val message = fetchMessage(title, timestamp)
            message shouldNotBe null
        }
    }

    @Test
    fun testChangeApplicationCodeToNull() {
        var disabled = true
        setup(APPLICATION_CODE)

        changeApplicationCode(null)

        Emarsys.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE) { disabled = false }

        val title = "Android - disable ME feature"
        val timestamp = System.currentTimeMillis()
        Emarsys.trackCustomEvent(
            TRIGGER_INBOX_EVENT, mapOf(
                "eventName" to title,
                "timestamp" to timestamp.toString()
            )
        ) { disabled = false }

        Emarsys.messageInbox.fetchMessages { disabled = false }

        disabled shouldBe true
    }

    @Test
    fun testInbox_addTag_removeTag() {
        setup(APPLICATION_CODE)

        setContact()

        val title = "Android - test Inbox tags"
        val timestamp = System.currentTimeMillis()
        trackCustomEvent(title, timestamp)
        var message: Message? = null
        retry(10, 2000) {
            message = fetchMessage(title, timestamp)
            message shouldNotBe null
        }
        val messageToTest = message
        if (messageToTest != null) {
            val addMessageTagLatch = CountDownLatch(1)

            Emarsys.messageInbox.addTag(TEST_TAG, messageToTest.id) {
                addMessageTagLatch.countDown()
            }
            addMessageTagLatch.await()

            retry(10, 2000) {
                val updatedMessage = fetchMessage(title, timestamp)
                updatedMessage shouldNotBe null
                if (updatedMessage != null) {
                    updatedMessage.tags?.contains(TEST_TAG) shouldBe true
                }
            }
            val removeMessageTagLatch = CountDownLatch(1)
            Emarsys.messageInbox.removeTag(TEST_TAG, messageToTest.id) {
                removeMessageTagLatch.countDown()
            }
            removeMessageTagLatch.await()
            retry(10, 2000) {
                val updatedMessage = fetchMessage(title, timestamp)
                updatedMessage shouldNotBe null
                updatedMessage?.tags?.contains(TEST_TAG) shouldBe false
            }
        }
    }

    @Test
    @Ignore("Test is too flaky to run on pipeline")
    fun testGeofence() {
        setup(APPLICATION_CODE)
        retry {
            val fusedLocationProviderClient = emarsys().fusedLocationProviderClient
            fusedLocationProviderClient.setMockMode(true)
            val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
                longitude = 30.0
                latitude = 30.0
                accuracy = 30.0f
                altitude = 100.0
                time = System.currentTimeMillis()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                }
            }
            fusedLocationProviderClient.setMockLocation(mockLocation)
            Emarsys.geofence.enable()
            Emarsys.geofence.setInitialEnterTriggerEnabled(true)


            val latch = CountDownLatch(1)
            emarsys().coreSdkHandler.post {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { currentLocation ->
                    val testAction = JSONObject(
                        mapOf<String, Any?>(
                            "id" to "geofenceActionId",
                            "type" to "MEAppEvent",
                            "name" to "geofence",
                            "payload" to mapOf(
                                "name" to "Home",
                                "trigger_type" to "enter"
                            )
                        )
                    )
                    val geofence = MEGeofence(
                        "testGeofence", currentLocation.latitude, currentLocation.longitude,
                        400.0, 0.0, listOf(Trigger("testAction", TriggerType.ENTER, 0, testAction))
                    )

                    val geofenceInternal = emarsys().geofenceInternal
                    ReflectionTestUtils.setInstanceField(
                        geofenceInternal,
                        "nearestGeofences",
                        listOf(geofence)
                    )
                    geofenceInternal.registerGeofences(listOf(geofence))
                }

            }
            Emarsys.geofence.setEventHandler { _, name, payload ->
                name shouldBe "geofence"
                (payload?.get("name") ?: "") shouldBe "Home"
                (payload?.get("trigger_type") ?: "") shouldBe "enter"

                latch.countDown()
            }
            latch.await(3, TimeUnit.SECONDS)
        }
    }

    private fun changeApplicationCode(appCode: String?) {
        val changeApplicationCodeLatch = CountDownLatch(1)
        Emarsys.config.changeApplicationCode(appCode) {
            changeApplicationCodeLatch.countDown()
        }

        changeApplicationCodeLatch.await()
    }

    private fun trackCustomEvent(title: String, timestamp: Long) {
        val trackCustomEventLatch = CountDownLatch(1)
        Emarsys.trackCustomEvent(
            TRIGGER_INBOX_EVENT, mapOf(
                "eventName" to title,
                "timestamp" to timestamp.toString()
            )
        ) {
            trackCustomEventLatch.countDown()
        }

        trackCustomEventLatch.await()
    }

    private fun setContact() {
        val setContactLatch = CountDownLatch(1)
        Emarsys.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE) {
            setContactLatch.countDown()
        }

        setContactLatch.await()
    }

    private fun setup(appCode: String?) {
        Emarsys.setup(
            EmarsysConfig.Builder()
                .application(application)
                .applicationCode(appCode)
                .build()
        )
    }

    private fun fetchMessage(title: String, timestamp: Long, timeout: Long = 1000): Message? {
        var message: Message? = null
        Thread.sleep(1000)
        val fetchMessageInboxLatch = CountDownLatch(1)

        Emarsys.messageInbox.fetchMessages { response ->
            message = response.result
                ?.messages
                ?.firstOrNull { message -> message.title == title && message.body == timestamp.toString() }
            fetchMessageInboxLatch.countDown()
        }

        fetchMessageInboxLatch.await(timeout, TimeUnit.MILLISECONDS)
        return message
    }
}