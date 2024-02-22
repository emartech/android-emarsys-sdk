package com.emarsys


import android.app.Application
import com.emarsys.config.EmarsysConfig
import com.emarsys.di.emarsys
import com.emarsys.testUtil.*
import com.emarsys.testUtil.rules.DuplicatedThreadExtension
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.CountDownLatch

@ExtendWith(DuplicatedThreadExtension::class)

class RemoteConfigIntegrationTest {

    private companion object {
        private const val APP_ID = "EMS1F-17E15"
    }

    private lateinit var baseConfig: EmarsysConfig

    private lateinit var latch: CountDownLatch
    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


    @BeforeEach
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        baseConfig = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .build()

        ConnectionTestUtils.checkConnection(application)

        Emarsys.setup(baseConfig)

        latch = CountDownLatch(1)
    }

    @AfterEach
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testRemoteConfig() {
        val coreSdkHandler = emarsys().concurrentHandlerHolder
        coreSdkHandler.coreHandler.post {
            coreSdkHandler.coreHandler.post {
                emarsys().configInternal.refreshRemoteConfig { latch.countDown() }
            }
        }
        latch.await()
        val clientServiceEndpointHost =
            emarsys().clientServiceEndpointProvider.provideEndpointHost()
        val eventServiceEndpointHost = emarsys().eventServiceEndpointProvider.provideEndpointHost()
        clientServiceEndpointHost shouldBe "https://me-client-staging.eservice.emarsys.com"
        eventServiceEndpointHost shouldBe "https://mobile-events-staging.eservice.emarsys.com"
    }

}