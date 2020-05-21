package com.emarsys

import android.app.Application
import android.os.Handler
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.ConfigInternal
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.Container.getDependency
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.predict.storage.PredictStorageKey
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.whenever
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.kotlintest.shouldBe
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch

class RemoteConfigIntegrationTest {

    private companion object {
        private const val APP_ID = "integrationTest"
        private const val CONTACT_FIELD_ID = 3

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            val options: FirebaseOptions = FirebaseOptions.Builder()
                    .setApplicationId("com.emarsys.sdk")
                    .build()

            FirebaseApp.initializeApp(InstrumentationRegistry.getTargetContext(), options)
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            FirebaseApp.clearInstancesForTest()
        }
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
        DependencyInjection.tearDown()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        FeatureTestUtils.resetFeatures()

        val setupLatch = CountDownLatch(1)
        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig, Runnable {
            setupLatch.countDown()
        }) {
            override fun getDeviceInfo(): DeviceInfo {
                return DeviceInfo(
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
            }
        })

        setupLatch.await()

        errorCause = null

        ConnectionTestUtils.checkConnection(application)

        Emarsys.setup(baseConfig)

        DependencyInjection.getContainer<EmarsysDependencyContainer>().getClientServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getEventServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getDeepLinkServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getMobileEngageV2ServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getInboxServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getPredictServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getMessageInboxServiceStorage().remove()

        latch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        try {
            FeatureTestUtils.resetFeatures()

            getDependency<Handler>("coreSdkHandler").looper.quit()
            application.unregisterActivityLifecycleCallbacks(getDependency<ActivityLifecycleWatchdog>())
            application.unregisterActivityLifecycleCallbacks(getDependency<CurrentActivityWatchdog>())

            getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.EVENT_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.ME_V2_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.INBOX_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key).remove()
            getDependency<StringStorage>(PredictStorageKey.PREDICT_SERVICE_URL.key).remove()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testRemoteConfig() {
        getDependency<ConfigInternal>().refreshRemoteConfig(CompletionListener { latch.countDown() })

        latch.await()

        val clientServiceEndpointHost = getDependency<ServiceEndpointProvider>(Endpoint.ME_V3_CLIENT_HOST).provideEndpointHost()
        val eventServiceEndpointHost = getDependency<ServiceEndpointProvider>(Endpoint.ME_V3_EVENT_HOST).provideEndpointHost()
        clientServiceEndpointHost shouldBe "https://integration.me-client.eservice.emarsys.net"
        eventServiceEndpointHost shouldBe "https://integration.mobile-events.eservice.emarsys.net"
    }

}