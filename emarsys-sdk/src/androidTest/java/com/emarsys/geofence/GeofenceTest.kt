package com.emarsys.geofence

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.emarsys.testUtil.IntegrationTestUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class GeofenceTest {

    private lateinit var geofenceProxy: GeofenceApi
    private lateinit var mockGeofenceInternal: GeofenceInternal


    @BeforeEach
    fun setUp() {
        mockGeofenceInternal = mock()
        val dependencyContainer = FakeDependencyContainer(geofenceInternal = mockGeofenceInternal)

        setupEmarsysComponent(dependencyContainer)

        geofenceProxy = Geofence()
    }

    @AfterEach
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_throughRunnerProxy() {
        geofenceProxy.enable()

        verify(mockGeofenceInternal).enable(null)
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_withCompletionListener() {
        val mockCompletionListener: CompletionListener = mock()
        geofenceProxy.enable(mockCompletionListener)

        verify(mockGeofenceInternal).enable(mockCompletionListener)
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_withLambda() {
        val mockCompletionListener: (Throwable?) -> Unit = mock()
        geofenceProxy.enable(mockCompletionListener)

        verify(mockGeofenceInternal).enable(any())
    }

    @Test
    fun testDisableDelegatesToGeofenceInternalMethod() {
        geofenceProxy.disable()

        verify(mockGeofenceInternal).disable()
    }

    @Test
    fun testSetEventHandler() {
        val eventHandler: EventHandler = mock()

        geofenceProxy.setEventHandler(eventHandler)

        verify(mockGeofenceInternal).setEventHandler(eventHandler)
    }

    @Test
    fun testIsEnabledDelegatesToGeofenceInternalMethod() {
        geofenceProxy.isEnabled()

        verify(mockGeofenceInternal).isEnabled()
    }

    @Test
    fun testRegisteredGeofencesDelegatesToInternal() {
        geofenceProxy.registeredGeofences

        verify(mockGeofenceInternal).registeredGeofences
    }
}