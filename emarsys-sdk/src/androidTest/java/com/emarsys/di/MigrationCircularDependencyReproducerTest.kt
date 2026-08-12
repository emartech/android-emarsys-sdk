package com.emarsys.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.Emarsys
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.storage.EncryptedSharedPreferencesToSharedPreferencesMigration
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test

class MigrationCircularDependencyReproducerTest {

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    private lateinit var baseConfig: EmarsysConfig

    private val failingOldPreferences = mockk<SharedPreferences>(relaxed = true).apply {
        every { all } throws SecurityException("AEADBadTagException: Failure during decryption")
    }

    private val migrationTarget = mockk<SharedPreferences>(relaxed = true)

    @Before
    fun setup() {
        application
            .getSharedPreferences("emarsys_secure_shared_preferences_v3", Context.MODE_PRIVATE)
            .edit().clear().commit()

        baseConfig = EmarsysConfig.Builder()
            .application(application)
            .applicationCode("14C19-A121F")
            .build()
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun reproducesStackOverflow_whenMigrationRunsInsideTheLazyInitializer() {
        val component = object : DefaultEmarsysComponent(baseConfig) {
            override val sharedPreferencesV3: SharedPreferences by lazy {
                EncryptedSharedPreferencesToSharedPreferencesMigration()
                    .migrate(failingOldPreferences, migrationTarget)
                migrationTarget
            }
        }
        setupEmarsysComponent(component)

        shouldThrow<StackOverflowError> {
            component.sharedPreferencesV3
        }
    }

    @Test
    fun doesNotCrash_whenMigrationRunsOutsideTheLazyInitializer() {
        shouldNotThrowAny {
            Emarsys.setup(baseConfig)
        }
    }
}
