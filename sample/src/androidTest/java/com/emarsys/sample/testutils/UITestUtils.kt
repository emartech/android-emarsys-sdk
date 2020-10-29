package com.emarsys.sample.testutils

import android.os.Build
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.emarsys.sample.R
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions
import com.schibsted.spain.barista.interaction.BaristaClickInteractions
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions

fun setupAndLogin(uiDevice: UiDevice) {
    BaristaScrollInteractions.scrollTo(R.id.buttonSetupChange)
    uiDevice.wait(Until.hasObject(By.text("CHANGE")), 10000)

    BaristaEditTextInteractions.writeTo(R.id.newApplicationCode, "EMS11-C3FD3")
    BaristaEditTextInteractions.writeTo(R.id.newContactFieldId, "2575")
    BaristaClickInteractions.clickOn("CHANGE")

    BaristaScrollInteractions.scrollTo(R.id.loggedInContact)
    uiDevice.wait(Until.hasObject(By.textContains("Logged in as")), 10000)
    BaristaVisibilityAssertions.assertDisplayed(R.id.currentApplicationCode, "ApplicationCode: EMS11-C3FD3")
    BaristaVisibilityAssertions.assertDisplayed(R.id.currentContactFieldId, "ContactFieldId: 2575")

    BaristaScrollInteractions.scrollTo(R.id.newApplicationCode)
    BaristaEditTextInteractions.writeTo(R.id.contactId, resolveApiLevelToContact())

    BaristaClickInteractions.clickOn(R.id.buttonLogin)
    BaristaScrollInteractions.scrollTo(R.id.loggedInContact)
    uiDevice.wait(Until.hasObject(By.textContains("Logged in as")), 10000)
    BaristaVisibilityAssertions.assertDisplayed(R.id.loggedInContact, resolveApiLevelToContact())
}

fun resolveApiLevelToContact(): String {
    return when (Build.VERSION.SDK_INT) {
        21 -> "emarsys-sdk-e2e-test-1"
        22 -> "emarsys-sdk-e2e-test-2"
        23 -> "emarsys-sdk-e2e-test-3"
        24 -> "emarsys-sdk-e2e-test-4"
        25 -> "emarsys-sdk-e2e-test-5"
        26 -> "emarsys-sdk-e2e-test-6"
        27 -> "emarsys-sdk-e2e-test-7"
        28 -> "emarsys-sdk-e2e-test-8"
        29 -> "emarsys-sdk-e2e-test-9"
        30 -> "emarsys-sdk-e2e-test-10"
        else -> "emarsys-sdk-e2e-test-11"
    }
}