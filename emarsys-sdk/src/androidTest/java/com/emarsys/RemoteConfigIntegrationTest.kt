package com.emarsys


import android.app.Application
import com.emarsys.config.EmarsysConfig
import com.emarsys.di.emarsys
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.rules.ConnectionRule
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotest.matchers.shouldBe
import org.junit.Rule
import java.util.concurrent.CountDownLatch


class RemoteConfigIntegrationTest : AnnotationSpec() {
    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    @Rule
    @JvmField
    val connectionRule = ConnectionRule(application)

    private companion object {
        private const val APP_ID = "EMS1F-17E15"
    }

    private lateinit var baseConfig: EmarsysConfig

    private lateinit var latch: CountDownLatch
    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


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