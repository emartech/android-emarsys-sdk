package com.emarsys.mobileengage.inbox

import android.app.Application
import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Registry
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.worker.Worker
import com.emarsys.mobileengage.RequestContext
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.mobileengage.fake.FakeInboxResultListener
import com.emarsys.mobileengage.fake.FakeInboxResultListener.Mode
import com.emarsys.mobileengage.fake.FakeResetBadgeCountResultListener
import com.emarsys.mobileengage.fake.FakeRestClient
import com.emarsys.mobileengage.inbox.model.NotificationCache
import com.emarsys.mobileengage.request.RequestModelFactory
import com.emarsys.mobileengage.util.RequestHeaderUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.SharedPrefsUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.*
import java.util.concurrent.CountDownLatch

class DefaultInboxInternalTest {

    private companion object {

        private const val HARDWARE_ID = "hwid"
        private const val APPLICATION_ID = "id"
        private const val CONTACT_FIELD_ID = 1
        private const val REQUEST_ID = "REQUEST_ID"
        private const val TIMESTAMP: Long = 100000
    }

    private lateinit var mockResultListener: ResultListener<Try<NotificationInboxStatus>>
    private lateinit var mockResetListener: CompletionListener
    private lateinit var defaultHeaders: Map<String, String>
    private lateinit var mockRequestManager: RequestManager
    private lateinit var latch: CountDownLatch
    private lateinit var inbox: DefaultInboxInternal

    private lateinit var application: Application
    private lateinit var cache: NotificationCache
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var requestContext: RequestContext
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var mockContactFieldValueStorage: Storage<String>
    private lateinit var mockRequestModelFactory: RequestModelFactory
    private lateinit var notificationList: List<Notification>

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        latch = CountDownLatch(1)

        application = InstrumentationRegistry.getTargetContext().applicationContext as Application

        mockRequestManager = mock(RequestManager::class.java)

        notificationList = createNotificationList()
        mockHardwareIdProvider = mock(HardwareIdProvider::class.java)
        mockLanguageProvider = mock(LanguageProvider::class.java)

        whenever(mockHardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID)
        deviceInfo = DeviceInfo(application, mockHardwareIdProvider, mock(VersionProvider::class.java), mockLanguageProvider, mock(NotificationManagerHelper::class.java), true)

        mockUuidProvider = mock(UUIDProvider::class.java)
        whenever(mockUuidProvider.provideId()).thenReturn(REQUEST_ID)

        mockTimestampProvider = mock(TimestampProvider::class.java)
        whenever(mockTimestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        mockContactFieldValueStorage = mock(Storage::class.java) as Storage<String>
        whenever(mockContactFieldValueStorage.get()).thenReturn("test@test.com")
        mockRequestModelFactory = mock(RequestModelFactory::class.java).apply {
            whenever(createResetBadgeCountRequest()).thenReturn(mock(RequestModel::class.java))
            whenever(createFetchNotificationsRequest()).thenReturn(mock(RequestModel::class.java))
        }
        requestContext = RequestContext(
                APPLICATION_ID,
                CONTACT_FIELD_ID,
                deviceInfo,
                mockTimestampProvider,
                mockUuidProvider,
                mock(Storage::class.java) as Storage<String>,
                mock(Storage::class.java) as Storage<String>,
                mock(Storage::class.java) as Storage<String>,
                mockContactFieldValueStorage
        )

        defaultHeaders = RequestHeaderUtils.createDefaultHeaders(requestContext)

        inbox = DefaultInboxInternal(mockRequestManager, requestContext, mockRequestModelFactory)

        mockResultListener = mock(ResultListener::class.java) as ResultListener<Try<NotificationInboxStatus>>
        mockResetListener = mock(CompletionListener::class.java)

        val cacheField = NotificationCache::class.java.getDeclaredField("internalCache")
        cacheField.isAccessible = true
        (cacheField.get(null) as MutableList<*>).clear()

        cache = NotificationCache()
    }

    @After
    fun tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_shouldNotBeNull() {
        DefaultInboxInternal(null, requestContext, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_shouldNotBeNull() {
        DefaultInboxInternal(mockRequestManager, null, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelFactory_mustNotBeNull() {
        DefaultInboxInternal(mockRequestManager, requestContext, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFetchNotifications_listenerShouldNotBeNull() {
        inbox.fetchNotifications(null)
    }

    @Test
    fun testFetchNotifications_shouldMakeRequest_viaRequestManager_submitNow() {
        inbox.fetchNotifications(mockResultListener)

        verify(mockRequestModelFactory).createFetchNotificationsRequest()
        verify(mockRequestManager).submitNow(any(), any(CoreCompletionHandler::class.java))

    }

    @Test
    fun testFetchNotifications_listener_success() {
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeInboxResultListener(latch)
        inbox.fetchNotifications(listener)

        latch.await()

        listener.resultStatus shouldBe NotificationInboxStatus(notificationList, 300)
        listener.successCount shouldBe 1
    }

    @Test
    fun testFetchNotifications_listener_success_shouldBeCalledOnMainThread() {
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeInboxResultListener(latch, Mode.MAIN_THREAD)
        inbox.fetchNotifications(listener)

        latch.await()

        listener.successCount shouldBe 1
    }

    @Test
    fun testFetchNotifications_listener_success_withCachedNotifications() {
        val cachedNotifications = createCacheList()

        cachedNotifications.asReversed().forEach {
            cache.cache(it)
        }

        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeInboxResultListener(latch, Mode.MAIN_THREAD)
        inbox.fetchNotifications(listener)

        latch.await()

        val result = listener.resultStatus.notifications

        val expected = ArrayList(cachedNotifications)
        expected.addAll(createNotificationList())

        result shouldBe expected
    }

    @Test
    fun testFetchNotifications_listener_failureWithException() {
        val expectedException = Exception("FakeRestClientException")
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(expectedException)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeInboxResultListener(latch)
        inbox.fetchNotifications(listener)

        latch.await()

        listener.errorCause shouldBe expectedException
        listener.errorCount shouldBe 1
    }

    @Test

    fun testFetchNotifications_listener_failureWithException_shouldBeCalledOnMainThread() {
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(Exception())),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeInboxResultListener(latch, Mode.MAIN_THREAD)
        inbox.fetchNotifications(listener)

        latch.await()

        listener.errorCount shouldBe 1
    }

    @Test
    fun testFetchNotification_listener_failureWithResponseModel() {
        val responseModel = ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel::class.java))
                .build()

        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeInboxResultListener(latch)
        inbox.fetchNotifications(listener)

        latch.await()

        val expectedException = ResponseErrorException(
                responseModel.statusCode,
                responseModel.message,
                responseModel.body)

        val resultException = listener.errorCause as ResponseErrorException

        resultException.statusCode shouldBe expectedException.statusCode
        resultException.message shouldBe expectedException.message
        resultException.body shouldBe expectedException.body
        listener.errorCount shouldBe 1
    }

    @Test
    fun testFetchNotification_listener_failureWithResponseModel_shouldBeCalledOnMainThread() {
        val responseModel = ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel::class.java))
                .build()
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeInboxResultListener(latch, Mode.MAIN_THREAD)
        inbox.fetchNotifications(listener)

        latch.await()

        listener.errorCount shouldBe 1
    }

    @Test
    fun testFetchNotification_listener_failureWithParametersNotSet() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val listener = FakeInboxResultListener(latch)
        inbox.fetchNotifications(listener)

        latch.await()

        listener.errorCause.javaClass shouldBe NotificationInboxException::class.java
        listener.errorCount shouldBe 1
    }

    @Test
    fun testFetchNotification_listener_failureWithParametersNotSet_shouldBeCalledOnMainThread() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val listener = FakeInboxResultListener(latch, Mode.MAIN_THREAD)
        inbox.fetchNotifications(listener)

        latch.await()

        listener.errorCount shouldBe 1
    }

    @Test
    fun testFetchNotification_listener_failureWithParametersSet_butLacksCredentials() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val listener = FakeInboxResultListener(latch)
        inbox.fetchNotifications(listener)

        latch.await()


        listener.errorCause.javaClass shouldBe NotificationInboxException::class.java
        listener.errorCount shouldBe 1
    }

    @Test

    fun testFetchNotification_listener_failureWithParametersSet_butLacksCredentials_shouldBeCalledOnMainThread() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val listener = FakeInboxResultListener(latch, Mode.MAIN_THREAD)
        inbox.fetchNotifications(listener)

        latch.await()

        listener.errorCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_shouldMakeRequest_viaRequestManager_submitNow() {
        inbox.resetBadgeCount(mockResetListener)

        verify(mockRequestModelFactory).createResetBadgeCountRequest()
        verify(mockRequestManager).submitNow(any(), any(CoreCompletionHandler::class.java))
    }

    @Test
    fun testResetBadgeCount_listener_success() {
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeResetBadgeCountResultListener(latch)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.successCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_listener_success_shouldBeCalledOnMainThread() {
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.successCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_listener_failureWithException() {
        val expectedException = Exception("FakeRestClientException")
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(expectedException)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeResetBadgeCountResultListener(latch)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.errorCause shouldBe expectedException
        listener.errorCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_listener_failureWithException_shouldBeCalledOnMainThread() {
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(Exception())),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.errorCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_listener_failureWithResponseModel() {
        val responseModel = ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel::class.java))
                .build()

        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeResetBadgeCountResultListener(latch)
        inbox.resetBadgeCount(listener)

        latch.await()

        val expectedException = ResponseErrorException(
                responseModel.statusCode,
                responseModel.message,
                responseModel.body)

        val resultException = listener.errorCause as ResponseErrorException

        resultException.statusCode shouldBe expectedException.statusCode
        resultException.message shouldBe expectedException.message
        resultException.body shouldBe expectedException.body
        listener.errorCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_listener_failureWithResponseModel_shouldBeCalledOnMainThread() {
        val responseModel = ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel::class.java))
                .build()
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext,
                mockRequestModelFactory
        )

        val listener = FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.errorCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_listener_failureWithParametersNotSet() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val listener = FakeResetBadgeCountResultListener(latch)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.errorCause.javaClass shouldBe NotificationInboxException::class.java
        listener.errorCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_listener_failureWithParametersNotSet_shouldBeCalledOnMainThread() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val listener = FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.errorCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_listener_failureWithParametersSet_butLacksCredentials() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val listener = FakeResetBadgeCountResultListener(latch)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.errorCause.javaClass shouldBe NotificationInboxException::class.java
        listener.errorCount shouldBe 1
    }

    @Test

    fun testResetBadgeCount_listener_failureWithParametersSet_butLacksCredentials_shouldBeCalledOnMainThread() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        val listener = FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD)
        inbox.resetBadgeCount(listener)

        latch.await()

        listener.errorCount shouldBe 1
    }

    @Test
    fun testResetBadgeCount_shouldNotFail_withNullListener_success() {
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext,
                mockRequestModelFactory
        )

        try {
            inbox.resetBadgeCount(null)
            Thread.sleep(150)
        } catch (e: Exception) {
            Assert.fail("Should not throw exception!")
        }

    }

    @Test
    fun testResetBadgeCount_shouldNotFail_withNullListener_failureWithException() {
        val expectedException = Exception("FakeRestClientException")
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(expectedException)),
                requestContext,
                mockRequestModelFactory
        )

        try {
            inbox.resetBadgeCount(null)
            Thread.sleep(150)
        } catch (e: Exception) {
            Assert.fail("Should not throw exception!")
        }

    }

    @Test
    fun testResetBadgeCount_shouldNotFail_withNullListener_failureWithResponseModel() {
        val responseModel = ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel::class.java))
                .build()
        inbox = DefaultInboxInternal(
                requestManagerWithRestClient(FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext,
                mockRequestModelFactory
        )

        try {
            inbox.resetBadgeCount(null)
            Thread.sleep(150)
        } catch (e: Exception) {
            Assert.fail("Should not throw exception!")
        }

    }

    @Test
    fun testResetBadgeCount_shouldNotFail_withNullListener_failureWithParametersNotSet() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        try {
            inbox.resetBadgeCount(null)
            Thread.sleep(150)
        } catch (e: Exception) {
            Assert.fail("Should not throw exception!")
        }
    }

    @Test
    fun testResetBadgeCount_shouldNotFail_withNullListener_failureWithParametersSet_butLacksCredentials() {
        whenever(mockContactFieldValueStorage.get()).thenReturn(null)

        try {
            inbox.resetBadgeCount(null)
            Thread.sleep(150)
        } catch (e: Exception) {
            Assert.fail("Should not throw exception!")
        }

    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackNotificationOpen_notification_mustNotBeNull() {
        inbox.trackNotificationOpen(null, mock(CompletionListener::class.java))
    }

    @Test
    fun testTrackNotificationOpen() {

        val message = Notification("id1", "sid1", "title", null, HashMap(), JSONObject(), 7200, Date().time)

        inbox.trackNotificationOpen(message, null)

        verify(mockRequestModelFactory).createTrackNotificationOpenRequest("sid1")
        verify(mockRequestManager).submit(any(), isNull<CompletionListener>())

    }

    @Test
    fun testTrackNotificationOpen_withCorrectCompletionListener() {
        val completionListener = mock(CompletionListener::class.java)
        val message = Notification("id1", "sid1", "title", null, HashMap(), JSONObject(), 7200, Date().time)

        inbox.trackNotificationOpen(message, completionListener)

        verify(mockRequestModelFactory).createTrackNotificationOpenRequest("sid1")
        verify(mockRequestManager).submit(any(), eq(completionListener))
    }

    private fun createNotificationList(): List<Notification> {
        val customData1 = HashMap<String, String>()
        customData1["data1"] = "dataValue1"
        customData1["data2"] = "dataValue2"

        val rootParams1 = JSONObject()
        rootParams1.put("param1", "paramValue1")
        rootParams1.put("param2", "paramValue2")

        val customData2 = HashMap<String, String>()
        customData2["data3"] = "dataValue3"
        customData2["data4"] = "dataValue4"

        val rootParams2 = JSONObject()
        rootParams2.put("param3", "paramValue3")
        rootParams2.put("param4", "paramValue4")


        val customData3 = HashMap<String, String>()
        customData3["data5"] = "dataValue5"
        customData3["data6"] = "dataValue6"

        val rootParams3 = JSONObject()
        rootParams3.put("param5", "paramValue5")
        rootParams3.put("param6", "paramValue6")

        return Arrays.asList(
                Notification("id1", "sid1", "title1", null, customData1, rootParams1, 300, 10000000),
                Notification("id2", "sid2", "title2", null, customData2, rootParams2, 200, 30000000),
                Notification("id3", "sid3", "title3", null, customData3, rootParams3, 100, 25000000)

        )
    }

    private fun createCacheList(): List<Notification> {
        val customData4 = HashMap<String, String>()
        customData4["data7"] = "dataValue7"
        customData4["data8"] = "dataValue8"

        val rootParams4 = JSONObject()
        rootParams4.put("param7", "paramValue7")
        rootParams4.put("param8", "paramValue8")

        val customData5 = HashMap<String, String>()
        customData5["data9"] = "dataValue9"
        customData5["data10"] = "dataValue10"

        val rootParams5 = JSONObject()
        rootParams5.put("param9", "paramValue9")
        rootParams5.put("param10", "paramValue10")

        return Arrays.asList(
                Notification("id4", "sid4", "title4", null, customData4, rootParams4, 400, 40000000),
                Notification("id5", "sid5", "title5", null, customData5, rootParams5, 500, 50000000)
        )
    }

    private fun createSuccessResponse(): ResponseModel {
        val notificationString1 = """
        {
            "id":"id1",
            "sid":"sid1",
            "title":"title1",
            "custom_data": {
                "data1":"dataValue1",
                "data2":"dataValue2"
            },
            "root_params": {
                "param1":"paramValue1",
                "param2":"paramValue2"
            },
            "expiration_time":300,
            "received_at":10000000
        }"""

        val notificationString2 = """
            {
                "id":"id2",
                "sid":"sid2",
                "title":"title2",
                "custom_data":{
                    "data3":"dataValue3",
                    "data4":"dataValue4"
                },
                "root_params": {
                    "param3":"paramValue3",
                    "param4":"paramValue4"
                },
                "expiration_time": 200,
                "received_at":30000000
            }"""

        val notificationString3 = """{
                "id":"id3",
                "sid":"sid3",
                "title":"title3",
                "custom_data": {
                    "data5":"dataValue5",
                    "data6":"dataValue6"
                },
                "root_params": {
                    "param5":"paramValue5",
                    "param6":"paramValue6"
                },
                "expiration_time": 100,
                "received_at":25000000
                }"""

        val json = """{"badge_count": 300, "notifications": [$notificationString1,$notificationString2,$notificationString3]}"""

        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(json)
                .requestModel(mock(RequestModel::class.java))
                .build()
    }

    @Suppress("UNCHECKED_CAST")
    private fun requestManagerWithRestClient(restClient: RestClient): RequestManager {
        return RequestManager(
                mock(Handler::class.java),
                mock(Repository::class.java) as Repository<RequestModel, SqlSpecification>,
                mock(Repository::class.java) as Repository<ShardModel, SqlSpecification>,
                mock(Worker::class.java),
                restClient,
                mock(Registry::class.java) as Registry<RequestModel, CompletionListener>,
                mock(CoreCompletionHandler::class.java)
        )
    }

}