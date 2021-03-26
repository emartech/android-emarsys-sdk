package com.emarsys

import android.app.Application
import android.content.Intent
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
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
        private const val CONTACT_FIELD_ID = 3
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
    val activityRule = ActivityTestRule(FakeActivity::class.java, false, false)

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
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
        activityRule.launchActivity(Intent())

        val coreSdkHandler = DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler()
        coreSdkHandler.post {
            latch.countDown()
        }
        latch.await()

        val clientServiceEndpointHost = getDependency<ServiceEndpointProvider>(Endpoint.ME_CLIENT_HOST).provideEndpointHost()
        val eventServiceEndpointHost = getDependency<ServiceEndpointProvider>(Endpoint.ME_EVENT_HOST).provideEndpointHost()
        clientServiceEndpointHost shouldBe "https://me-client-staging.eservice.emarsys.com"
        eventServiceEndpointHost shouldBe "https://mobile-events-staging.eservice.emarsys.com"
    }
}