package com.emarsys.sample

import android.content.Context
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.emarsys.Emarsys
import com.emarsys.core.api.EmarsysIdlingResources
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.sample.testutils.setupAndLogin
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import com.schibsted.spain.barista.rule.BaristaRule
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldMatch
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.regex.Pattern


class PushTests {
    @get:Rule
    var baristaRule = BaristaRule.create(MainActivity::class.java)

    private lateinit var uiDevice: UiDevice

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EmarsysIdlingResources.countingIdlingResource)

        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @After
    fun unregisterIdlingResource() {
        uiDevice.drag(50, 500, 50, 200, 10)
        IdlingRegistry.getInstance().unregister(EmarsysIdlingResources.countingIdlingResource)
    }

    @Test
    fun testSimplePush() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        clickOn("Mobile engage")
        clickOn("Tracking")
        scrollTo(R.id.geofenceTitle)
        uiDevice.wait(Until.hasObject(By.text("TRACK PUSH TOKEN")), 10000)
        clickOn(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        val timestamp = System.currentTimeMillis().toString()
        val testCase = "Android - simple push"
        writeTo(R.id.eventName, "emarsys-sdk-push-e2e-test")
        writeTo(R.id.eventAttributes, """{"eventName":"$testCase","timestamp":"$timestamp"}""")

        clickOn(R.id.buttonTrackCustomEvent)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Emarsys SDK Samples")), 10000)
        val title = uiDevice.findObject(By.text(testCase)).text
        val text = uiDevice.findObject(By.text(timestamp)).text

        title shouldBe testCase
        text shouldBe timestamp
    }

    @Test
    fun testOpenExternalPush() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        clickOn("Mobile engage")
        clickOn("Tracking")
        scrollTo(R.id.geofenceTitle)
        uiDevice.wait(Until.hasObject(By.text("TRACK PUSH TOKEN")), 10000)
        clickOn(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        val timestamp = System.currentTimeMillis().toString()
        val testCase = "Android - open external push"
        writeTo(R.id.eventName, "emarsys-sdk-push-e2e-test")
        writeTo(R.id.eventAttributes, """{"eventName":"$testCase","timestamp":"$timestamp"}""")

        clickOn(R.id.buttonTrackCustomEvent)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Emarsys SDK Samples")), 10000)

        val openExternalUrlButton: UiObject2 = uiDevice.findObject(By.text("EXTERNAL_URL"))
        openExternalUrlButton.click()

        val welcomeTextRegex = "(Welcome to Chrome)|(Example Domain)"
        uiDevice.wait(Until.hasObject(By.text(Pattern.compile(welcomeTextRegex))), 10000)
        val webPageTitle = uiDevice.findObject(By.text(Pattern.compile(welcomeTextRegex))).text
        uiDevice.pressHome()

        webPageTitle shouldMatch Regex(welcomeTextRegex)
    }

    @Test
    fun testCustomEventPush() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        clickOn("Mobile engage")
        clickOn("Tracking")
        scrollTo(R.id.geofenceTitle)
        uiDevice.wait(Until.hasObject(By.text("TRACK PUSH TOKEN")), 10000)
        clickOn(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        val timestamp = System.currentTimeMillis().toString()
        val testCase = "Android - custom event push"
        writeTo(R.id.eventName, "emarsys-sdk-push-e2e-test")
        writeTo(R.id.eventAttributes, """{"eventName":"$testCase","timestamp":"$timestamp"}""")

        clickOn(R.id.buttonTrackCustomEvent)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Emarsys SDK Samples")), 10000)

        val customEventButton: UiObject2 = uiDevice.findObject(By.text("CUSTOM_EVENT"))
        customEventButton.click()

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("customEvent")), 10000)
        val customEventTriggerTitle = "customEvent"
        val title = uiDevice.findObject(By.text(customEventTriggerTitle)).text
        val text = uiDevice.findObject(By.text(timestamp)).text

        val appEventButton: UiObject2 = uiDevice.findObject(By.text("APP_EVENT"))
        appEventButton.click()

        title shouldBe customEventTriggerTitle
        text shouldBe timestamp
    }

    @Test
    fun testAppEventPush() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        val timestamp = System.currentTimeMillis().toString()
        val testCase = "Android - app event push"

        clickOn("Mobile engage")
        clickOn("Tracking")
        scrollTo(R.id.geofenceTitle)
        uiDevice.wait(Until.hasObject(By.text("TRACK PUSH TOKEN")), 10000)
        clickOn(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        val latch = CountDownLatch(1)
        var text = ""

        Emarsys.push.setNotificationEventHandler(object : EventHandler {
            override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                text = eventName + " - " + payload.toString()
                latch.countDown()
            }
        })

        writeTo(R.id.eventName, "emarsys-sdk-push-e2e-test")
        writeTo(R.id.eventAttributes, """{"eventName":"$testCase","timestamp":"$timestamp"}""")

        clickOn(R.id.buttonTrackCustomEvent)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Emarsys SDK Samples")), 10000)

        val appEventButton: UiObject2 = uiDevice.findObject(By.text("APP_EVENT"))
        appEventButton.click()
        latch.await()
        text shouldContain timestamp
    }

    @Test
    fun testSilentAppEventPush() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        val timestamp = System.currentTimeMillis().toString()
        val testCase = "Android - silent app event push"

        clickOn("Mobile engage")
        clickOn("Tracking")
        scrollTo(R.id.geofenceTitle)
        uiDevice.wait(Until.hasObject(By.text("TRACK PUSH TOKEN")), 10000)
        clickOn(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        val latch = CountDownLatch(1)
        var text = ""
        Emarsys.push.setSilentMessageEventHandler(object : EventHandler {
            override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                text = eventName + " - " + payload.toString()
                latch.countDown()
            }
        })
        writeTo(R.id.eventName, "emarsys-sdk-push-e2e-test")
        writeTo(R.id.eventAttributes, """{"eventName":"$testCase","timestamp":"$timestamp"}""")

        clickOn(R.id.buttonTrackCustomEvent)
        latch.await()

        text shouldContain timestamp
    }
}