package com.emarsys.mobileengage.geofence

import android.app.Activity
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

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
        fetchGeofencesAction = FetchGeofencesAction(mockGeofenceInternal)
    }

    @Test
    fun testExecute_callsDefaultGeofenceInternalsFetchGeofences() {
        fetchGeofencesAction.execute(mockActivity)

        verify(mockGeofenceInternal).fetchGeofences()
    }
}