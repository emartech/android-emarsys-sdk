package com.emarsys.predict

import android.os.Handler
import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.ScopeDelegatorCompletionHandlerProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.predict.api.model.*
import com.emarsys.predict.fake.FakeRestClient
import com.emarsys.predict.fake.FakeResultListener
import com.emarsys.predict.model.LastTrackedItemContainer
import com.emarsys.predict.provider.PredictRequestModelBuilderProvider
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.request.PredictRequestModelBuilder
import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.ThreadSpy
import com.emarsys.testUtil.mockito.anyNotNull
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*
import java.lang.IllegalArgumentException
import java.util.concurrent.CountDownLatch

class DefaultPredictInternalTest {

    companion object {
        const val TTL = Long.MAX_VALUE
        const val TIMESTAMP = 100000L
        const val ID1 = "id1"
        const val ID2 = "id2"
        val PRODUCT: Product = Product(ID1, "title", "https://emarsys.com", "RELATED", "AAAA")
        const val FIELD = "Field"
        const val COMPARISON = "Comparison"
        const val TYPE = "INCLUDE_OR_EXCLUDE"
        val EXPECTATIONS = listOf<String>()
        const val CONTACT_FIELD_ID = 999
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var mockKeyValueStore: KeyValueStore
    private lateinit var predictInternal: PredictInternal
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockRequestContext: PredictRequestContext
    private lateinit var mockRequestModelBuilderProvider: PredictRequestModelBuilderProvider
    private lateinit var mockRequestModelBuilder: PredictRequestModelBuilder
    private lateinit var mockPredictResponseMapper: PredictResponseMapper
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var uiHandler: Handler
    private lateinit var mockLogic: Logic
    private lateinit var mockRecommendationFilter: RecommendationFilter
    private lateinit var mockRecommendationFilters: List<RecommendationFilter>
    private lateinit var latch: CountDownLatch
    private lateinit var mockResultListener: ResultListener<Try<List<Product>>>
    private lateinit var mockLastTrackedItemContainer: LastTrackedItemContainer

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        latch = CountDownLatch(1)

        mockRequestModel = mock {
            on { id } doReturn ID1
        }
        mockResponseModel = mock()
        mockKeyValueStore = mock()
        mockRequestManager = mock()
        uiHandler = Handler(Looper.getMainLooper())
        mockPredictResponseMapper = mock()
        mockLogic = mock()
        mockRecommendationFilter = mock {
            on { field } doReturn FIELD
            on { comparison } doReturn COMPARISON
            on { type } doReturn TYPE
            on { expectations } doReturn EXPECTATIONS
        }
        mockRecommendationFilters = listOf(mockRecommendationFilter)

        mockResultListener = mock() as ResultListener<Try<List<Product>>>
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn TIMESTAMP
        }

        mockUuidProvider = mock {
            on { provideId() } doReturnConsecutively listOf(ID1, ID2)
        }

        mockRequestContext = mock {
            on { keyValueStore } doReturn mockKeyValueStore
            on { timestampProvider } doReturn mockTimestampProvider
            on { uuidProvider } doReturn mockUuidProvider
        }

        mockRequestModelBuilder = mock {
            on { withLogic(anyNotNull(), anyNotNull()) } doReturn it
            on { withLimit(anyOrNull()) } doReturn it
            on { withAvailabilityZone(anyOrNull()) } doReturn it
            on { withShardData(anyOrNull()) } doReturn it
            on { withFilters(anyOrNull()) } doReturn it
            on { build() } doReturn mockRequestModel
        }

        mockRequestModelBuilderProvider = mock {
            on { providePredictRequestModelBuilder() } doReturn mockRequestModelBuilder
        }

        mockLastTrackedItemContainer = mock()

        predictInternal = DefaultPredictInternal(
            mockRequestContext,
            mockRequestManager,
            uiHandler,
            mockRequestModelBuilderProvider,
            mockPredictResponseMapper
        )

        ReflectionTestUtils.setInstanceField(
            predictInternal,
            "lastTrackedContainer",
            mockLastTrackedItemContainer
        )
    }

    @Test
    fun testSetContact_shouldPersistsWithKeyValueStore() {
        val contactId = "contactId"

        predictInternal.setContact(CONTACT_FIELD_ID, contactId)

        verify(mockKeyValueStore).putString("predict_contact_id", contactId)
    }

    @Test
    fun testClearContact_shouldRemove_contactIdFromKeyValueStore() {
        predictInternal.clearContact()

        verify(mockKeyValueStore).remove("predict_contact_id")
    }

    @Test
    fun testClearContact_shouldRemove_visitorIdFromKeyValueStore() {
        predictInternal.clearContact()

        verify(mockKeyValueStore).remove("predict_visitor_id")
    }

    @Test
    fun testTrackCart_returnsShardId() {
        Assert.assertEquals(ID1, predictInternal.trackCart(listOf()))
    }

    @Test
    fun testTrackCart_shouldCallRequestManager_withCorrectShardModel() {
        val expectedShardModel =
            ShardModel(
                ID1,
                "predict_cart",
                mapOf(
                    "cv" to 1,
                    "ca" to "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0"
                ),
                TIMESTAMP,
                TTL
            )

        predictInternal.trackCart(
            listOf(
                PredictCartItem("itemId1", 200.0, 100.0),
                PredictCartItem("itemId2", 201.0, 101.0)
            )
        )

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test
    fun testTrackPurchase_returnsShardId() {
        Assert.assertEquals(ID1, predictInternal.trackPurchase("orderId", listOf(mock())))
    }

    @Test
    fun testTrackPurchase_shouldCallRequestManager_withCorrectShardModel() {
        val orderId = "orderId"

        val expectedShardModel =
            ShardModel(
                ID1,
                "predict_purchase",
                mapOf(
                    "oi" to orderId,
                    "co" to "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0"
                ),
                TIMESTAMP,
                TTL
            )

        predictInternal.trackPurchase(
            orderId,
            listOf(
                PredictCartItem("itemId1", 200.0, 100.0),
                PredictCartItem("itemId2", 201.0, 101.0)
            )
        )

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test
    fun testTrackPurchase_shouldThrowException_whenItemsListIsEmpty() {
        val expectedException = shouldThrow<IllegalArgumentException> {
            predictInternal.trackPurchase("orderId", emptyList())
        }
        expectedException.message shouldBe "Items must not be empty!"
    }

    @Test
    fun testTrackItemView_returnsShardId() {
        Assert.assertEquals(ID1, predictInternal.trackItemView("itemId"))
    }

    @Test
    fun testTrackItemView_shouldCallRequestManager_withCorrectShardModel() {
        val itemId = "itemId"

        val expectedShardModel = ShardModel(
            ID1,
            "predict_item_view",
            mapOf("v" to "i:$itemId"),
            TIMESTAMP,
            TTL
        )

        predictInternal.trackItemView(itemId)

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test
    fun testTrackCategoryView_returnsShardId() {
        Assert.assertEquals(ID1, predictInternal.trackCategoryView("categoryPath"))
    }

    @Test
    fun testTrackCategoryView_shouldCallRequestManager_withCorrectShardModel() {
        val categoryPath = "categoryPath"

        val expectedShardModel = ShardModel(
            ID1,
            "predict_category_view",
            mapOf("vc" to categoryPath),
            TIMESTAMP,
            TTL
        )

        predictInternal.trackCategoryView(categoryPath)

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test
    fun testTrackSearchTerm_returnsShardId() {
        Assert.assertEquals(ID1, predictInternal.trackSearchTerm("searchTerm"))
    }

    @Test
    fun testTrackSearchTerm_shouldCallRequestManager_withCorrectShardModel() {
        val searchTerm = "searchTerm"

        val expectedShardModel = ShardModel(
            ID1,
            "predict_search_term",
            mapOf("q" to searchTerm),
            TIMESTAMP,
            TTL
        )

        predictInternal.trackSearchTerm(searchTerm)

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test
    fun testTrackTag_shouldCallRequestManager_withCorrectShardModel() {
        val tag = "testTag"

        val expectedShardModel = ShardModel(
            ID1,
            "predict_tag",
            mapOf("ta" to """{"name":"$tag","attributes":{"testKey":"testValue"}}"""),
            TIMESTAMP,
            TTL
        )

        predictInternal.trackTag(tag, mapOf("testKey" to "testValue"))

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test
    fun testTrackTag_shouldCallRequestManager_withCorrectShardModel_when_attributesIsNull() {
        val tag = "testTag"

        val expectedShardModel = ShardModel(
            ID1,
            "predict_tag",
            mapOf("t" to tag),
            TIMESTAMP,
            TTL
        )

        predictInternal.trackTag(tag, null)

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test
    fun testTrackSearchTerm_shouldSetLastTrackedItemContainersLastSearchTermField_withCorrectSearchTerm() {
        val searchTerm = "searchTerm"
        ReflectionTestUtils.setInstanceField(
            predictInternal,
            "lastTrackedContainer",
            mockLastTrackedItemContainer
        )
        predictInternal.trackSearchTerm(searchTerm)
        verify(mockLastTrackedItemContainer).lastSearchTerm = searchTerm
    }

    @Test
    fun testTrackCart_shouldSetLastTrackedItemContainersLastCartItemsField_withCorrectCartItemList() {
        val cartList = listOf<CartItem>(
            PredictCartItem("testCartItem1", 1.0, 1.0),
            PredictCartItem("testCartItem2", 2.0, 2.0)
        )
        ReflectionTestUtils.setInstanceField(
            predictInternal,
            "lastTrackedContainer",
            mockLastTrackedItemContainer
        )
        predictInternal.trackCart(cartList)
        verify(mockLastTrackedItemContainer).lastCartItems = cartList
    }

    @Test
    fun testTrackPurchase_shouldSetLastTrackedItemContainersLastCartItemsField_withCorrectCartItemList() {
        val cartList = listOf<CartItem>(
            PredictCartItem("testCartItem1", 1.0, 1.0),
            PredictCartItem("testCartItem2", 2.0, 2.0)
        )
        ReflectionTestUtils.setInstanceField(
            predictInternal,
            "lastTrackedContainer",
            mockLastTrackedItemContainer
        )
        predictInternal.trackPurchase("testOrderId", cartList)
        verify(mockLastTrackedItemContainer).lastCartItems = cartList
    }

    @Test
    fun testTrackItemView_shouldSetLastTrackedItemContainersLastItemViewField_withCorrectItemId() {
        val itemId = "itemId"
        ReflectionTestUtils.setInstanceField(
            predictInternal,
            "lastTrackedContainer",
            mockLastTrackedItemContainer
        )
        predictInternal.trackItemView(itemId)
        verify(mockLastTrackedItemContainer).lastItemView = itemId
    }

    @Test
    fun testTrackCategoryView_shouldSetLastTrackedItemContainersLastCategoryPathField_withCorrectCategoryPath() {
        val categoryPath = "categoryPath"
        ReflectionTestUtils.setInstanceField(
            predictInternal,
            "lastTrackedContainer",
            mockLastTrackedItemContainer
        )
        predictInternal.trackCategoryView(categoryPath)
        verify(mockLastTrackedItemContainer).lastCategoryPath = categoryPath
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_withCorrectRequestModel_whenLimitUsed() {
        predictInternal.recommendProducts(
            mockLogic,
            10,
            mockRecommendationFilters,
            null,
            mockResultListener
        )

        verify(mockRequestModelBuilder).withLogic(mockLogic, mockLastTrackedItemContainer)
        verify(mockRequestModelBuilder).withLimit(10)
        verify(mockRequestModelBuilder).build()

        verify(mockRequestManager).submitNow(eq(mockRequestModel), any(), anyOrNull())
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_withCorrectRequestModel_whenAvailabilityZoneUsed() {
        predictInternal.recommendProducts(
            mockLogic,
            10,
            mockRecommendationFilters,
            "hu",
            mockResultListener
        )

        verify(mockRequestModelBuilder).withLogic(mockLogic, mockLastTrackedItemContainer)
        verify(mockRequestModelBuilder).withAvailabilityZone("hu")
        verify(mockRequestModelBuilder).build()

        verify(mockRequestManager).submitNow(eq(mockRequestModel), any(), anyOrNull())
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_withCorrectRequestModel_whenFilterUsed() {
        predictInternal.recommendProducts(
            mockLogic,
            10,
            mockRecommendationFilters,
            null,
            mockResultListener
        )

        verify(mockRequestModelBuilder).withLogic(mockLogic, mockLastTrackedItemContainer)
        verify(mockRequestModelBuilder).withLimit(10)
        verify(mockRequestModelBuilder).withFilters(listOf(mockRecommendationFilter))
        verify(mockRequestModelBuilder).build()

        verify(mockRequestManager).submitNow(eq(mockRequestModel), any(), anyOrNull())
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_withCorrectRequestModel() {
        predictInternal.recommendProducts(mockLogic, resultListener = mockResultListener)

        verify(mockRequestModelBuilder).withLogic(mockLogic, mockLastTrackedItemContainer)
        verify(mockRequestModelBuilder).withLimit(null)
        verify(mockRequestModelBuilder).build()

        verify(mockRequestManager).submitNow(eq(mockRequestModel), any(), anyOrNull())
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_success_shouldBeCalledOnMainThread() {
        val expectedResult = listOf(PRODUCT)
        whenever(mockPredictResponseMapper.map(mockResponseModel)).thenReturn(expectedResult)

        predictInternal = DefaultPredictInternal(
            mockRequestContext,
            requestManagerWithRestClient(
                FakeRestClient(
                    mockResponseModel,
                    FakeRestClient.Mode.SUCCESS
                )
            ),
            uiHandler,
            mockRequestModelBuilderProvider,
            mockPredictResponseMapper
        )

        val mockResultListener: ResultListener<Try<List<Product>>> = mock()
        predictInternal.recommendProducts(
            mockLogic,
            5,
            mockRecommendationFilters,
            resultListener = mockResultListener
        )

        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockResultListener.onResult(any())).doAnswer {
            threadSpy.answer(it)
            latch.countDown()
        }
        latch.await()
        argumentCaptor<Try<List<Product>>>().apply {
            verify(mockResultListener).onResult(capture())
            firstValue.result shouldBe expectedResult
        }
        threadSpy.verifyCalledOnMainThread()
        verify(mockPredictResponseMapper).map(mockResponseModel)
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_failure_shouldBeCalled() {
        predictInternal = DefaultPredictInternal(
            mockRequestContext,
            requestManagerWithRestClient(
                FakeRestClient(
                    mockResponseModel,
                    FakeRestClient.Mode.ERROR_RESPONSE_MODEL
                )
            ),
            uiHandler,
            mockRequestModelBuilderProvider,
            mockPredictResponseMapper
        )
        val resultListener =
            FakeResultListener<List<Product>>(latch, FakeResultListener.Mode.MAIN_THREAD)
        predictInternal.recommendProducts(
            mockLogic,
            5,
            mockRecommendationFilters,
            resultListener = resultListener
        )
        latch.await()
        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_failure_shouldBeCalledOnMainThread() {
        predictInternal = DefaultPredictInternal(
            mockRequestContext,
            requestManagerWithRestClient(
                FakeRestClient(
                    mockResponseModel,
                    FakeRestClient.Mode.ERROR_RESPONSE_MODEL
                )
            ),
            uiHandler,
            mockRequestModelBuilderProvider,
            mockPredictResponseMapper
        )
        val mockResultListener: ResultListener<Try<List<Product>>> = mock()
        predictInternal.recommendProducts(
            mockLogic,
            5,
            mockRecommendationFilters,
            resultListener = mockResultListener
        )
        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockResultListener.onResult(any())).doAnswer {
            threadSpy.answer(it)
            latch.countDown()
        }
        latch.await()
        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_failureWithException_shouldBeCalled() {
        val mockException: Exception = mock()

        predictInternal = DefaultPredictInternal(
            mockRequestContext,
            requestManagerWithRestClient(FakeRestClient(mockException)),
            uiHandler,
            mockRequestModelBuilderProvider,
            mockPredictResponseMapper
        )
        val resultListener =
            FakeResultListener<List<Product>>(latch, FakeResultListener.Mode.MAIN_THREAD)
        predictInternal.recommendProducts(
            mockLogic,
            5,
            mockRecommendationFilters,
            resultListener = resultListener
        )
        latch.await()
        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_failureWithException_shouldBeCalledOnMainThread() {
        val mockException: Exception = mock()

        predictInternal = DefaultPredictInternal(
            mockRequestContext,
            requestManagerWithRestClient(FakeRestClient(mockException)),
            uiHandler,
            mockRequestModelBuilderProvider,
            mockPredictResponseMapper
        )
        val mockResultListener: ResultListener<Try<List<Product>>> = mock()
        predictInternal.recommendProducts(
            mockLogic,
            5,
            mockRecommendationFilters,
            resultListener = mockResultListener
        )
        val threadSpy: ThreadSpy<*> = ThreadSpy<Any?>()
        whenever(mockResultListener.onResult(any())).doAnswer {
            threadSpy.answer(it)
            latch.countDown()
        }
        latch.await()
        threadSpy.verifyCalledOnMainThread()
    }

    @Test
    fun testTrackRecommendationClick_returnsShardId() {
        Assert.assertEquals(ID1, predictInternal.trackRecommendationClick(PRODUCT))
    }

    @Test
    fun testTrackRecommendationClick_shouldCallRequestManager_withCorrectShardModel() {
        val expectedShardModel = ShardModel(
            ID1,
            "predict_item_view",
            mapOf("v" to "i:${PRODUCT.productId},t:${PRODUCT.feature},c:${PRODUCT.cohort}"),
            TIMESTAMP,
            TTL
        )

        predictInternal.trackRecommendationClick(PRODUCT)

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test
    fun testTrackRecommendationClick_shouldSetLastTrackedItemContainersLastItemViewField_withCorrectItemId() {
        ReflectionTestUtils.setInstanceField(
            predictInternal,
            "lastTrackedContainer",
            mockLastTrackedItemContainer
        )
        predictInternal.trackRecommendationClick(PRODUCT)
        verify(mockLastTrackedItemContainer).lastItemView = PRODUCT.productId
    }

    @Suppress("UNCHECKED_CAST")
    private fun requestManagerWithRestClient(restClient: RestClient): RequestManager {
        val mockScopeDelegatorCompletionHandlerProvider: ScopeDelegatorCompletionHandlerProvider =
            mock {
                on { provide(any(), any()) } doAnswer {
                    it.arguments[0] as CoreCompletionHandler
                }
                on { provide(any(), any()) } doAnswer {
                    it.arguments[0] as CoreCompletionHandler
                }
            }
        val mockProvider: CompletionHandlerProxyProvider = mock {
            on { provideProxy(isNull(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
            on { provideProxy(any(), any()) } doAnswer {
                it.arguments[1] as CoreCompletionHandler
            }
        }
        return RequestManager(
            mock(),
            mock() as Repository<RequestModel, SqlSpecification>,
            mock() as Repository<ShardModel, SqlSpecification>,
            mock(),
            restClient,
            mock() as Registry<RequestModel, CompletionListener?>,
            mock(),
            mockProvider,
            mockScopeDelegatorCompletionHandlerProvider,
            mock()
        )
    }
}