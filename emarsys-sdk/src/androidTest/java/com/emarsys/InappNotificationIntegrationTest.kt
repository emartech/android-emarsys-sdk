package com.emarsys

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.util.FileDownloader
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.mobileengage.iam.InAppPresenter
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.whenever
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
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
                .inAppEventHandler(mock(EventHandler::class.java))
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        FeatureTestUtils.resetFeatures()

        mockInappPresenter = mock(InAppPresenter::class.java)

        doAnswer {
            completionListenerLatch.countDown()
        }.`when`(mockInappPresenter).present(any(String::class.java), isNull(), any(String::class.java), isNull(), any(Long::class.java), any(String::class.java), isNull())

        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getClientServiceProvider(): ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java).apply {
                whenever(provideEndpointHost()).thenReturn(Endpoint.ME_V3_CLIENT_HOST)
            }

            override fun getEventServiceProvider(): ServiceEndpointProvider = mock(ServiceEndpointProvider::class.java).apply {
                whenever(provideEndpointHost()).thenReturn(Endpoint.ME_V3_EVENT_HOST)
            }

            override fun getInAppPresenter() = mockInappPresenter
        })

        ConnectionTestUtils.checkConnection(application)

        Emarsys.setup(baseConfig)

        DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.set(null)
        DependencyInjection.getContainer<MobileEngageDependencyContainer>().pushTokenStorage.remove()

        IntegrationTestUtils.doLogin()
    }

    @After
    fun tearDown() {
        try {
            FeatureTestUtils.resetFeatures()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.set(null)
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testInappPresent() {
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