package com.emarsys.di

import com.emarsys.geofence.Geofence
import com.emarsys.testUtil.IntegrationTestUtils
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class EmarsysDependencyInjectionTest {

    @AfterEach
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