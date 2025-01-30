package com.emarsys.di

import com.emarsys.geofence.Geofence
import com.emarsys.testUtil.IntegrationTestUtils
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.After
import org.junit.Test

class EmarsysDependencyInjectionTest  {

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testGeofence_whenGooglePlayIsNotAvailable() {
        val geofenceApi: Geofence = mockk(relaxed = true)
        val loggingGeofenceApi: Geofence = mockk(relaxed = true)

        val dependencyContainer = FakeDependencyContainer(
                geofence = geofenceApi,
                loggingGeofence = loggingGeofenceApi,
                isGooglePlayServiceAvailable = false)

        setupEmarsysComponent(dependencyContainer)

        EmarsysDependencyInjection.geofence() shouldBe loggingGeofenceApi
    }
}