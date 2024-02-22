package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class FetchGeofencesActionTest {



    private lateinit var mockGeofenceInternal: GeofenceInternal
    private lateinit var mockActivity: Activity
    private lateinit var fetchGeofencesAction: FetchGeofencesAction

    @BeforeEach
    fun setUp() {
        mockGeofenceInternal = mock()
        mockActivity = mock()

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        fetchGeofencesAction = FetchGeofencesAction(mockGeofenceInternal)
    }

    @AfterEach
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