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
import androidx.test.uiautomator.Until
import com.emarsys.Emarsys
import com.emarsys.core.api.EmarsysIdlingResources
import com.emarsys.inapp.ui.InlineInAppView
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.sample.testutils.TimeoutUtils
import com.emarsys.sample.testutils.setupAndLogin
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions
import com.schibsted.spain.barista.rule.BaristaRule
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class InlineInAppTests {

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
        IdlingRegistry.getInstance().unregister(EmarsysIdlingResources.countingIdlingResource)
    }

    @Test
    fun testInlineInAppIsVisible() {
        baristaRule.launchActivity()
        setupAndLogin(uiDevice)
        clickOn("Mobile engage")
        clickOn("INLINE IN-APP")
        uiDevice.wait(Until.hasObject(By.text("This is an Inline InApp Message")), 10000)
        val text = uiDevice.findObject(By.text("This is an Inline InApp Message")).text

        text shouldBe "This is an Inline InApp Message"
    }

    @Test
    fun testInlineInApp() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        clickOn("Mobile engage")
        clickOn("INLINE IN-APP")

        val activity = baristaRule.activityTestRule.activity
        uiDevice.waitForWindowUpdate("com.emarsys.sample", 3000)
        activity.runOnUiThread {
            activity.findViewById<InlineInAppView>(R.id.inlineInAppFullyFromXml).removeAllViews()
            activity.findViewById<InlineInAppView>(R.id.inlineInAppFromXmlAndCode).removeAllViews()
        }

        writeTo(R.id.textInputLayoutViewId, "ia")
        clickOn("SHOW")
        uiDevice.waitForWindowUpdate("com.emarsys.sample", 2000)

        onWebView()
                .withElement(findElement(Locator.CLASS_NAME, "close"))
                .perform(webClick())
    }

    @Test
    fun testInlineInAppAppEvent() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)

        clickOn("Mobile engage")
        clickOn("INLINE IN-APP")

        val activity = baristaRule.activityTestRule.activity
        activity.runOnUiThread {
            activity.findViewById<InlineInAppView>(R.id.inlineInAppFromXmlAndCode).removeAllViews()

        }
        uiDevice.waitForWindowUpdate("com.emarsys.sample", 1000)

        onWebView()
                .withElement(findElement(Locator.CLASS_NAME, "ems-inapp-button"))
                .perform(webClick())
        uiDevice.wait(Until.hasObject(By.textContains("AppEvent - DeepLink")), 10000)
        val result = uiDevice.findObject(By.textContains("AppEvent - DeepLink")).text

        result shouldBe """AppEvent - DeepLink, {"name":"DeepLink","payload":{"url":"appEvent"},"id":"2"}"""
    }

    @Test
    fun testInlineInAppCustomEvent() {
        baristaRule.launchActivity()
        clickOn("Dashboard")
        setupAndLogin(uiDevice)
        clickOn("Mobile engage")
        clickOn("Tracking")
        BaristaScrollInteractions.scrollTo(R.id.geofenceTitle)
        uiDevice.wait(Until.hasObject(By.text("TRACK PUSH TOKEN")), 10000)
        clickOn(R.id.buttonTrackPushToken)
        clickOn(R.id.buttonTrackPushToken)

        clickOn("Mobile engage")
        clickOn("INLINE IN-APP")

        val activity = baristaRule.activityTestRule.activity
        activity.runOnUiThread {
            activity.findViewById<InlineInAppView>(R.id.inlineInAppFullyFromXml).removeAllViews()

        }
        uiDevice.waitForWindowUpdate("com.emarsys.sample", 1000)

        onWebView()
                .withElement(findElement(Locator.CLASS_NAME, "ems-inapp-button"))
                .perform(webClick())
        val latch = CountDownLatch(1)
        var text = ""
        Emarsys.push.setSilentMessageEventHandler(object : EventHandler {
            override fun handleEvent(context: Context, eventName: String, payload: JSONObject?) {
                text = eventName + " - " + payload.toString()
                latch.countDown()
            }
        })

        latch.await(2000, TimeUnit.MILLISECONDS)

        text shouldContain "inline-in-app"
    }
}