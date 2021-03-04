package com.emarsys.mobileengage.iam.jsbridge

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.test.rule.ActivityTestRule
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.mobileengage.iam.model.InAppMessage
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.ThreadSpy
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch

class JSCommandFactoryTest {
    private companion object {
        const val PROPERTY = "testProperty"
        const val TIMESTAMP = 1233L
        const val CAMPAIGN_ID = "campaignId"
        const val SID = "sid"
        const val URL = "url"
        const val TEST_URL = "https://emarsys.com"

    }

    private lateinit var jsCommandFactory: JSCommandFactory
    private lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    private lateinit var uiHandler: Handler
    private lateinit var coreSdkHandler: CoreSdkHandler
    private lateinit var mockInAppInternal: InAppInternal
    private lateinit var mockButtonClickedRepository: Repository<ButtonClicked, SqlSpecification>
    private lateinit var mockOnCloseListener: OnCloseListener
    private lateinit var mockOnAppEventListener: OnAppEventListener
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockActivity: Activity

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setUp() {
        mockActivity = mock()
        mockCurrentActivityProvider = mock() {
            on { get() } doReturn mockActivity
        }
        uiHandler = Handler(Looper.getMainLooper())
        coreSdkHandler = CoreSdkHandlerProvider().provideHandler()
        mockInAppInternal = mock()
        mockButtonClickedRepository = mock()
        mockOnCloseListener = mock()
        mockOnAppEventListener = mock()
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn TIMESTAMP
        }
        jsCommandFactory = JSCommandFactory(mockCurrentActivityProvider, uiHandler, coreSdkHandler, mockInAppInternal, mockButtonClickedRepository, mockOnCloseListener, mockOnAppEventListener, mockTimestampProvider)
    }

    @Test
    fun testCreate_shouldTriggerCloseCommand_onMainThread() {

        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockOnCloseListener.invoke()).doAnswer(threadSpy)

        jsCommandFactory.create(JSCommandFactory.CommandType.ON_CLOSE).invoke(null, JSONObject())

        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testCreate_shouldTriggerOnAppEventCommand_onMainThread() {
        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        val expectedJson = JSONObject(mapOf("testKey" to "testValue"))
        whenever(mockOnAppEventListener.invoke(anyOrNull(), any())).doAnswer(threadSpy)

        jsCommandFactory.create(JSCommandFactory.CommandType.ON_APP_EVENT).invoke("test", expectedJson)

        threadSpy.verifyCalledOnMainThread()
        verify(mockOnAppEventListener).invoke("test", expectedJson)
    }

    @Test
    fun testCreate_shouldTriggerOnButtonClickedCommand_onCoreSDKThread() {
        val inAppMessage = InAppMessage(CAMPAIGN_ID, SID, URL)
        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)
        coreSdkHandler.post { latch1.await() }
        jsCommandFactory.create(JSCommandFactory.CommandType.ON_BUTTON_CLICKED, inAppMessage).invoke(PROPERTY, JSONObject(mapOf("key" to "value")))
        verifyZeroInteractions(mockButtonClickedRepository)
        verifyZeroInteractions(mockInAppInternal)
        latch1.countDown()
        coreSdkHandler.post { latch2.countDown() }
        latch2.await()
        verify(mockButtonClickedRepository).add(ButtonClicked(inAppMessage.campaignId, PROPERTY, TIMESTAMP))
        val expectedEventName = "inapp:click"
        val attributes = mapOf(
                "campaignId" to CAMPAIGN_ID,
                "buttonId" to PROPERTY,
                "sid" to SID,
                "url" to URL)

        verify(mockInAppInternal).trackInternalCustomEvent(expectedEventName, attributes, null)
    }

    @Test
    fun testCreate_shouldTriggerInAPpInternalWithAttributes_whenSIDANDURL_areNulls() {
        val inAppMessage = InAppMessage(CAMPAIGN_ID, null, null)
        val latch = CountDownLatch(1)
        jsCommandFactory.create(JSCommandFactory.CommandType.ON_BUTTON_CLICKED, inAppMessage).invoke(PROPERTY, JSONObject())

        coreSdkHandler.post { latch.countDown() }
        latch.await()
        verify(mockButtonClickedRepository).add(ButtonClicked(inAppMessage.campaignId, PROPERTY, TIMESTAMP))
        val expectedEventName = "inapp:click"
        val attributes = mapOf(
                "campaignId" to CAMPAIGN_ID,
                "buttonId" to PROPERTY)

        verify(mockInAppInternal).trackInternalCustomEvent(expectedEventName, attributes, null)
    }

    @Test
    fun testCreate_OpenExternalUrl_shouldTriggerStartActivityOnMainThread_whenActivityIsNotNullAndActivityCanBeResolved() {
        val property = TEST_URL
        val threadSpy = ThreadSpy<Any>()

        whenever(mockActivity.startActivity(any())).doAnswer(threadSpy)

        jsCommandFactory.create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)
                .invoke(property, JSONObject())

        verify(mockCurrentActivityProvider).get()

        verify(mockActivity).startActivity(any())
        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testCreate_OpenExternalUrl_shouldThrowException_whenActivityIsNotNullAndActivityCanNotBeResolved() {
        val property = TEST_URL

        whenever(mockActivity.startActivity(any())).thenAnswer {
            throw Exception()
        }

        val openExternalUrlListener = jsCommandFactory.create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)

        val exception = shouldThrow<Exception> {
            openExternalUrlListener.invoke(property, JSONObject())
        }
        exception.message shouldBe "Url cannot be handled by any application!"
    }

    @Test
    fun testCreate_OpenExternalUrl_shouldThrowException_whenActivityIsNull() {
        val property = TEST_URL
        whenever(mockCurrentActivityProvider.get()) doReturn null

        val openExternalUrlListener = jsCommandFactory.create(JSCommandFactory.CommandType.ON_OPEN_EXTERNAL_URL)

        val exception = shouldThrow<Exception> {
            openExternalUrlListener.invoke(property, JSONObject())
        }
        exception.message shouldBe "UI unavailable!"
    }

    @Test
    fun testCreate_MEEvent_shouldCallTrackCustomEventAsync_onCoreSDKThread() {
        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)

        coreSdkHandler.post { latch1.await() }
        val meEventListener = jsCommandFactory.create(JSCommandFactory.CommandType.ON_ME_EVENT)
        val property = "testProperty"
        meEventListener.invoke(property, JSONObject(mapOf("key" to "value", "payload" to mapOf("payloadKey" to "payloadValue"))))

        verifyZeroInteractions(mockInAppInternal)
        latch1.countDown()
        coreSdkHandler.post { latch2.countDown() }
        latch2.await()

        verify(mockInAppInternal).trackCustomEventAsync(property, mapOf("payloadKey" to "payloadValue"), null)
    }
}