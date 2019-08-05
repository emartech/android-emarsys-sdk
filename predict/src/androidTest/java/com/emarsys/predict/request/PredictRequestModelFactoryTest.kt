package com.emarsys.predict.request

import android.net.Uri
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.util.RequestModelUtils
import com.emarsys.predict.api.model.PredictCartItem
import com.emarsys.predict.api.model.RecommendationLogic
import com.emarsys.predict.model.InternalLogic
import com.emarsys.predict.model.LastTrackedItemContainer
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.net.URLDecoder
import java.net.URLEncoder

class PredictRequestModelFactoryTest {
    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val MERCHANT_ID = "merchantId"
        const val OS_VERSION = "1.0.0"
        const val PLATFORM = "android"
        const val TTL = Long.MAX_VALUE
        val BASE_HEADER = mapOf("User-Agent" to "EmarsysSDK|osversion:$OS_VERSION|platform:$PLATFORM")
    }

    private lateinit var requestModelFactory: PredictRequestModelFactory
    private lateinit var lastTrackedItemContainer: LastTrackedItemContainer
    private lateinit var mockRequestContext: PredictRequestContext
    private lateinit var mockHeaderFactory: PredictHeaderFactory
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
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
        }

        requestModelFactory = PredictRequestModelFactory(mockRequestContext, mockHeaderFactory)

        lastTrackedItemContainer = LastTrackedItemContainer()
        lastTrackedItemContainer.setLastCategoryPath("testCategoryPath")
        lastTrackedItemContainer.setLastSearchTerm("testSearchTerm")
        lastTrackedItemContainer.setLastCartItems(
                listOf(PredictCartItem("1234", 1.0, 1.0),
                        PredictCartItem("4321", 2.0, 2.0)))
        lastTrackedItemContainer.setLastItemView("testLastItem")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        PredictRequestModelFactory(null, mockHeaderFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_headerFactory_mustNotBeNull() {
        PredictRequestModelFactory(mockRequestContext, null)
    }

    @Test
    fun testCreateRecommendationRequest_withSearchWithParams() {
        println(URLDecoder.decode("[%3D1%25ca%3D]", "utf-8"))
        forall(
                row(RecommendationLogic.search("searchTerm"), createRequestModelWithUrl(mapOf(
                        "f" to "f:SEARCH,l:5,o:0",
                        "q" to "searchTerm"))),
                row(RecommendationLogic.cart(listOf(
                        PredictCartItem("1234", 1.0, 1.0),
                        PredictCartItem("4321", 2.0, 2.0))), createRequestModelWithUrl(mapOf(
                        "f" to "f:CART,l:5,o:0",
                        "cv" to "1",
                        "ca" to "i:1234,p:1.0,q:1.0|i:4321,p:2.0,q:2.0"))),
                row(RecommendationLogic.category("categoryPath"), createRequestModelWithUrl(mapOf(
                        "f" to "f:CATEGORY,l:5,o:0",
                        "vc" to "categoryPath"))),
                row(RecommendationLogic.popular("categoryPath"), createRequestModelWithUrl(mapOf(
                        "f" to "f:POPULAR,l:5,o:0",
                        "vc" to "categoryPath"))),
                row(RecommendationLogic.alsoBought("itemId"), createRequestModelWithUrl(mapOf(
                        "f" to "f:ALSO_BOUGHT,l:5,o:0",
                        "v" to "i:itemId"))),
                row(RecommendationLogic.related("itemId"), createRequestModelWithUrl(mapOf(
                        "f" to "f:RELATED,l:5,o:0",
                        "v" to "i:itemId")))
        ) { logic, expectedRequestModel -> RequestModelUtils.extractQueryParameters(requestModelFactory.createRecommendationRequest(InternalLogic(logic, lastTrackedItemContainer))) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel) }
    }

    private fun createRequestModelWithUrl(queryParams: Map<String, String>): RequestModel {
        val uriBuilder = Uri.parse("https://recommender.scarabresearch.com/merchants/merchantId").buildUpon()
        if (queryParams.isNotEmpty()) {
            for (key in queryParams.keys) {
                uriBuilder.appendQueryParameter(key, queryParams.get(key))
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

    @Test
    fun testCreateRecommendationRequest_withSearchTermFromHistory() {
        val recommendationCriteria = URLEncoder.encode("f:SEARCH,l:5,o:0", "utf-8")
        val recommendationLogic = RecommendationLogic.search()

        val expected = RequestModel(
                "https://recommender.scarabresearch.com/merchants/merchantId?f=$recommendationCriteria&q=testSearchTerm",
                RequestMethod.GET,
                null,
                BASE_HEADER,
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val internalLogic = InternalLogic(recommendationLogic, lastTrackedItemContainer)
        val result = requestModelFactory.createRecommendationRequest(internalLogic)

        result shouldBe expected
    }

    @Test
    fun testCreateRequestFromShardData_shouldEncodeUrl() {
        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId?cp=1&%3C%3E%2C=%22%60%3B%2F%3F%3A%5E%25%23%40%26%3D%24%2B%7B%7D%3C%3E%2C%7C%20")

        val shardData = mapOf(
                "cp" to 1,
                "<>," to "\"`;/?:^%#@&=\$+{}<>,| ")

        val result = requestModelFactory.createRequestFromShardData(shardData)

        result shouldBe expectedRequestModel
    }

    @Test
    fun testCreateRequestFromShardData_withComplexShardData() {
        val expectedRequestModel = requestModel("https://recommender.scarabresearch.com/merchants/merchantId?cp=1&vi=888999888&ci=12345&q3=c")

        val shardData = mapOf(
                "cp" to 1,
                "q3" to "c",
                "vi" to "888999888",
                "ci" to "12345")

        val result = requestModelFactory.createRequestFromShardData(shardData)

        result.payload shouldBe expectedRequestModel.payload
        result.method shouldBe expectedRequestModel.method
        result.timestamp shouldBe expectedRequestModel.timestamp
        result.headers shouldBe expectedRequestModel.headers
        result.id shouldBe expectedRequestModel.id
        result.ttl shouldBe expectedRequestModel.ttl
        RequestModelUtils.extractQueryParameters(result) shouldBe RequestModelUtils.extractQueryParameters(expectedRequestModel)
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