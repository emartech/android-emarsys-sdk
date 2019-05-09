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
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarysDependencyContainer
import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch

class MobileEngageIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val APP_PASSWORD = "PaNkfOD90AVpYimMBuZopCpm8OWCrREu"
        private const val CONTACT_FIELD_ID = 3
        private const val MERCHANT_ID = "1428C8EE286EC34B"
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
    val activityRule = ActivityTestRule<FakeActivity>(FakeActivity::class.java)

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .inAppEventHandler(mock(EventHandler::class.java))
                .mobileEngageCredentials(APP_ID, APP_PASSWORD)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .build()

        completionHandler = createDefaultCoreCompletionHandler()

        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getCoreCompletionHandler() = completionHandler

            override fun getDeviceInfo() = DeviceInfo(
                    application,
                    mock(HardwareIdProvider::class.java).apply {
                        whenever(provideHardwareId()).thenReturn("mobileengage_integration_hwid")
                    },
                    mock(VersionProvider::class.java).apply {
                        whenever(provideSdkVersion()).thenReturn("0.0.0-mobileengage_integration_version")
                    },
                    LanguageProvider()
            )
        })

        errorCause = null

        ConnectionTestUtils.checkConnection(application)

        sharedPreferences = application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)

        ExperimentalTestUtils.resetExperimentalFeatures()

        Emarsys.setup(baseConfig)

        clientStateStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.clientStateStorage
        contactTokenStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.contactTokenStorage
        refreshTokenStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.refreshTokenStorage
        deviceInfoHashStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().deviceInfoHashStorage

        clientStateStorage.remove()
        contactTokenStorage.remove()
        refreshTokenStorage.remove()
        deviceInfoHashStorage.remove()

        IntegrationTestUtils.doLogin()

        completionListenerLatch = CountDownLatch(1)
        completionHandlerLatch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        ExperimentalTestUtils.resetExperimentalFeatures()

        with(DependencyInjection.getContainer<EmarysDependencyContainer>()) {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleWatchdog)
            application.unregisterActivityLifecycleCallbacks(currentActivityWatchdog)
            coreSdkHandler.looper.quit()
        }

        clientStateStorage.remove()
        contactTokenStorage.remove()
        refreshTokenStorage.remove()
        deviceInfoHashStorage.remove()

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
        val mobileEngageInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().mobileEngageInternal

        mobileEngageInternal.trackInternalCustomEvent(
                "integrationTestInternalCustomEvent",
                null,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackInternalCustomEvent_V3_withAttributes() {
        val mobileEngageInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().mobileEngageInternal

        mobileEngageInternal.trackInternalCustomEvent(
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

        Emarsys.Push.setPushToken("integration_test_push_token",
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testRemovePushToken() {
        Emarsys.Push.removePushToken(
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testDeepLinkOpen() {
        val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5_6"))

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

        val mobileEngageInternal = DependencyInjection.getContainer<MobileEngageDependencyContainer>().mobileEngageInternal

        mobileEngageInternal.trackDeviceInfo().also(this::eventuallyAssertCompletionHandlerSuccess)
    }

    private fun eventuallyStoreResult(errorCause: Throwable?) {
        this.errorCause = errorCause
        completionListenerLatch.countDown()
    }

    private fun eventuallyAssertSuccess(ignored: Any) {
        completionListenerLatch.await()
        errorCause shouldBe null
    }

    private fun eventuallyAssertCompletionHandlerSuccess(ignored: Any) {
        completionHandlerLatch?.await()
        errorCause shouldBe null
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