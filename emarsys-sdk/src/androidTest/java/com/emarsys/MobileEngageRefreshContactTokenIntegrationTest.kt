package com.emarsys


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.clientid.ClientIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.storage.Storage
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.di.emarsys
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.RetryUtils
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import com.emarsys.testUtil.rules.RetryRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import java.util.concurrent.CountDownLatch

class MobileEngageRefreshContactTokenIntegrationTest : AnnotationSpec() {

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    companion object {
        private const val APP_ID = "14C19-A121F"
    }

    private lateinit var completionListenerLatch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var contactTokenStorage: Storage<String?>

    private var errorCause: Throwable? = null

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        baseConfig = EmarsysConfig.Builder()
            .application(application)
            .applicationCode(APP_ID)
            .build()

        FeatureTestUtils.resetFeatures()

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
        })

        errorCause = null

        ConnectionTestUtils.checkConnection(application)

        sharedPreferences = application.getSharedPreferences(
            "emarsys_secure_shared_preferences",
            Context.MODE_PRIVATE
        )

        Emarsys.setup(baseConfig)

        emarsys().concurrentHandlerHolder.coreHandler.post {
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

    fun testRefreshContactToken_shouldUpdateContactToken_whenOutDated() {
        contactTokenStorage.remove()
        contactTokenStorage.set("tokenForIntegrationTest")

        val eventServiceInternal = emarsys().eventServiceInternal

        eventServiceInternal.trackInternalCustomEvent(
            "integrationTest",
            emptyMap(),
            this::eventuallyStoreResult
        ).apply { eventuallyAssertSuccess() }

        contactTokenStorage.get() shouldNotBe "tokenForIntegrationTest"
    }

    private fun eventuallyStoreResult(errorCause: Throwable?) {
        this.errorCause = errorCause
        completionListenerLatch.countDown()
    }

    private fun eventuallyAssertSuccess() {
        completionListenerLatch.await()
        errorCause shouldBe null
    }

}