package com.emarsys

import android.app.Application
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.api.result.Try
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
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.predict.storage.PredictStorageKey
import com.emarsys.testUtil.*
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.isAccessible
@Ignore
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

    @Before
    fun setup() {
        FeatureTestUtils.resetFeatures()
        DatabaseTestUtils.deleteCoreDatabase()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        errorCause = null
        latch = CountDownLatch(1)
        ConnectionTestUtils.checkConnection(application)

        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {

            override fun getDeviceInfo(): DeviceInfo {
                return DeviceInfo(
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
                        true
                )
            }
        })

        Emarsys.setup(baseConfig)

        DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler().post {

            getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_STATE.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.CONTACT_FIELD_VALUE.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.CONTACT_TOKEN.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.PUSH_TOKEN.key).remove()

            getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.EVENT_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.ME_V2_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.INBOX_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key).remove()
            getDependency<StringStorage>(PredictStorageKey.PREDICT_SERVICE_URL.key).remove()
        }

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
