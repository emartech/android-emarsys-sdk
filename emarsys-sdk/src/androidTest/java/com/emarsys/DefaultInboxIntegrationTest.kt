package com.emarsys


import android.app.Application
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.KotestRunnerAndroid
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.ConnectionRule
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch


@RunWith(KotestRunnerAndroid::class)
class DefaultInboxIntegrationTest : AnnotationSpec() {
    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    @Rule
    @JvmField
    val connectionRule = ConnectionRule(application)

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val SDK_VERSION = "2.1.0-integration"
        private const val LANGUAGE = "en-US"
        private const val MESSAGE_ID = Integer.MAX_VALUE.toString()
    }

    private lateinit var latch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig

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

        errorCause = null
        latch = CountDownLatch(1)
        val deviceInfo = DeviceInfo(
            application,
            mock(HardwareIdProvider::class.java).apply {
                whenever(provideHardwareId()).thenReturn("inboxv1_integration_hwid")
            },
            mock(VersionProvider::class.java).apply {
                whenever(provideSdkVersion()).thenReturn(SDK_VERSION)
            },
            mock(LanguageProvider::class.java).apply {
                whenever(provideLanguage(any())).thenReturn(LANGUAGE)
            },
            mock(NotificationManagerHelper::class.java),
            isAutomaticPushSendingEnabled = true,
            isGooglePlayAvailable = true
        )

        DefaultEmarsysDependencies(baseConfig, object : DefaultEmarsysComponent(baseConfig) {
            override val deviceInfo: DeviceInfo
                get() = deviceInfo
        })

        Emarsys.setup(baseConfig)

        IntegrationTestUtils.doLogin(2575)
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testFetchInboxMessages() {
        val latch = CountDownLatch(1)
        Emarsys.messageInbox.fetchMessages {
            it.errorCause shouldBe null
            it.result shouldNotBe null
            latch.countDown()
        }
        latch.await()
    }

    @Test
    fun testAddTag() {
        val latch = CountDownLatch(1)
        Emarsys.messageInbox.addTag("TEST_TAG", MESSAGE_ID) {
            it shouldBe null
            latch.countDown()
        }
        latch.await()
    }

    @Test
    fun testRemoveTag() {
        val latch = CountDownLatch(1)
        Emarsys.messageInbox.removeTag("TEST_TAG", MESSAGE_ID) {
            latch.countDown()
            it shouldBe null
        }
        latch.await()
    }
}
