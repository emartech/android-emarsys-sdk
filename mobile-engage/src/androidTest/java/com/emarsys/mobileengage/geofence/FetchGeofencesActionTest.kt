package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask


import com.emarsys.testUtil.AnnotationSpec
import io.mockk.mockk
import io.mockk.verify

class FetchGeofencesActionTest : AnnotationSpec() {


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