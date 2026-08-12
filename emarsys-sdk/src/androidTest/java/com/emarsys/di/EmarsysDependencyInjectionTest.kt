package com.emarsys.di

import android.app.Application
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.storage.EncryptedSharedPreferencesToSharedPreferencesMigration
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.StatusLog
import com.emarsys.geofence.Geofence
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.After
import org.junit.Test
import java.util.concurrent.CountDownLatch

class EmarsysDependencyInjectionTest {

    @After
    fun tearDown() {
        unmockkConstructor(EncryptedSharedPreferencesToSharedPreferencesMigration::class)
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testGeofence_whenGooglePlayIsNotAvailable() {
        val geofenceApi: Geofence = mockk(relaxed = true)
        val loggingGeofenceApi: Geofence = mockk(relaxed = true)

        val dependencyContainer = FakeDependencyContainer(
            geofence = geofenceApi,
            loggingGeofence = loggingGeofenceApi,
            isGooglePlayServiceAvailable = false
        )

        setupEmarsysComponent(dependencyContainer)

        EmarsysDependencyInjection.geofence() shouldBe loggingGeofenceApi
    }

    @Test
    fun migration_doesNotCauseStackOverflow_whenLoggerErrorCalledDuringMigrationAfterSetup() {
        mockkConstructor(EncryptedSharedPreferencesToSharedPreferencesMigration::class)
        every {
            anyConstructed<EncryptedSharedPreferencesToSharedPreferencesMigration>().migrate(any(), any())
        } answers {
            Logger.error(
                StatusLog(
                    EncryptedSharedPreferencesToSharedPreferencesMigration::class.java,
                    "migrate",
                    mapOf("exception" to "simulated keystore failure")
                )
            )
        }

        val application = InstrumentationRegistry.getTargetContext().applicationContext as Application
        val config = EmarsysConfig.Builder().application(application).build()

        shouldNotThrowAny {
            DefaultEmarsysDependencies(config)
        }

        val latch = CountDownLatch(1)
        emarsys().concurrentHandlerHolder.coreHandler.post { latch.countDown() }
        latch.await()
    }
}