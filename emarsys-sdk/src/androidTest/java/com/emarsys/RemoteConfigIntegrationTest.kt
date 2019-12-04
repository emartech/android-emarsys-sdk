package com.emarsys

import android.app.Application
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.DefaultConfigInternal
import com.emarsys.config.EmarsysConfig
import com.emarsys.config.FetchRemoteConfigAction
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch

class RemoteConfigIntegrationTest {

    private companion object {
        private const val APP_ID = "14C19-A121F"
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

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .inAppEventHandler(Mockito.mock(EventHandler::class.java))
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        FeatureTestUtils.resetFeatures()

        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getDeviceInfo() = DeviceInfo(
                    application,
                    Mockito.mock(HardwareIdProvider::class.java).apply {
                        whenever(provideHardwareId()).thenReturn("mobileengage_integration_hwid")
                    },
                    Mockito.mock(VersionProvider::class.java).apply {
                        whenever(provideSdkVersion()).thenReturn("0.0.0-mobileengage_integration_version")
                    },
                    LanguageProvider(),
                    Mockito.mock(NotificationManagerHelper::class.java),
                    true
            )
        })

        errorCause = null


        ConnectionTestUtils.checkConnection(application)

        Emarsys.setup(baseConfig)

        DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.remove()

        latch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        try {
            FeatureTestUtils.resetFeatures()

            with(DependencyInjection.getContainer<EmarsysDependencyContainer>()) {
                application.unregisterActivityLifecycleCallbacks(activityLifecycleWatchdog)
                application.unregisterActivityLifecycleCallbacks(currentActivityWatchdog)
                coreSdkHandler.looper.quit()
            }
            DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.remove()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testRemoteConfig() {
        DependencyInjection.getContainer<EmarsysDependencyContainer>().activityLifecycleWatchdog.applicationStartActions
                .filterIsInstance<FetchRemoteConfigAction>().first().execute(activityRule.activity)

        (DependencyInjection.getContainer<EmarsysDependencyContainer>().configInternal as DefaultConfigInternal).fetchRemoteConfig(ResultListener {
            latch.countDown()
        })

        latch.await()

        val endpointHost = DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceProvider.provideEndpointHost()
        endpointHost shouldBe "https://integraiton.me-client.eservice.emarsys.net"
    }
}