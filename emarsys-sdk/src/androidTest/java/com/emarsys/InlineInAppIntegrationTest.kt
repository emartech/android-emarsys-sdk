package com.emarsys

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.inapp.ui.InlineInAppView
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.predict.storage.PredictStorageKey
import com.emarsys.testUtil.*
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.RetryRule
import io.kotlintest.shouldNotBe
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch

class InlineInAppIntegrationTest {
    private companion object {
        private const val APP_ID = "EMS11-C3FD3"
        private const val CONTACT_FIELD_ID = 3
    }

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    private lateinit var baseConfig: EmarsysConfig
    private lateinit var sharedPreferences: SharedPreferences
    private var completionHandlerLatch: CountDownLatch? = null
    private lateinit var completionListenerLatch: CountDownLatch

    private var errorCause: Throwable? = null


    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    @Before
    fun setUp() {
        DatabaseTestUtils.deleteCoreDatabase()

        application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        FeatureTestUtils.resetFeatures()

        val mockPushTokenProvider = Mockito.mock(PushTokenProvider::class.java).apply {
            whenever(providePushToken()).thenReturn("integration_test_push_token")
        }

        val setupLatch = CountDownLatch(1)
        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {

            override fun getPushTokenProvider(): PushTokenProvider {
                return mockPushTokenProvider
            }

            override fun getDeviceInfo(): DeviceInfo {
                return DeviceInfo(
                        application,
                        Mockito.mock(HardwareIdProvider::class.java).apply {
                            whenever(provideHardwareId()).thenReturn("mobileengage_integration_hwid")
                        },
                        Mockito.mock(VersionProvider::class.java).apply {
                            whenever(provideSdkVersion()).thenReturn("0.0.0-mobileengage_integration_version")
                        },
                        Mockito.mock(LanguageProvider::class.java).apply {
                            whenever(provideLanguage(ArgumentMatchers.any())).thenReturn("en-US")
                        },
                        Mockito.mock(NotificationManagerHelper::class.java),
                        true
                )
            }
        })
        DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler().post {
            setupLatch.countDown()
        }

        setupLatch.await()

        errorCause = null

        ConnectionTestUtils.checkConnection(application)

        sharedPreferences = application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)

        Emarsys.setup(baseConfig)

        waitForTask()

        val clientStateStorage = getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_STATE.key)
        val contactTokenStorage = getDependency<StringStorage>(MobileEngageStorageKey.CONTACT_TOKEN.key)

        clientStateStorage.remove()
        contactTokenStorage.remove()

        getDependency<StringStorage>(MobileEngageStorageKey.DEVICE_INFO_HASH.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.REFRESH_TOKEN.key).remove()

        getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.EVENT_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.ME_V2_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.INBOX_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key).remove()
        getDependency<StringStorage>(PredictStorageKey.PREDICT_SERVICE_URL.key).remove()


        IntegrationTestUtils.doLogin()

        completionListenerLatch = CountDownLatch(1)
        completionHandlerLatch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        try {
            doTearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun doTearDown() {
        FeatureTestUtils.resetFeatures()

        getDependency<Handler>("coreSdkHandler").looper.quit()
        application.unregisterActivityLifecycleCallbacks(getDependency<ActivityLifecycleWatchdog>())
        application.unregisterActivityLifecycleCallbacks(getDependency<CurrentActivityWatchdog>())

        getDependency<StringStorage>(MobileEngageStorageKey.DEVICE_INFO_HASH.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.REFRESH_TOKEN.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.PUSH_TOKEN.key).remove()

        DependencyInjection.getContainer<EmarsysDependencyContainer>().getClientStateStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getContactTokenStorage().remove()
        DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().getRequestContext().pushTokenStorage.remove()

        DependencyInjection.getContainer<EmarsysDependencyContainer>().getClientServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getEventServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getDeepLinkServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getMobileEngageV2ServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getInboxServiceStorage().remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getPredictServiceStorage().remove()

        DependencyInjection.tearDown()
    }

    @Test
    @Ignore
    fun testFetchInlineInAppMessage() {
        val latch = CountDownLatch(1)
        val inlineInAppView = InlineInAppView("main-screen-banner")

        inlineInAppView.fetchInlineInAppMessage()
        Emarsys.setContact("test-contact") {
            latch.countDown()
        }

        latch.await()

        val inlineHTML = ReflectionTestUtils.getInstanceField<String>(inlineInAppView, "html")

        inlineHTML shouldNotBe null
    }

    private fun waitForTask() {
        val latch = CountDownLatch(1)
        getDependency<Handler>("coreSdkHandler").post {
            latch.countDown()
        }
        latch.await()
    }
}