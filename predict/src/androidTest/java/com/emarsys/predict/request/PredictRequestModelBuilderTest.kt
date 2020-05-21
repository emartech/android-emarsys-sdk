package com.emarsys.predict.request

import android.net.Uri
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.util.JsonUtils
import com.emarsys.core.util.RequestModelUtils
import com.emarsys.predict.api.model.PredictCartItem
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.api.model.RecommendationLogic
import com.emarsys.predict.model.LastTrackedItemContainer
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class PredictRequestModelBuilderTest {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val MERCHANT_ID = "merchantId"
        const val OS_VERSION = "1.0.0"
        const val PLATFORM = "android"
        const val TTL = Long.MAX_VALUE
        val BASE_HEADER = mapOf("User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM")
        const val SEARCH_TERM = "testSearchTerm"
        const val CART = "i:1234,p:1.0,q:1.0|i:4321,p:2.0,q:2.0"
        const val CATEGORY_PATH = "testCategoryPath"
        const val LAST_ITEM = "i:itemId"
        const val PREDICT_VISITOR_ID_KEY = "predict_visitor_id"
        const val PREDICT_CONTACT_ID_KEY = "predict_contact_id"
        val CART_ITEMS = listOf(
                PredictCartItem("1234", 1.0, 1.0),
                PredictCartItem("4321", 2.0, 2.0))
    }

    private lateinit var requestModelBuilder: PredictRequestModelBuilder
    private lateinit var mockRequestContext: PredictRequestContext
    private lateinit var mockHeaderFactory: PredictHeaderFactory
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockKeyValueStore: KeyValueStore
    private lateinit var lastTrackedItemContainer: LastTrackedItemContainer
    private lateinit var mockServiceProvider: ServiceEndpointProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockKeyValueStore = mock(KeyValueStore::class.java).apply {
            whenever(getString(PREDICT_VISITOR_ID_KEY)).thenReturn("visitorId")
            whenever(getString(PREDICT_CONTACT_ID_KEY)).thenReturn("contactId")
        }
        mockHeaderFactory = mock(PredictHeaderFactory::class.java).apply {
            whenever(createBaseHeader()).thenReturn(BASE_HEADER)
        }
        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }

        mockRequestContext = mock(PredictRequestContext::class.java).apply {
            whenever(merchantId).thenReturn(MERCHANT_ID)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
            whenever(keyValueStore).thenReturn(mockKeyValueStore)
        }

        lastTrackedItemContainer = LastTrackedItemContainer()
        lastTrackedItemContainer.lastCategoryPath = "testCategoryPath"
        lastTrackedItemContainer.lastSearchTerm = SEARCH_TERM
        lastTrackedItemContainer.lastCartItems = CART_ITEMS
        lastTrackedItemContainer.lastItemView = "itemId"
        mockServiceProvider = mock(ServiceEndpointProvider::class.java).apply {
            whenever(provideEndpointHost()).thenReturn("https://recommender.scarabresearch.com")
        }
        requestModelBuilder = PredictRequestModelBuilder(mockRequestContext, mockHeaderFactory, mockServiceProvider)
    }

    @Test
    fun testBuild_withShardData() {
        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId?cp=1&vi=888999888&ci=12345&q3=c")

        val shardData = mapOf(
                "cp" to 1,
                "q3" to "c",
                "vi" to "888999888",
                "ci" to "12345")

        val result = requestModelBuilder.withShardData(shardData).build()

        result.payload shouldBe expectedRequestModel.payload
        result.method shouldBe expectedRequestModel.method
        result.timestamp shouldBe expectedRequestModel.timestamp
        result.headers shouldBe expectedRequestModel.headers
        result.id shouldBe expectedRequestModel.id
        result.ttl shouldBe expectedRequestModel.ttl
        RequestModelUtils.extractQueryParameters(result) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWithLimit_limit_mustBeGreaterThanZero() {
        requestModelBuilder.withLimit(-10)
    }

    @Test
    fun testBuild_withLogic_withLogicData() {
        forall(
                row(RecommendationLogic.search("searchTerm"), createRequestModelWithUrl(mapOf(
                        "f" to "f:SEARCH,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "q" to "searchTerm"))),
                row(RecommendationLogic.cart(listOf(
                        PredictCartItem("1234", 1.0, 1.0),
                        PredictCartItem("4321", 2.0, 2.0))), createRequestModelWithUrl(mapOf(
                        "f" to "f:CART,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "cv" to "1",
                        "ca" to "i:1234,p:1.0,q:1.0|i:4321,p:2.0,q:2.0"))),
                row(RecommendationLogic.category("categoryPath"), createRequestModelWithUrl(mapOf(
                        "f" to "f:CATEGORY,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "vc" to "categoryPath"))),
                row(RecommendationLogic.popular("categoryPath"), createRequestModelWithUrl(mapOf(
                        "f" to "f:POPULAR,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "vc" to "categoryPath"))),
                row(RecommendationLogic.alsoBought("itemId"), createRequestModelWithUrl(mapOf(
                        "f" to "f:ALSO_BOUGHT,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "v" to "i:itemId"))),
                row(RecommendationLogic.related("itemId"), createRequestModelWithUrl(mapOf(
                        "f" to "f:RELATED,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "v" to "i:itemId")))
        ) { logic, expectedRequestModel -> RequestModelUtils.extractQueryParameters(requestModelBuilder.withLogic(logic, lastTrackedItemContainer).build()) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel) }
    }

    @Test
    fun testBuild_withLogic_withLogicData_withoutVisitorId() {
        whenever(mockKeyValueStore.getString(PREDICT_VISITOR_ID_KEY)).thenReturn(null)
        val logic = RecommendationLogic.related("itemId")
        val expectedRequestModel = createRequestModelWithUrl(mapOf(
                "f" to "f:RELATED,l:5,o:0",
                "ci" to "contactId",
                "v" to "i:itemId"))

        RequestModelUtils.extractQueryParameters(requestModelBuilder.withLogic(logic, lastTrackedItemContainer).build()) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel)
    }

    @Test
    fun testBuild_withLogic_withLogicData_withoutContactId() {
        whenever(mockKeyValueStore.getString(PREDICT_CONTACT_ID_KEY)).thenReturn(null)
        val logic = RecommendationLogic.related("itemId")
        val expectedRequestModel = createRequestModelWithUrl(mapOf(
                "f" to "f:RELATED,l:5,o:0",
                "vi" to "visitorId",
                "v" to "i:itemId"))

        RequestModelUtils.extractQueryParameters(requestModelBuilder.withLogic(logic, lastTrackedItemContainer).build()) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel)
    }


    @Test
    fun testBuild_withLogic_withFiltersData() {
        val logic = RecommendationLogic.related("itemId")
        val filters = listOf(RecommendationFilter.exclude("field1").hasValue("expectation1"),
                RecommendationFilter.exclude("field2").isValue("expectation2"),
                RecommendationFilter.exclude("field3").inValues(listOf("expectation31", "expectation32")),
                RecommendationFilter.exclude("field4").overlapsValues(listOf("expectation41", "expectation42")),
                RecommendationFilter.include("field5").hasValue("expectation5"),
                RecommendationFilter.include("field6").isValue("expectation6"),
                RecommendationFilter.include("field7").inValues(listOf("expectation71", "expectation72")),
                RecommendationFilter.include("field8").overlapsValues(listOf("expectation81", "expectation82")))
        val expected = createRequestModelWithUrl(mapOf(
                "f" to "f:RELATED,l:5,o:0",
                "v" to "i:itemId",
                "vi" to "visitorId",
                "ci" to "contactId",
                "ex" to """[{"f":"field1","r":"HAS","v":"expectation1","n":false},{"f":"field2","r":"IS","v":"expectation2","n":false},{"f":"field3","r":"IN","v":"expectation31|expectation32","n":false},{"f":"field4","r":"OVERLAPS","v":"expectation41|expectation42","n":false},{"f":"field5","r":"HAS","v":"expectation5","n":true},{"f":"field6","r":"IS","v":"expectation6","n":true},{"f":"field7","r":"IN","v":"expectation71|expectation72","n":true},{"f":"field8","r":"OVERLAPS","v":"expectation81|expectation82","n":true}]"""))

        val expectedMap = RequestModelUtils.extractQueryParameters(expected)
        val returnedMap = RequestModelUtils.extractQueryParameters(requestModelBuilder.withLogic(logic, lastTrackedItemContainer).withFilters(filters).build())

        returnedMap["f"] shouldBe expectedMap["f"]
        returnedMap["v"] shouldBe expectedMap["v"]

        val returnedExJson = JSONArray(returnedMap["ex"])

        val returnedExList = mutableListOf<Map<String, String>>()
        for (i in 0 until returnedExJson.length()) {
            returnedExList.add(JsonUtils.toFlatMap(returnedExJson.get(i) as JSONObject))
        }

        val expectedExJson = JSONArray(expectedMap["ex"])

        val expectedExList = mutableListOf<Map<String, String>>()
        for (i in 0 until returnedExJson.length()) {
            expectedExList.add(JsonUtils.toFlatMap(expectedExJson.get(i) as JSONObject))
        }

        returnedExList shouldBe expectedExList
    }

    @Test
    fun testBuild_withLogic_withoutLogicData() {
        forall(
                row(RecommendationLogic.search(), createRequestModelWithUrl(mapOf(
                        "f" to "f:SEARCH,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "q" to SEARCH_TERM))),
                row(RecommendationLogic.cart(), createRequestModelWithUrl(mapOf(
                        "f" to "f:CART,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "cv" to "1",
                        "ca" to CART))),
                row(RecommendationLogic.category(), createRequestModelWithUrl(mapOf(
                        "f" to "f:CATEGORY,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "vc" to CATEGORY_PATH))),
                row(RecommendationLogic.popular(), createRequestModelWithUrl(mapOf(
                        "f" to "f:POPULAR,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "vc" to CATEGORY_PATH))),
                row(RecommendationLogic.alsoBought(), createRequestModelWithUrl(mapOf(
                        "f" to "f:ALSO_BOUGHT,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "v" to LAST_ITEM))),
                row(RecommendationLogic.related(), createRequestModelWithUrl(mapOf(
                        "f" to "f:RELATED,l:5,o:0",
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "v" to LAST_ITEM)))
        ) { logic, expectedRequestModel -> RequestModelUtils.extractQueryParameters(requestModelBuilder.withLogic(logic, lastTrackedItemContainer).build()) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel) }
    }

    @Test
    fun testBuild_withLogic_withoutLogicData_withNoLastTrackedItems() {
        lastTrackedItemContainer = LastTrackedItemContainer()
        forall(
                row(RecommendationLogic.search(), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:SEARCH,l:5,o:0"))),
                row(RecommendationLogic.cart(), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:CART,l:5,o:0"))),
                row(RecommendationLogic.category(), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:CATEGORY,l:5,o:0"))),
                row(RecommendationLogic.popular(), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:POPULAR,l:5,o:0"))),
                row(RecommendationLogic.alsoBought(), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:ALSO_BOUGHT,l:5,o:0"))),
                row(RecommendationLogic.related(), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:RELATED,l:5,o:0"))),
                row(RecommendationLogic.personal(), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:PERSONAL,l:5,o:0"))),
                row(RecommendationLogic.personal(listOf("1", "2", "3")), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:PERSONAL_1,l:5,o:0|f:PERSONAL_2,l:5,o:0|f:PERSONAL_3,l:5,o:0"))),
                row(RecommendationLogic.home(), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:HOME,l:5,o:0"))),
                row(RecommendationLogic.home(listOf("1", "2", "3")), createRequestModelWithUrl(mapOf(
                        "vi" to "visitorId",
                        "ci" to "contactId",
                        "f" to "f:HOME_1,l:5,o:0|f:HOME_2,l:5,o:0|f:HOME_3,l:5,o:0")))
        ) { logic, expectedRequestModel -> RequestModelUtils.extractQueryParameters(requestModelBuilder.withLogic(logic, lastTrackedItemContainer).build()) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel) }
    }

    @Test
    fun testBuild_withLimit_setsLimit() {
        val expectedRequestModel = createRequestModelWithUrl(mapOf(
                "f" to "f:SEARCH,l:10,o:0",
                "vi" to "visitorId",
                "ci" to "contactId",
                "q" to SEARCH_TERM))

        val result = requestModelBuilder.withLogic(RecommendationLogic.search(SEARCH_TERM), lastTrackedItemContainer).withLimit(10).build()

        RequestModelUtils.extractQueryParameters(result) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel)
    }

    @Test
    fun testBuild_limitIsSetToDefault_whenLimitIsNull() {
        val expectedRequestModel = createRequestModelWithUrl(mapOf(
                "f" to "f:SEARCH,l:5,o:0",
                "vi" to "visitorId",
                "ci" to "contactId",
                "q" to SEARCH_TERM))

        val result = requestModelBuilder.withLogic(RecommendationLogic.search(SEARCH_TERM), lastTrackedItemContainer).withLimit(null).build()

        RequestModelUtils.extractQueryParameters(result) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel)
    }

    private fun createRequestModelWithUrl(queryParams: Map<String, String>): RequestModel {
        val uriBuilder = Uri.parse("https://recommender.scarabresearch.com/merchants/merchantId").buildUpon()
        if (queryParams.isNotEmpty()) {
            for (key in queryParams.keys) {
                uriBuilder.appendQueryParameter(key, queryParams[key])
            }
        }
        return RequestModel(
                uriBuilder.build().toString(),
                RequestMethod.GET,
                null,
                BASE_HEADER,
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )
    }

    private fun requestModel(url: String) = RequestModel(
            url,
            RequestMethod.GET,
            null,
            BASE_HEADER,
            TIMESTAMP,
            TTL,
            REQUEST_ID
    )
}