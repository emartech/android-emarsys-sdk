package com.emarsys

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.RetryRule
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.kotlintest.matchers.numerics.shouldBeInRange
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch

class MobileEngageIntegrationTest {

    private companion object {
        private const val APP_ID = "14C19-A121F"
        private const val OTHER_APP_ID = "EMS11-C3FD3"
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

    private var completionHandlerLatch: CountDownLatch? = null
    private lateinit var completionListenerLatch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var responseModel: ResponseModel
    private lateinit var completionHandler: DefaultCoreCompletionHandler
    private lateinit var clientStateStorage: Storage<String>
    private lateinit var contactTokenStorage: Storage<String>
    private lateinit var refreshTokenStorage: Storage<String>
    private lateinit var deviceInfoHashStorage: Storage<Int>

    private var errorCause: Throwable? = null

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule


    @Rule
    @JvmField
    val activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .inAppEventHandler(mock(EventHandler::class.java))
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        completionHandler = createDefaultCoreCompletionHandler()

        FeatureTestUtils.resetFeatures()

        val mockPushTokenProvider = mock(PushTokenProvider::class.java).apply {
            whenever(providePushToken()).thenReturn("integration_test_push_token")
        }

        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getClientServiceProvider(): ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java).apply {
                whenever(provideEndpointHost()).thenReturn(Endpoint.ME_V3_CLIENT_HOST)
            }

            override fun getEventServiceProvider(): ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java).apply {
                whenever(provideEndpointHost()).thenReturn(Endpoint.ME_V3_EVENT_HOST)
            }

            override fun getPushTokenProvider() = mockPushTokenProvider

            override fun getCoreCompletionHandler() = completionHandler

            override fun getDeviceInfo() = DeviceInfo(
                    application,
                    mock(HardwareIdProvider::class.java).apply {
                        whenever(provideHardwareId()).thenReturn("mobileengage_integration_hwid")
                    },
                    mock(VersionProvider::class.java).apply {
                        whenever(provideSdkVersion()).thenReturn("0.0.0-mobileengage_integration_version")
                    },
                    mock(LanguageProvider::class.java).apply {
                        whenever(provideLanguage(ArgumentMatchers.any())).thenReturn("en-US")
                    },
                    mock(NotificationManagerHelper::class.java),
                    true
            )
        })

        errorCause = null

        ConnectionTestUtils.checkConnection(application)

        sharedPreferences = application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)

        Emarsys.setup(baseConfig)

        clientStateStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.clientStateStorage
        contactTokenStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.contactTokenStorage
        refreshTokenStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.refreshTokenStorage
        deviceInfoHashStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().deviceInfoHashStorage

        clientStateStorage.remove()
        contactTokenStorage.remove()
        refreshTokenStorage.remove()
        deviceInfoHashStorage.remove()

        DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.set(null)


        IntegrationTestUtils.doLogin()

        completionListenerLatch = CountDownLatch(1)
        completionHandlerLatch = CountDownLatch(1)
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

            clientStateStorage.remove()
            contactTokenStorage.remove()
            refreshTokenStorage.remove()
            deviceInfoHashStorage.remove()

            DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.set(null)

            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testSetContact() {
        contactTokenStorage.remove()
        Emarsys.setContact(
                "test@test.com",
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testClearContact() {
        Emarsys.clearContact(
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackCustomEvent_V3_noAttributes() {
        Emarsys.trackCustomEvent(
                "integrationTestCustomEvent",
                null,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackCustomEvent_V3_withAttributes() {
        Emarsys.trackCustomEvent(
                "integrationTestCustomEvent",
                mapOf("key1" to "value1", "key2" to "value2"),
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackInternalCustomEvent_V3_noAttributes() {
        val eventServiceInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().eventServiceInternal

        eventServiceInternal.trackInternalCustomEvent(
                "integrationTestInternalCustomEvent",
                null,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackInternalCustomEvent_V3_withAttributes() {
        val eventServiceInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().eventServiceInternal

        eventServiceInternal.trackInternalCustomEvent(
                "integrationTestInternalCustomEvent",
                mapOf("key1" to "value1", "key2" to "value2"),
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackMessageOpen_V3() {
        val intent = Intent().apply {
            putExtra("payload", Bundle().apply {
                putString("key1", "value1")
                putString("u", """{"sid": "dd8_zXfDdndBNEQi"}""")
            })
        }

        Emarsys.Push.trackMessageOpen(
                intent,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testSetPushToken() {
        clientStateStorage.remove()
        contactTokenStorage.remove()

        Emarsys.push.setPushToken("integration_test_push_token",
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testRemovePushToken() {
        Emarsys.Push.clearPushToken(
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testDeepLinkOpen() {
        val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/emartech/android-emarsys-sdk/wiki?ems_dl=210268110_ZVwwYrYUFR_1_100302293_1_2000000"))

        Emarsys.trackDeepLink(
                activityRule.activity,
                intent,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackDeviceInfo() {
        clientStateStorage.remove()
        contactTokenStorage.remove()

        val clientServiceInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().clientServiceInternal

        clientServiceInternal.trackDeviceInfo().also(this::eventuallyAssertCompletionHandlerSuccess)
    }

    @Test
    fun testConfig_changeApplicationCode() {
        val originalApplicationCode = Emarsys.Config.applicationCode
        Emarsys.Config.changeApplicationCode(OTHER_APP_ID, this::eventuallyStoreResult).also(this::eventuallyAssertSuccess)
        originalApplicationCode shouldNotBe Emarsys.Config.applicationCode
        Emarsys.Config.applicationCode shouldBe OTHER_APP_ID
    }

    private fun eventuallyStoreResult(errorCause: Throwable?) {
        this.errorCause = errorCause
        completionListenerLatch.countDown()
    }

    private fun eventuallyAssertSuccess(ignored: Any) {
        completionListenerLatch.await()
        errorCause shouldBe null
        responseModel.statusCode shouldBeInRange IntRange(200, 299)
    }

    private fun eventuallyAssertCompletionHandlerSuccess(ignored: Any) {
        completionHandlerLatch?.await()
        errorCause shouldBe null
        responseModel.statusCode shouldBeInRange IntRange(200, 299)
    }

    private fun createDefaultCoreCompletionHandler(): DefaultCoreCompletionHandler {
        return object : DefaultCoreCompletionHandler(mutableMapOf()) {
            override fun onSuccess(id: String?, responseModel: ResponseModel) {
                super.onSuccess(id, responseModel)
                this@MobileEngageIntegrationTest.responseModel = responseModel
                completionHandlerLatch?.countDown()

            }

            override fun onError(id: String?, cause: Exception) {
                super.onError(id, cause)
                this@MobileEngageIntegrationTest.errorCause = cause
                completionHandlerLatch?.countDown()
            }

            override fun onError(id: String?, responseModel: ResponseModel) {
                super.onError(id, responseModel)
                this@MobileEngageIntegrationTest.responseModel = responseModel
                completionHandlerLatch?.countDown()
            }
        }
    }
}