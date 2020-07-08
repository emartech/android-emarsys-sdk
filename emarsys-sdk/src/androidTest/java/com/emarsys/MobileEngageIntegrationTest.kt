package com.emarsys

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.DefaultCoreCompletionHandler
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
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StringStorage
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.predict.storage.PredictStorageKey
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
    private lateinit var clientStateStorage: Storage<String?>
    private lateinit var contactTokenStorage: Storage<String?>

    private var errorCause: Throwable? = null

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

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

        val setupLatch = CountDownLatch(1)
        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getCoreCompletionHandler(): DefaultCoreCompletionHandler {
                return completionHandler
            }

            override fun getPushTokenProvider(): PushTokenProvider {
                return mockPushTokenProvider
            }

            override fun getDeviceInfo(): DeviceInfo {
                return DeviceInfo(
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

        clientStateStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().getRequestContext().clientStateStorage
        contactTokenStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().getRequestContext().contactTokenStorage

        clientStateStorage = getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_STATE.key)
        contactTokenStorage = getDependency<StringStorage>(MobileEngageStorageKey.CONTACT_TOKEN.key)

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

        clientStateStorage.remove()
        contactTokenStorage.remove()
        DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().getRequestContext().pushTokenStorage.remove()

        DependencyInjection.getContainer<EmarsysDependencyContainer>().getClientServiceStorage().set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getEventServiceStorage().set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getDeepLinkServiceStorage().set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getMobileEngageV2ServiceStorage().set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getInboxServiceStorage().set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().getPredictServiceStorage().set(null)

        DependencyInjection.tearDown()
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
        val eventServiceInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().getEventServiceInternal()

        eventServiceInternal.trackInternalCustomEvent(
                "integrationTestInternalCustomEvent",
                null,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackInternalCustomEvent_V3_withAttributes() {
        val eventServiceInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().getEventServiceInternal()

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
                putString("u", """{"sid": "1cf3f_JhIPRzBvNtQF"}""")
            })
        }

        Emarsys.push.trackMessageOpen(
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
        Emarsys.push.clearPushToken(
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testDeepLinkOpen() {
        val activity = mock(Activity::class.java)
        whenever(activity.intent).thenReturn(Intent())

        val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/emartech/android-emarsys-sdk/wiki?ems_dl=210268110_ZVwwYrYUFR_1_100302293_1_2000000"))

        Emarsys.trackDeepLink(
                activity,
                intent,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackDeviceInfo() {
        clientStateStorage.remove()
        contactTokenStorage.remove()

        val clientServiceInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().getClientServiceInternal()

        clientServiceInternal.trackDeviceInfo(null).also(this::eventuallyAssertCompletionHandlerSuccess)
    }

    @Test
    fun testConfig_changeApplicationCode() {
        val originalApplicationCode = Emarsys.config.applicationCode
        Emarsys.config.changeApplicationCode(OTHER_APP_ID, this::eventuallyStoreResult).also(this::eventuallyAssertSuccess)
        originalApplicationCode shouldNotBe Emarsys.config.applicationCode
        Emarsys.config.applicationCode shouldBe OTHER_APP_ID
    }

    @Test
    fun testConfig_changeApplicationCode_nilToSomething() {
        doTearDown()

        val config = EmarsysConfig.Builder()
                .application(application)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()
        Emarsys.setup(config)

        var returnedThrowable: Throwable? = Throwable("testErrorCause")

        val latch = CountDownLatch(1)
        Emarsys.config.changeApplicationCode(APP_ID) {
            returnedThrowable = it
            latch.countDown()
        }
        latch.await()

        returnedThrowable shouldBe null
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

    private fun waitForTask() {
        val latch = CountDownLatch(1)
        getDependency<Handler>("coreSdkHandler").post {
            latch.countDown()
        }
        latch.await()
    }
}