package com.emarsys

import android.app.Application
import com.emarsys.config.ConfigInternal
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.*
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch

class RemoteConfigIntegrationTest {

    private companion object {
        private const val APP_ID = "integrationTest"
        private const val CONTACT_FIELD_ID = 3
    }

    private lateinit var baseConfig: EmarsysConfig

    private var errorCause: Throwable? = null
    private lateinit var latch: CountDownLatch
    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()
        DependencyInjection.tearDown()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        FeatureTestUtils.resetFeatures()

        errorCause = null

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
        DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler().post {
            getDependency<ConfigInternal>().refreshRemoteConfig(CompletionListener { latch.countDown() })
        }

        latch.await()

        val clientServiceEndpointHost = getDependency<ServiceEndpointProvider>(Endpoint.ME_CLIENT_HOST).provideEndpointHost()
        val eventServiceEndpointHost = getDependency<ServiceEndpointProvider>(Endpoint.ME_EVENT_HOST).provideEndpointHost()
        clientServiceEndpointHost shouldBe "https://integration.me-client.eservice.emarsys.net"
        eventServiceEndpointHost shouldBe "https://integration.mobile-events.eservice.emarsys.net"
    }

}