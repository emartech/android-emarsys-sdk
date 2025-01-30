package com.emarsys.mobileengage.iam.model.requestRepositoryProxy


import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.helper.DbHelper
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.CompositeRequestModel
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.RequestModelRepository
import com.emarsys.core.request.model.specification.QueryLatestRequestModel
import com.emarsys.core.util.TimestampUtils
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.iam.InAppEventHandlerInternal
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.mobileengage.util.RequestPayloadUtils.createCompositeRequestModelPayload
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.RandomTestUtils.randomString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class RequestRepositoryProxyTest  {
    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockRequestModelRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var mockDisplayedIamRepository: Repository<DisplayedIam, SqlSpecification>
    private lateinit var mockButtonClickedRepository: Repository<ButtonClicked, SqlSpecification>
    private lateinit var requestModelRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var displayedIamRepository: Repository<DisplayedIam, SqlSpecification>
    private lateinit var buttonClickedRepository: Repository<ButtonClicked, SqlSpecification>
    private lateinit var timestampProvider: TimestampProvider
    private lateinit var inAppEventHandlerInternal: InAppEventHandlerInternal
    private lateinit var compositeRepository: RequestRepositoryProxy
    private lateinit var uuidProvider: UUIDProvider
    private lateinit var mockEventServiceProvider: ServiceEndpointProvider
    private lateinit var mockRequestModelHelper: RequestModelHelper
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder


    @Before
    fun setUp() {
        deleteCoreDatabase()
        val context = getTargetContext()
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()
        mockRequestContext = mock()
        mockRequestModelRepository = mock()
        mockDisplayedIamRepository = mock()
        mockButtonClickedRepository = mock()
        whenever(mockRequestContext.applicationCode).thenReturn(APPLICATION_CODE)
        val dbHelper: DbHelper = CoreDbHelper(context, HashMap())
        requestModelRepository = RequestModelRepository(dbHelper, concurrentHandlerHolder)
        displayedIamRepository = DisplayedIamRepository(dbHelper, concurrentHandlerHolder)
        buttonClickedRepository = ButtonClickedRepository(dbHelper, concurrentHandlerHolder)
        timestampProvider = mock()
        whenever(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        uuidProvider = mock()
        whenever(uuidProvider.provideId()).thenReturn(REQUEST_ID)
        inAppEventHandlerInternal = mock()

        mockEventServiceProvider = mock {
            on { provideEndpointHost() } doReturn EVENT_HOST
        }
        mockRequestModelHelper = mock {
            on { isCustomEvent(any()) } doReturn true
        }
        compositeRepository = RequestRepositoryProxy(
            mockRequestModelRepository,
            mockDisplayedIamRepository,
            mockButtonClickedRepository,
            timestampProvider,
            uuidProvider,
            inAppEventHandlerInternal,
            mockEventServiceProvider,
            mockRequestModelHelper
        )
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        FeatureRegistry.disableFeature(InnerFeature.EVENT_SERVICE_V4)
    }

    @Test
    fun testAdd_shouldDelegate_toRequestModelRepository() {
        val requestModel: RequestModel = mock()
        runBlocking {
            compositeRepository.add(requestModel)
            verify(mockRequestModelRepository).add(requestModel)
        }
    }

    @Test
    fun testAdd_shouldNotStoreCompositeRequestModels() {
        val requestModel: CompositeRequestModel = mock()
        runBlocking {
            compositeRepository.add(requestModel)
            verifyNoInteractions(mockRequestModelRepository)
        }
    }

    @Test
    fun testRemove_shouldDelegate_toRequestModelRepository() {
        val spec: SqlSpecification = mock()
        runBlocking {
            compositeRepository.remove(spec)
            verify(mockRequestModelRepository).remove(spec)
        }
    }

    @Test
    fun testIsEmpty_whenEmpty_shouldDelegate_toRequestModelRepository() {
        whenever(mockRequestModelRepository.isEmpty()).thenReturn(true)
        compositeRepository.isEmpty() shouldBe true
        verify(mockRequestModelRepository).isEmpty()
    }

    @Test
    fun testIsEmpty_whenNotEmpty_shouldDelegate_toRequestModelRepository() {
        whenever(mockRequestModelRepository.isEmpty()).thenReturn(false)
        compositeRepository.isEmpty() shouldBe false
        verify(mockRequestModelRepository).isEmpty()
    }

    @Test
    fun testQuery_shouldReturnOriginalQuery_whenThereAreNoCustomEvents() {
        whenever(mockRequestModelHelper.isCustomEvent(any())).thenReturn(false)
        compositeRepository = compositeRepositoryWithRealRepositories()
        val firstRequestModel = requestModel()
        runBlocking {
            requestModelRepository.add(firstRequestModel)
            requestModelRepository.add(requestModel())
            requestModelRepository.add(requestModel())
        }
        val expected = listOf(firstRequestModel)
        compositeRepository.query(QueryLatestRequestModel()) shouldBe expected
    }

    @Test
    fun testQuery_shouldReturnOriginalQuery_whenThereISCustomEventInTheFirstButNoCustomEventsInTHeLastRepositoryQuery() {
        val customEvent = customEvent(123, "testEventName")
        val requestModel = requestModel()

        whenever(mockRequestModelRepository.query(any())).doReturnConsecutively(
            listOf(
                listOf(
                    customEvent,
                    requestModel
                ), listOf()
            )
        )

        val expected = listOf(customEvent, requestModel)

        val result = compositeRepository.query(Everything())

        result shouldBe expected
    }

    @Test
    fun testQuery_resultShouldContainCompositeRequestModel_whenResultContainsCustomEvent() {
        runBlocking {
            compositeRepository = compositeRepositoryWithRealRepositories()
            val request1 = requestModel()
            val request2 = requestModel()
            val request3 = requestModel()
            val customEvent1 = customEvent(900, "event1")
            val attributes = HashMap<String, Any>()
            attributes["key1"] = "value1"
            attributes["key2"] = "value2"
            val customEvent2 = customEvent(1000, "event2", attributes)
            val customEvent3 = customEvent(1200, "event3")
            whenever(mockRequestModelHelper.isCustomEvent(customEvent1)).thenReturn(true)
            whenever(mockRequestModelHelper.isCustomEvent(customEvent2)).thenReturn(true)
            whenever(mockRequestModelHelper.isCustomEvent(customEvent3)).thenReturn(true)
            whenever(mockRequestModelHelper.isCustomEvent(request1)).thenReturn(false)
            whenever(mockRequestModelHelper.isCustomEvent(request2)).thenReturn(false)
            whenever(mockRequestModelHelper.isCustomEvent(request3)).thenReturn(false)
            requestModelRepository.add(request1)
            requestModelRepository.add(request2)
            requestModelRepository.add(customEvent1)
            requestModelRepository.add(customEvent2)
            requestModelRepository.add(request3)
            requestModelRepository.add(customEvent3)
            val event1: MutableMap<String, Any> = HashMap()
            event1["type"] = "custom"
            event1["name"] = "event1"
            event1["timestamp"] = TimestampUtils.formatTimestampWithUTC(900)
            val event2: MutableMap<String, Any> = HashMap()
            event2["type"] = "custom"
            event2["name"] = "event2"
            event2["timestamp"] = TimestampUtils.formatTimestampWithUTC(1000)
            event2["attributes"] = object : HashMap<String?, String?>() {
                init {
                    put("key1", "value1")
                    put("key2", "value2")
                }
            }
            val event3: MutableMap<String, Any> = HashMap()
            event3["type"] = "custom"
            event3["name"] = "event3"
            event3["timestamp"] = TimestampUtils.formatTimestampWithUTC(1200)
            val payload: Map<String, Any?> = createCompositeRequestModelPayload(
                listOf<Map<String, Any>>(event1, event2, event3), emptyList(), emptyList(),
                false
            )
            val expectedComposite: RequestModel = CompositeRequestModel(
                REQUEST_ID,
                "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events",
                RequestMethod.POST,
                payload,
                customEvent1.headers,
                TIMESTAMP,
                Long.MAX_VALUE,
                arrayOf(customEvent1.id, customEvent2.id, customEvent3.id)
            )
            val expected = listOf(
                request1,
                request2,
                expectedComposite,
                request3
            )
            compositeRepository.query(Everything()) shouldBe expected
        }
    }

    @Test
    fun testQuery_resultShouldContainClicksAndDisplays_withSingleCustomEvent() {
        runBlocking {
            compositeRepository = compositeRepositoryWithRealRepositories()
            val attributes = HashMap<String, Any>()
            attributes["key1"] = "value1"
            attributes["key2"] = "value2"
            val customEvent1 = customEvent(1000, "event1", attributes)
            requestModelRepository.add(customEvent1)
            val buttonClicked1 = ButtonClicked("campaign1", "button1", 200)
            val buttonClicked2 = ButtonClicked("campaign1", "button2", 300)
            val buttonClicked3 = ButtonClicked("campaign2", "button1", 2000)
            buttonClickedRepository.add(buttonClicked1)
            buttonClickedRepository.add(buttonClicked2)
            buttonClickedRepository.add(buttonClicked3)
            val displayedIam1 = DisplayedIam("campaign1", 100)
            val displayedIam2 = DisplayedIam("campaign2", 1500)
            val displayedIam3 = DisplayedIam("campaign3", 30000)
            displayedIamRepository.add(displayedIam1)
            displayedIamRepository.add(displayedIam2)
            displayedIamRepository.add(displayedIam3)
            val eventAttributes = HashMap<String, String>()
            eventAttributes["key1"] = "value1"
            eventAttributes["key2"] = "value2"
            val event1: MutableMap<String, Any> = HashMap()
            event1["type"] = "custom"
            event1["name"] = "event1"
            event1["timestamp"] = TimestampUtils.formatTimestampWithUTC(1000)
            event1["attributes"] = eventAttributes
            val payload: Map<String, Any?> = createCompositeRequestModelPayload(
                listOf<Map<String, Any>>(event1),
                listOf(
                    DisplayedIam("campaign1", 100),
                    DisplayedIam("campaign2", 1500),
                    DisplayedIam("campaign3", 30000)
                ),
                listOf(
                    ButtonClicked("campaign1", "button1", 200),
                    ButtonClicked("campaign1", "button2", 300),
                    ButtonClicked("campaign2", "button1", 2000)
                ),
                false
            )
            val expectedComposite: RequestModel = CompositeRequestModel(
                REQUEST_ID,
                "https://mobile-events.eservice.emarsys.net/v3/apps/" + APPLICATION_CODE + "/client/events",
                RequestMethod.POST,
                payload,
                customEvent1.headers,
                TIMESTAMP, Long.MAX_VALUE, arrayOf(customEvent1.id)
            )
            val expected = listOf(expectedComposite)
            compositeRepository.query(Everything()) shouldBe expected
        }
    }

    @Test
    fun testQuery_resultShouldContainClicksAndDisplays_withMultipleRequests() {
        runBlocking {
            compositeRepository = compositeRepositoryWithRealRepositories()
            val request1 = requestModel()
            val request2 = requestModel()
            val request3 = requestModel()
            val customEvent1 = customEvent(900, "event1")
            val attributes = HashMap<String, Any>()
            attributes["key1"] = "value1"
            attributes["key2"] = "value2"
            val customEvent2 = customEvent(1000, "event2", attributes)
            val customEvent3 = customEvent(1200, "event3")
            whenever(mockRequestModelHelper.isCustomEvent(customEvent1)).thenReturn(true)
            whenever(mockRequestModelHelper.isCustomEvent(customEvent2)).thenReturn(true)
            whenever(mockRequestModelHelper.isCustomEvent(customEvent3)).thenReturn(true)
            whenever(mockRequestModelHelper.isCustomEvent(request1)).thenReturn(false)
            whenever(mockRequestModelHelper.isCustomEvent(request2)).thenReturn(false)
            whenever(mockRequestModelHelper.isCustomEvent(request3)).thenReturn(false)
            requestModelRepository.add(request1)
            requestModelRepository.add(request2)
            requestModelRepository.add(customEvent1)
            requestModelRepository.add(customEvent2)
            requestModelRepository.add(request3)
            requestModelRepository.add(customEvent3)
            val buttonClicked1 = ButtonClicked("campaign1", "button1", 200)
            val buttonClicked2 = ButtonClicked("campaign1", "button2", 300)
            val buttonClicked3 = ButtonClicked("campaign2", "button1", 2000)
            buttonClickedRepository.add(buttonClicked1)
            buttonClickedRepository.add(buttonClicked2)
            buttonClickedRepository.add(buttonClicked3)
            val displayedIam1 = DisplayedIam("campaign1", 100)
            val displayedIam2 = DisplayedIam("campaign2", 1500)
            val displayedIam3 = DisplayedIam("campaign3", 30000)
            displayedIamRepository.add(displayedIam1)
            displayedIamRepository.add(displayedIam2)
            displayedIamRepository.add(displayedIam3)
            val event1: MutableMap<String, Any> = HashMap()
            event1["type"] = "custom"
            event1["name"] = "event1"
            event1["timestamp"] = TimestampUtils.formatTimestampWithUTC(900)
            val event2: MutableMap<String, Any> = HashMap()
            event2["type"] = "custom"
            event2["name"] = "event2"
            event2["timestamp"] = TimestampUtils.formatTimestampWithUTC(1000)
            event2["attributes"] = object : HashMap<String?, String?>() {
                init {
                    put("key1", "value1")
                    put("key2", "value2")
                }
            }
            val event3: MutableMap<String, Any> = HashMap()
            event3["type"] = "custom"
            event3["name"] = "event3"
            event3["timestamp"] = TimestampUtils.formatTimestampWithUTC(1200)
            val payload: Map<String, Any?> = createCompositeRequestModelPayload(
                listOf<Map<String, Any>>(event1, event2, event3),
                listOf(
                    DisplayedIam("campaign1", 100),
                    DisplayedIam("campaign2", 1500),
                    DisplayedIam("campaign3", 30000)
                ),
                listOf(
                    ButtonClicked("campaign1", "button1", 200),
                    ButtonClicked("campaign1", "button2", 300),
                    ButtonClicked("campaign2", "button1", 2000)
                ),
                false
            )
            val expectedComposite: RequestModel = CompositeRequestModel(
                REQUEST_ID,
                "https://mobile-events.eservice.emarsys.net/v3/apps/" + APPLICATION_CODE + "/client/events",
                RequestMethod.POST,
                payload,
                customEvent1.headers,
                TIMESTAMP,
                Long.MAX_VALUE,
                arrayOf(customEvent1.id, customEvent2.id, customEvent3.id)
            )
            val expected = listOf(
                request1,
                request2,
                expectedComposite,
                request3
            )
            compositeRepository.query(Everything()) shouldBe expected
        }
    }

    @Test
    fun testQuery_resultPayloadShouldContainDoNotDisturbWithTrue_whenDoNotDisturbIsOn() {
        whenever(inAppEventHandlerInternal.isPaused).thenReturn(true)
        compositeRepository = compositeRepositoryWithRealRepositories()
        val customEvent1 = customEvent(900, "event1")
        runBlocking {
            requestModelRepository.add(customEvent1)
        }
        val result = compositeRepository.query(Everything())
        val payload = result[0].payload
        payload?.get("dnd") shouldBe true
    }

    @Test
    fun testQuery_resultPayloadShouldNotContainDoNotDisturb_whenDoNotDisturbIsOff() {
        whenever(inAppEventHandlerInternal.isPaused).thenReturn(false)
        compositeRepository = compositeRepositoryWithRealRepositories()
        val customEvent1 = customEvent(900, "event1")
        runBlocking {
            requestModelRepository.add(customEvent1)
        }
        val result = compositeRepository.query(Everything())
        val payload = result[0].payload
        payload?.get("dnd") shouldBe null
    }

    private fun compositeRepositoryWithRealRepositories(): RequestRepositoryProxy {
        return RequestRepositoryProxy(
            requestModelRepository,
            displayedIamRepository,
            buttonClickedRepository,
            timestampProvider,
            uuidProvider,
            inAppEventHandlerInternal,
            mockEventServiceProvider,
            mockRequestModelHelper
        )
    }

    private fun customEvent(
        timestamp: Long,
        eventName: String,
        attributes: Map<String, Any>? = null
    ): RequestModel {
        val event: MutableMap<String, Any> = HashMap()
        event["type"] = "custom"
        event["name"] = eventName
        event["timestamp"] = TimestampUtils.formatTimestampWithUTC(timestamp)
        if (attributes != null && attributes.isNotEmpty()) {
            event["attributes"] = attributes
        }
        val payload: MutableMap<String, Any?> = HashMap()
        payload["clicks"] = ArrayList<Any>()
        payload["viewed_messages"] = ArrayList<Any>()
        payload["events"] = listOf<Map<String, Any>>(event)

        val headers: MutableMap<String, String> = HashMap()
        headers["custom_event_header1"] = "custom_event_value1"
        headers["custom_event_header2"] = "custom_event_value2"
        return RequestModel(
            "https://mobile-events.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/events",
            RequestMethod.POST,
            payload,
            headers,
            System.currentTimeMillis(),
            999,
            uuidProvider.provideId()
        )
    }

    private fun requestModel(): RequestModel {
        val payload: MutableMap<String, Any?> = HashMap()
        payload["key"] = randomString()
        val headers: MutableMap<String, String> = HashMap()
        headers["header1"] = "value1"
        headers["header2"] = "value2"
        return RequestModel.Builder(timestampProvider, uuidProvider)
            .url("https://emarsys.com")
            .payload(payload)
            .headers(headers)
            .build()
    }

    companion object {
        private const val TIMESTAMP = 80000L
        private const val REQUEST_ID = "REQUEST_ID"
        private const val APPLICATION_CODE = "applicationCode"
        private const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net/v3"
    }
}