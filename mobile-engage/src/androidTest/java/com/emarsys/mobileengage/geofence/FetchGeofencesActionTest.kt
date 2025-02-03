package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class FetchGeofencesActionTest  {


    private lateinit var mockGeofenceInternal: GeofenceInternal
    private lateinit var mockActivity: Activity
    private lateinit var fetchGeofencesAction: FetchGeofencesAction

    @Before
    fun setUp() {
        mockGeofenceInternal = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)

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

        verify { mockGeofenceInternal.fetchGeofences(null) }
    }


}