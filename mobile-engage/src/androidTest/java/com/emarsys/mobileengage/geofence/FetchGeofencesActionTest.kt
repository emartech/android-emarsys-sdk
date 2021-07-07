package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import com.emarsys.testUtil.TimeoutUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class FetchGeofencesActionTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var mockGeofenceInternal: GeofenceInternal
    private lateinit var mockActivity: Activity
    private lateinit var fetchGeofencesAction: FetchGeofencesAction

    @Before
    fun setUp() {
        mockGeofenceInternal = mock()
        mockActivity = mock()

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        fetchGeofencesAction = FetchGeofencesAction(mockGeofenceInternal)
    }

    @After
    fun tearDown() {
        tearDownMobileEngageComponent()
    }


    @Test
    fun testExecute_callsDefaultGeofenceInternalsFetchGeofences() {
        fetchGeofencesAction.execute(mockActivity)
        waitForTask()

        verify(mockGeofenceInternal).fetchGeofences(null)
    }


}