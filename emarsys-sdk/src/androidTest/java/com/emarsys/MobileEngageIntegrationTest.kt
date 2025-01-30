package com.emarsys


import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.clientid.ClientIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.di.emarsys
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.RetryUtils
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import com.emarsys.testUtil.rules.RetryRule
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch

class MobileEngageIntegrationTest  {
    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    private companion object {
        private const val APP_ID = "14C19-A121F"
        private const val OTHER_APP_ID = "EMS11-C3FD3"
        private const val CONTACT_FIELD_ID = 3
    }

    private var completionHandlerLatch: CountDownLatch? = null
    private lateinit var completionListenerLatch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var responseModel: ResponseModel
    private lateinit var completionHandler: DefaultCoreCompletionHandler

    private var errorCause: Throwable? = null

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        application.getSharedPreferences("emarsys_secure_shared_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        baseConfig = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .build()

        completionHandler = createDefaultCoreCompletionHandler()


        val mockPushTokenProvider = mockk<PushTokenProvider>(relaxed = true).apply {
            every { providePushToken() } returns "integration_test_push_token"
        }
        val deviceInfo = DeviceInfo(
            application,
            mockk<ClientIdProvider>(relaxed = true).apply {
                every { provideClientId() } returns "mobileengage_integration_hwid"
            },
            mockk<VersionProvider>(relaxed = true).apply {
                every { provideSdkVersion() } returns "0.0.0-mobileengage_integration_version"
            },
            mockk<LanguageProvider>(relaxed = true).apply {
                every { provideLanguage(any()) } returns "en-US"
            },
            mockk<NotificationManagerHelper>(relaxed = true),
            isAutomaticPushSendingEnabled = true,
            isGooglePlayAvailable = true
        )

        DefaultEmarsysDependencies(baseConfig, object : DefaultEmarsysComponent(baseConfig) {
            override val deviceInfo: DeviceInfo
                get() = deviceInfo
            override val pushTokenProvider: PushTokenProvider
                get() = mockPushTokenProvider
            override val coreCompletionHandler: DefaultCoreCompletionHandler
                get() = completionHandler
        })

        errorCause = null

        ConnectionTestUtils.checkConnection(application)

        sharedPreferences = application.getSharedPreferences(
            "emarsys_secure_shared_preferences",
            Context.MODE_PRIVATE
        )

        Emarsys.setup(baseConfig)

        IntegrationTestUtils.doLogin()

        completionListenerLatch = CountDownLatch(1)
        completionHandlerLatch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        try {
            IntegrationTestUtils.tearDownEmarsys(application)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testSetContact() {
        Emarsys.setContact(
            CONTACT_FIELD_ID,
            "test@test.com",
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }
    }

    @Test
    fun testClearContact() {
        Emarsys.clearContact(
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }
    }

    @Test
    fun testTrackCustomEvent_V3_noAttributes() {
        Emarsys.trackCustomEvent(
            "integrationTestCustomEvent",
            null,
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }
    }

    @Test
    fun testTrackCustomEvent_V3_withAttributes() {
        Emarsys.trackCustomEvent(
            "integrationTestCustomEvent",
            mapOf("key1" to "value1", "key2" to "value2"),
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }
    }

    @Test
    fun testTrackInternalCustomEvent_V3_noAttributes() {
        val eventServiceInternal = emarsys().eventServiceInternal

        eventServiceInternal.trackInternalCustomEvent(
            "integrationTestInternalCustomEvent",
            null,
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }
    }

    @Test
    fun testTrackInternalCustomEvent_V3_withAttributes() {
        val eventServiceInternal = emarsys().eventServiceInternal

        eventServiceInternal.trackInternalCustomEvent(
            "integrationTestInternalCustomEvent",
            mapOf("key1" to "value1", "key2" to "value2"),
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }
    }

    @Test
    fun testSetPushToken() {
        Emarsys.push.setPushToken(
            "integration_test_push_token",
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }
    }

    @Test
    fun testRemovePushToken() {
        Emarsys.push.clearPushToken(
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }
    }

    @Test
    fun testDeepLinkOpen() {
        Thread.sleep(1000)
        val activity = mockk<Activity>(relaxed = true)
        every { activity.intent } returns Intent()

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/emartech/android-emarsys-sdk/wiki?ems_dl=210268110_ZVwwYrYUFR_1_100302293_1_2000000")
        )

        Emarsys.trackDeepLink(
            activity,
            intent,
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }

        Thread.sleep(1000)
    }

    @Test
    fun testTrackDeviceInfo() {
        val clientServiceInternal = emarsys().clientServiceInternal

        clientServiceInternal.trackDeviceInfo(this::eventuallyStoreResult)
            .apply { eventuallyAssertCompletionHandlerSuccess() }
    }

    @Test
    fun testConfig_changeApplicationCode() {
        val originalApplicationCode = Emarsys.config.applicationCode
        Emarsys.config.changeApplicationCode(OTHER_APP_ID, this::eventuallyStoreResult)
            .apply { eventuallyAssertSuccess() }
        originalApplicationCode shouldNotBe Emarsys.config.applicationCode
        Emarsys.config.applicationCode shouldBe OTHER_APP_ID
    }

    @Test
    fun testConfig_changeApplicationCode_nilToSomething() {
        val setupLatch = CountDownLatch(1)
        emarsys().concurrentHandlerHolder.coreHandler.post {
            setupLatch.countDown()
        }
        setupLatch.await()

        IntegrationTestUtils.tearDownEmarsys(application)

        val config = EmarsysConfig.Builder()
            .application(application)
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

    private fun eventuallyAssertSuccess() {
        completionListenerLatch.await()
        completionHandlerLatch?.await()
        errorCause shouldBe null
        responseModel.statusCode shouldBeInRange IntRange(200, 299)
    }

    private fun eventuallyAssertCompletionHandlerSuccess() {
        completionHandlerLatch?.await()
        errorCause shouldBe null
        responseModel.statusCode shouldBeInRange IntRange(200, 299)
    }

    private fun createDefaultCoreCompletionHandler(): DefaultCoreCompletionHandler {
        return object : DefaultCoreCompletionHandler(mutableMapOf()) {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                super.onSuccess(id, responseModel)
                this@MobileEngageIntegrationTest.responseModel = responseModel
                completionHandlerLatch?.countDown()

            }

            override fun onError(id: String, cause: Exception) {
                super.onError(id, cause)
                this@MobileEngageIntegrationTest.errorCause = cause
                completionHandlerLatch?.countDown()
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                super.onError(id, responseModel)
                this@MobileEngageIntegrationTest.responseModel = responseModel
                completionHandlerLatch?.countDown()
            }
        }
    }
}