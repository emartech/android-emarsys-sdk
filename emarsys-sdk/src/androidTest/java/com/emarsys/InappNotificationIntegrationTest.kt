package com.emarsys

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.di.Container
import com.emarsys.core.di.Container.getDependency
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.util.FileDownloader
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.mobileengage.iam.InAppPresenter
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.predict.storage.PredictStorageKey
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.*
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch

class InappNotificationIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val CONTACT_FIELD_ID = 3

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

    private lateinit var completionListenerLatch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var mockInappPresenter: InAppPresenter

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
        completionListenerLatch = CountDownLatch(1)

        DatabaseTestUtils.deleteCoreDatabase()

        application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .inAppEventHandler(mock())
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        FeatureTestUtils.resetFeatures()

        mockInappPresenter = mock()

        whenever(mockInappPresenter.present(
                anyOrNull(),
                eq(null),
                anyOrNull(),
                eq(null),
                anyOrNull(),
                anyOrNull(),
                eq(null))).thenAnswer { completionListenerLatch.countDown() }

        val setupLatch = CountDownLatch(1)
        DependencyInjection.setup(DefaultEmarsysDependencyContainer(baseConfig, Runnable {
            setupLatch.countDown()
        }))

        setupLatch.await()

        ConnectionTestUtils.checkConnection(application)

        Emarsys.setup(baseConfig)
        getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.EVENT_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.ME_V2_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.INBOX_SERVICE_URL.key).remove()
        getDependency<StringStorage>(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key).remove()
        getDependency<StringStorage>(PredictStorageKey.PREDICT_SERVICE_URL.key).remove()
        IntegrationTestUtils.doLogin()
    }

    @After
    fun tearDown() {
        try {
            getDependency<Handler>("coreSdkHandler").looper.quit()
            application.unregisterActivityLifecycleCallbacks(getDependency<ActivityLifecycleWatchdog>())
            application.unregisterActivityLifecycleCallbacks(getDependency<CurrentActivityWatchdog>())

            FeatureTestUtils.resetFeatures()
            getDependency<StringStorage>(MobileEngageStorageKey.CLIENT_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.EVENT_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.DEEPLINK_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.ME_V2_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.INBOX_SERVICE_URL.key).remove()
            getDependency<StringStorage>(MobileEngageStorageKey.MESSAGE_INBOX_SERVICE_URL.key).remove()
            getDependency<StringStorage>(PredictStorageKey.PREDICT_SERVICE_URL.key).remove()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testInappPresent() {
        Container.addDependency(mockInappPresenter)

        val context = InstrumentationRegistry.getTargetContext().applicationContext
        val url = FileDownloader(context).download("https://www.google.com")
        val emsPayload = """{"inapp": {"campaignId": "222","url": "https://www.google.com","fileUrl": "$url"}}"""
        val remoteMessageData = mapOf("ems" to emsPayload)

        val intent = IntentUtils.createNotificationHandlerServiceIntent(
                context,
                remoteMessageData,
                0,
                null
        )

        context.startService(intent)

        activityRule.launchActivity(Intent())

        completionListenerLatch.await()
    }

}