package com.emarsys

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.api.result.Try
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.mobileengage.api.inbox.InboxResult
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.whenever
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
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

class DefaultInboxIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val CONTACT_FIELD_ID = 3
        private const val SDK_VERSION = "2.1.0-integration"
        private const val LANGUAGE = "en-US"

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

    private lateinit var latch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var triedNotificationInboxStatus: Try<NotificationInboxStatus>
    private lateinit var triedInboxResult: Try<InboxResult>
    private lateinit var sharedPreferences: SharedPreferences

    private var errorCause: Throwable? = null

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(FakeActivity::class.java)

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

        ConnectionTestUtils.checkConnection(application)

        sharedPreferences = application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)

        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getClientServiceProvider(): ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java).apply {
                whenever(provideEndpointHost()).thenReturn(Endpoint.ME_V3_CLIENT_HOST)
            }

            override fun getEventServiceProvider(): ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java).apply {
                whenever(provideEndpointHost()).thenReturn(Endpoint.ME_V3_EVENT_HOST)
            }

            override fun getDeviceInfo() = DeviceInfo(
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
        })

        DependencyInjection.getContainer<MobileEngageDependencyContainer>().clientStateStorage.remove()
        DependencyInjection.getContainer<MobileEngageDependencyContainer>().contactFieldValueStorage.remove()
        DependencyInjection.getContainer<MobileEngageDependencyContainer>().contactTokenStorage.remove()
        DependencyInjection.getContainer<MobileEngageDependencyContainer>().deviceInfoHashStorage.remove()
        DependencyInjection.getContainer<MobileEngageDependencyContainer>().pushTokenStorage.remove()

        Emarsys.setup(baseConfig)

        DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.remove()
        DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.remove()

        IntegrationTestUtils.doLogin()

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
            DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.remove()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
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
        Emarsys.messageInbox.addTag("TEST_TAG", "testMessageId", eventuallyStoreResultInProperty(this::errorCause.setter)).eventuallyAssert {
            errorCause shouldBe null
        }
    }

    @Test
    fun testRemoveTag() {
        Emarsys.messageInbox.removeTag("TEST_TAG", "testMessageId", eventuallyStoreResultInProperty(this::errorCause.setter)).eventuallyAssert {
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
