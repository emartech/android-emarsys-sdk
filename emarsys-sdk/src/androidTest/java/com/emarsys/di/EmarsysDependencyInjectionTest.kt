package com.emarsys.di

import com.emarsys.geofence.Geofence
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.IntegrationTestUtils
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class EmarsysDependencyInjectionTest : AnnotationSpec() {

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