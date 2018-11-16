package com.emarsys

import android.app.Application
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.api.result.Try
import com.emarsys.core.di.DependencyInjection
import com.emarsys.di.EmarysDependencyContainer
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature.USER_CENTRIC_INBOX
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.mobileengage.storage.AppLoginStorage
import com.emarsys.mobileengage.storage.MeIdStorage
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.ExperimentalTestUtils
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.fake.FakeActivity
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.isAccessible

class InboxV2IntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val APP_PASSWORD = "PaNkfOD90AVpYimMBuZopCpm8OWCrREu"
        private const val CONTACT_FIELD_ID = 3
        private const val MERCHANT_ID = "1428C8EE286EC34B"
    }

    private lateinit var latch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var triedNotificationInboxStatus: Try<NotificationInboxStatus>
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
        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageCredentials(APP_ID, APP_PASSWORD)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .enableExperimentalFeatures(USER_CENTRIC_INBOX)
                .build()

        errorCause = null
        latch = CountDownLatch(1)

        ConnectionTestUtils.checkConnection(application)
        MeIdStorage(application).remove()
        AppLoginStorage(application).remove()

        ExperimentalTestUtils.resetExperimentalFeatures()

        Emarsys.setup(baseConfig)
    }

    @After
    fun tearDown() {
        ExperimentalTestUtils.resetExperimentalFeatures()

        with(DependencyInjection.getContainer<EmarysDependencyContainer>()) {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleWatchdog)
            application.unregisterActivityLifecycleCallbacks(currentActivityWatchdog)
            coreSdkHandler.looper.quit()
        }

        MeIdStorage(application).remove()
        AppLoginStorage(application).remove()

        DependencyInjection.tearDown()
    }

    @Test
    fun testFetchNotifications() {
        IntegrationTestUtils.doAppLogin()

        Emarsys.Inbox.fetchNotifications(eventuallyStoreResultInProperty(this::triedNotificationInboxStatus.setter)).eventuallyAssert {
            triedNotificationInboxStatus.errorCause shouldBe null
            triedNotificationInboxStatus.result shouldNotBe null
        }
    }

    @Test
    fun testResetBadgeCount() {
        IntegrationTestUtils.doAppLogin()

        Emarsys.Inbox.resetBadgeCount(eventuallyStoreResultInProperty(this::errorCause.setter)).eventuallyAssert {
            errorCause shouldBe null
        }
    }

    @Test
    fun testTrackNotificationOpen() {
        IntegrationTestUtils.doAppLogin()

        val notification = Notification(
                "id",
                "161e_D/1UiO/jCmE4",
                "title",
                null,
                emptyMap(),
                JSONObject(),
                2000,
                Date().time)

        Emarsys.Inbox.trackNotificationOpen(notification, eventuallyStoreResultInProperty(this::errorCause.setter)).eventuallyAssert {
            errorCause shouldBe null
        }
    }

    private fun <T> eventuallyStoreResultInProperty(setter: KMutableProperty0.Setter<T>): (T)-> Unit{
        return {
            setter.isAccessible = true
            setter(it)
            latch.countDown()
        }
    }

    private fun Any.eventuallyAssert(assertion: ()->Unit) {
        latch.await()
        assertion.invoke()
    }
}
