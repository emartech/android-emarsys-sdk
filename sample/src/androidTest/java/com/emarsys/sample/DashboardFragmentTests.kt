package com.emarsys.sample

import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.emarsys.core.api.EmarsysIdlingResources
import com.emarsys.sample.testutils.resolveApiLevelToContact
import com.emarsys.sample.testutils.setupAndLogin
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import com.schibsted.spain.barista.rule.BaristaRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class DashboardFragmentTests {
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
        uiDevice.drag(50, 400, 50, 50, 10)
        IdlingRegistry.getInstance().unregister(EmarsysIdlingResources.countingIdlingResource)
    }

    @Test
    fun testSetupSDKAndLoginWithValidContact() {
        baristaRule.launchActivity()
        setupAndLogin(uiDevice)
    }

    @Test
    fun testLogout() {
        baristaRule.launchActivity()

        scrollTo(R.id.buttonSetupChange)
        writeTo(R.id.newApplicationCode, "EMS11-C3FD3")
        writeTo(R.id.contactFieldId, "2575")
        clickOn("CHANGE")

        scrollTo(R.id.dashboardCard)
        assertDisplayed(R.id.currentApplicationCode, "ApplicationCode: EMS11-C3FD3")

        scrollTo(R.id.newApplicationCode)
        writeTo(R.id.contactId, resolveApiLevelToContact())

        clickOn(R.id.buttonLogin)
        scrollTo(R.id.loggedInContact)
        assertDisplayed(R.id.loggedInContact, resolveApiLevelToContact())

        scrollTo(R.id.newApplicationCode)
        clickOn(R.id.buttonLogout)
        scrollTo(R.id.loggedInContact)
        assertDisplayed(R.id.loggedInContact, "Anonymous")
    }

    @Test
    fun testSetupSDKWithInvalidApplicationCode() {
        baristaRule.launchActivity()

        scrollTo(R.id.buttonSetupChange)
        writeTo(R.id.newApplicationCode, "EMS-C3FD3")
        writeTo(R.id.contactFieldId, "2575")
        clickOn("CHANGE")

        scrollTo(R.id.dashboardCard)
        assertDisplayed(R.id.currentApplicationCode, "ApplicationCode: not set")

        scrollTo(R.id.newApplicationCode)
        writeTo(R.id.contactId, resolveApiLevelToContact())

        clickOn(R.id.buttonLogin)
        scrollTo(R.id.loggedInContact)
        assertDisplayed(R.id.loggedInContact, "Anonymous")
    }
}