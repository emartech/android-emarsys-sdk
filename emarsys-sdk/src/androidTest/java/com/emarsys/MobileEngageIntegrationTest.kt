package com.emarsys

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider

import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.di.emarsys
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.testUtil.*
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import com.emarsys.testUtil.rules.RetryRule
import io.kotlintest.matchers.shouldBeInRange
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch

class MobileEngageIntegrationTest {

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

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

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
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        completionHandler = createDefaultCoreCompletionHandler()


        val mockPushTokenProvider = mock(PushTokenProvider::class.java).apply {
            whenever(providePushToken()).thenReturn("integration_test_push_token")
        }
        val deviceInfo = DeviceInfo(
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

        sharedPreferences = application.getSharedPreferences("emarsys_secure_shared_preferences", Context.MODE_PRIVATE)

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
        val eventServiceInternal = emarsys().eventServiceInternal

        eventServiceInternal.trackInternalCustomEvent(
                "integrationTestInternalCustomEvent",
                null,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackInternalCustomEvent_V3_withAttributes() {
        val eventServiceInternal = emarsys().eventServiceInternal

        eventServiceInternal.trackInternalCustomEvent(
                "integrationTestInternalCustomEvent",
                mapOf("key1" to "value1", "key2" to "value2"),
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testSetPushToken() {
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
        Thread.sleep(1000)
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

        Thread.sleep(1000)
    }

    @Test
    fun testTrackDeviceInfo() {
        val clientServiceInternal = emarsys().clientServiceInternal

        clientServiceInternal.trackDeviceInfo(this::eventuallyStoreResult)
                .also(this::eventuallyAssertCompletionHandlerSuccess)
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
        val setupLatch = CountDownLatch(1)
        emarsys().coreSdkHandler.post {
            setupLatch.countDown()
        }
        setupLatch.await()

        IntegrationTestUtils.tearDownEmarsys(application)

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
}