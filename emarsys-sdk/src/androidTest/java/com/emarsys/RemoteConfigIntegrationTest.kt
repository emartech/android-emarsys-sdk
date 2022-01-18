package com.emarsys

import android.app.Application
import com.emarsys.config.EmarsysConfig


import com.emarsys.di.emarsys
import com.emarsys.testUtil.*
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch

class RemoteConfigIntegrationTest {

    private companion object {
        private const val APP_ID = "EMS1F-17E15"
    }

    private lateinit var baseConfig: EmarsysConfig

    private lateinit var latch: CountDownLatch
    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    @Before
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

    @After
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