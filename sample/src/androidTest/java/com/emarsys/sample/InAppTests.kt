package com.emarsys.sample

import android.content.Context
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.emarsys.Emarsys
import com.emarsys.core.api.EmarsysIdlingResources
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.sample.testutils.TimeoutUtils
import com.emarsys.sample.testutils.setupAndLogin
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import com.schibsted.spain.barista.rule.BaristaRule
import io.kotlintest.matchers.string.shouldContain
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch


class InAppTests {

    @get:Rule
    var baristaRule = BaristaRule.create(MainActivity::class.java)

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var uiDevice: UiDevice

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EmarsysIdlingResources.countingIdlingResource)
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @After
    fun unregisterIdlingResource() {
        uiDevice.drag(50, 400, 50, 50, 10)
        IdlingRegistry.getInstance().unregister(EmarsysIdlingResources.countingIdlingResource)
    }

    @Test
    fun testInApp() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        clickOn("Mobile engage")
        clickOn("Tracking")
        scrollTo(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        writeTo(R.id.eventName, "emarsys-sdk-e2e-test")
        clickOn(R.id.buttonTrackCustomEvent)

        uiDevice.waitForWindowUpdate("com.emarsys.sample", 10000)
        onWebView()
                .withElement(findElement(Locator.CLASS_NAME, "ems-inapp-button"))
                .perform(webClick())
    }

    @Test
    fun testInAppAppEvent() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)


        val latch = CountDownLatch(1)
        var text = ""
        Emarsys.inApp.setEventHandler(object : EventHandler {
            override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                text = eventName + " - " + payload.toString()
                latch.countDown()
            }
        })

        clickOn("Mobile engage")
        clickOn("Tracking")
        scrollTo(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        writeTo(R.id.eventName, "emarsys-sdk-app-event-e2e-test")
        clickOn(R.id.buttonTrackCustomEvent)

        uiDevice.waitForWindowUpdate("com.emarsys.sample", 10000)
        onWebView()
                .withElement(findElement(Locator.CLASS_NAME, "ems-inapp-button"))
                .perform(webClick())

        latch.await()
        text shouldContain "emarsys-sdk-app-event-e2e-test"
    }

    @Test
    fun testPushToInApp() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        val timestamp = System.currentTimeMillis().toString()
        val testCase = "Android - push to inapp"

        clickOn("Mobile engage")
        clickOn("Tracking")
        scrollTo(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        writeTo(R.id.eventName, "emarsys-sdk-push-e2e-test")
        writeTo(R.id.eventAttributes, """{"eventName":"$testCase","timestamp":"$timestamp"}""")

        clickOn(R.id.buttonTrackCustomEvent)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.text("Emarsys SDK Samples")), 10000)

        val defaultAction: UiObject2 = uiDevice.findObject(By.text("Android - push to inapp"))
        defaultAction.click()

        uiDevice.waitForWindowUpdate("com.emarsys.sample", 10000)
        onWebView()
                .withElement(findElement(Locator.CLASS_NAME, "ems-inapp-button"))
                .perform(webClick())
    }
}