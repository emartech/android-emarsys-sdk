package com.emarsys.geofence


import com.emarsys.core.api.result.CompletionListener
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.testUtil.IntegrationTestUtils
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class GeofenceTest  {

    private lateinit var geofenceProxy: GeofenceApi
    private lateinit var mockGeofenceInternal: GeofenceInternal


    @Before
    fun setUp() {
        mockGeofenceInternal = mockk(relaxed = true)
        val dependencyContainer = FakeDependencyContainer(geofenceInternal = mockGeofenceInternal)

        setupEmarsysComponent(dependencyContainer)

        geofenceProxy = Geofence()
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_throughRunnerProxy() {
        geofenceProxy.enable()

        verify { mockGeofenceInternal.enable(null) }
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_withCompletionListener() {
        val mockCompletionListener: CompletionListener = mockk(relaxed = true)
        geofenceProxy.enable(mockCompletionListener)

        verify { mockGeofenceInternal.enable(mockCompletionListener) }
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_withLambda() {
        val mockCompletionListener: (Throwable?) -> Unit = mockk(relaxed = true)
        geofenceProxy.enable(mockCompletionListener)

        verify { mockGeofenceInternal.enable(any()) }
    }

    @Test
    fun testDisableDelegatesToGeofenceInternalMethod() {
        geofenceProxy.disable()

        verify { mockGeofenceInternal.disable() }
    }

    @Test
    fun testSetEventHandler() {
        val eventHandler: EventHandler = mockk(relaxed = true)

        geofenceProxy.setEventHandler(eventHandler)

        verify { mockGeofenceInternal.setEventHandler(eventHandler) }
    }

    @Test
    fun testIsEnabledDelegatesToGeofenceInternalMethod() {
        geofenceProxy.isEnabled()

        verify { mockGeofenceInternal.isEnabled() }
    }

    @Test
    fun testRegisteredGeofencesDelegatesToInternal() {
        geofenceProxy.registeredGeofences

        verify { mockGeofenceInternal.registeredGeofences }
    }
}