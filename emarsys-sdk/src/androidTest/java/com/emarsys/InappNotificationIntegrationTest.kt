package com.emarsys

import android.app.Application
import android.content.Intent
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.util.FileUtils
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.mobileengage.iam.InAppPresenter
import com.emarsys.mobileengage.service.IntentUtils
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
        }.`when`(mockInappPresenter).present(any(String::class.java), isNull(), any(Long::class.java), any(String::class.java), isNull())

        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getInAppPresenter() = mockInappPresenter
        })

        ConnectionTestUtils.checkConnection(application)

        Emarsys.setup(baseConfig)

        IntegrationTestUtils.doLogin()
    }

    @After
    fun tearDown() {
        try {
            FeatureTestUtils.resetFeatures()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testInappPresent() {
        val context = InstrumentationRegistry.getTargetContext().getApplicationContext()
        val url = FileUtils.download(context, "https://www.emarsys.com")
        val emsPayload = "{\"inapp\": {\"campaignId\": \"222\",\"url\": \"https://www.emarsys.com\",\"fileUrl\": \"$url\"}}"
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