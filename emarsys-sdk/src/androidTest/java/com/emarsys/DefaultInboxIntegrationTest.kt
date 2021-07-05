package com.emarsys

import android.app.Application
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.api.result.Try
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.ConnectionRule
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.isAccessible


class DefaultInboxIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val CONTACT_FIELD_ID = 3
        private const val SDK_VERSION = "2.1.0-integration"
        private const val LANGUAGE = "en-US"
        private const val MESSAGE_ID = Integer.MAX_VALUE.toString()
    }

    private lateinit var latch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    lateinit var triedNotificationInboxStatus: Try<NotificationInboxStatus>
    private lateinit var triedInboxResult: Try<InboxResult>

    private var errorCause: Throwable? = null

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    @Rule
    @JvmField
    val connectionRule = ConnectionRule(application)

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
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

        IntegrationTestUtils.doLogin()
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testFetchNotifications() {
        Emarsys.inbox.fetchNotifications(eventuallyStoreResultInProperty(this::triedNotificationInboxStatus.setter)).eventuallyAssert {
            triedNotificationInboxStatus.errorCause shouldBe null
            triedNotificationInboxStatus.result shouldNotBe null
        }
    }

    @Test
    fun testResetBadgeCount() {
        Emarsys.inbox.resetBadgeCount(eventuallyStoreResultInProperty(this::errorCause.setter)).eventuallyAssert {
            errorCause shouldBe null
        }
    }

    @Test
    fun testTrackNotificationOpen() {
        val notification = Notification(
                "id",
                "161e_D/1UiO/jCmE4",
                "title",
                null,
                emptyMap(),
                JSONObject(),
                2000,
                Date().time)

        Emarsys.inbox.trackNotificationOpen(notification, eventuallyStoreResultInProperty(this::errorCause.setter)).eventuallyAssert {
            errorCause shouldBe null
        }
    }

    @Test
    fun testFetchInboxMessages() {
        Emarsys.messageInbox.fetchMessages(eventuallyStoreResultInProperty(this::triedInboxResult.setter)).eventuallyAssert {
            triedInboxResult.errorCause shouldBe null
            triedInboxResult.result shouldNotBe null
        }
    }

    @Test
    fun testAddTag() {
        Emarsys.messageInbox.addTag("TEST_TAG", MESSAGE_ID, eventuallyStoreResultInProperty(this::errorCause.setter)).eventuallyAssert {
            errorCause shouldBe null
        }
    }

    @Test
    fun testRemoveTag() {
        Emarsys.messageInbox.removeTag("TEST_TAG", MESSAGE_ID, eventuallyStoreResultInProperty(this::errorCause.setter)).eventuallyAssert {
            errorCause shouldBe null
        }
    }


    private fun <T> eventuallyStoreResultInProperty(setter: KMutableProperty0.Setter<T>): (T) -> Unit {
        return {
            setter.isAccessible = true
            setter(it)
            latch.countDown()
        }
    }

    private fun Any.eventuallyAssert(assertion: () -> Unit) {
        latch.await()
        assertion.invoke()
    }
}
