package com.emarsys.sample

import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.emarsys.core.api.EmarsysIdlingResources
import com.emarsys.sample.testutils.setupAndLogin
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.rule.BaristaRule
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class MobileEngageTests {
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
        IdlingRegistry.getInstance().unregister(EmarsysIdlingResources.countingIdlingResource)
    }

    @Test
    fun testInlineInAppIsVisible() {
        baristaRule.launchActivity()
        setupAndLogin(uiDevice)
        clickOn("Mobile engage")
        clickOn("INLINE IN-APP")
        uiDevice.wait(Until.hasObject(By.text("New features")), 10000)
        val text = uiDevice.findObject(By.text("New features")).text

        text shouldBe "New features"
    }
}