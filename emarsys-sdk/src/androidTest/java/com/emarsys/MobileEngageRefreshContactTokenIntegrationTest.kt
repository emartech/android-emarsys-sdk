package com.emarsys

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider



import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.storage.Storage
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.di.emarsys

import com.emarsys.testUtil.*
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import com.emarsys.testUtil.rules.RetryRule
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch

class MobileEngageRefreshContactTokenIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val CONTACT_FIELD_ID = 3
    }

    private lateinit var completionListenerLatch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var sharedPreferences: SharedPreferences
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

        FeatureTestUtils.resetFeatures()

        val deviceInfo = DeviceInfo(
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
                isAutomaticPushSendingEnabled = true,
                isGooglePlayAvailable = true
        )

        DefaultEmarsysDependencies(baseConfig, object : DefaultEmarsysComponent(baseConfig) {
            override val deviceInfo: DeviceInfo
                get() = deviceInfo
        })

        errorCause = null

        ConnectionTestUtils.checkConnection(application)

        sharedPreferences = application.getSharedPreferences("emarsys_secure_shared_preferences", Context.MODE_PRIVATE)

        Emarsys.setup(baseConfig)

        emarsys().coreSdkHandler.post {
            contactTokenStorage = emarsys().contactTokenStorage
            contactTokenStorage.remove()
            emarsys().pushTokenStorage.remove()
        }

        IntegrationTestUtils.doLogin()

        completionListenerLatch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testRefreshContactToken() {
        contactTokenStorage.remove()

        val refreshTokenInternal = emarsys().refreshTokenInternal

        refreshTokenInternal.refreshContactToken(this::eventuallyStoreResult).also(this::eventuallyAssertSuccess)

        contactTokenStorage.get() shouldNotBe null
    }

    @Test
    fun testRefreshContactToken_shouldUpdateContactToken_whenOutDated() {
        contactTokenStorage.remove()
        contactTokenStorage.set("tokenForIntegrationTest")

        val eventServiceInternal = emarsys().eventServiceInternal

        eventServiceInternal.trackInternalCustomEvent("integrationTest", emptyMap(), this::eventuallyStoreResult).also(this::eventuallyAssertSuccess)

        contactTokenStorage.get() shouldNotBe "tokenForIntegrationTest"
    }

    private fun eventuallyStoreResult(errorCause: Throwable?) {
        this.errorCause = errorCause
        completionListenerLatch.countDown()
    }

    private fun eventuallyAssertSuccess(ignored: Any) {
        completionListenerLatch.await()
        errorCause shouldBe null
    }

}