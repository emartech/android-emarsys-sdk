package com.emarsys.geofence

import com.emarsys.core.RunnerProxy
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.geofence.GeofenceInternal
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

class GeofenceProxyTest {

    private lateinit var geofenceProxy: GeofenceApi
    private lateinit var mockGeofenceInternal: GeofenceInternal
    private lateinit var spyRunnerProxy: RunnerProxy

    @Before
    fun setUp() {
        mockGeofenceInternal = mock()
        spyRunnerProxy = spy()
        geofenceProxy = GeofenceProxy(mockGeofenceInternal, spyRunnerProxy)
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_throughRunnerProxy() {
        geofenceProxy.enable()

        verify(spyRunnerProxy).logException(any())
        verify(mockGeofenceInternal).enable(null)
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_withCompletionListener() {
        val mockCompletionListener: CompletionListener = mock()
        geofenceProxy.enable(mockCompletionListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockGeofenceInternal).enable(mockCompletionListener)
    }

    @Test
    fun testEnableDelegatesToGeofenceInternalMethod_withLambda() {
        val mockCompletionListener: (Throwable?) -> Unit = mock()
        geofenceProxy.enable(mockCompletionListener)

        verify(spyRunnerProxy).logException(any())
        verify(mockGeofenceInternal).enable(any())
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
}