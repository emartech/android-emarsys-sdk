package com.emarsys.di

import com.emarsys.geofence.Geofence
import com.emarsys.testUtil.IntegrationTestUtils
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.mock

class EmarsysDependencyInjectionTest {

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testGeofence_whenGooglePlayIsNotAvailable() {
        val geofenceApi: Geofence = mock()
        val loggingGeofenceApi: Geofence = mock()

        val dependencyContainer = FakeDependencyContainer(
                geofence = geofenceApi,
                loggingGeofence = loggingGeofenceApi,
                isGooglePlayServiceAvailable = false)

        setupEmarsysComponent(dependencyContainer)

        EmarsysDependencyInjection.geofence() shouldBe loggingGeofenceApi
    }
}