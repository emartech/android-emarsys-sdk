package com.emarsys.predict

import android.os.Handler
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
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.worker.Worker
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.PredictCartItem
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.fake.FakeRestClient
import com.emarsys.predict.fake.FakeResultListener
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.request.PredictRequestModelFactory
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch

class DefaultPredictInternalTest {

    companion object {
        const val TTL = Long.MAX_VALUE
        const val TIMESTAMP = 100000L
        const val ID1 = "id1"
        const val ID2 = "id2"
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
    private lateinit var mockRequestModelFactory: PredictRequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockResponseModel: ResponseModel
    private lateinit var latch: CountDownLatch

    @Before
    fun init() {

        latch = CountDownLatch(1)

        mockRequestModel = mock(RequestModel::class.java)
        mockResponseModel = mock(ResponseModel::class.java)
        mockKeyValueStore = mock(KeyValueStore::class.java)
        mockRequestManager = mock(RequestManager::class.java)

        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }

        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(ID1, ID2)
        }

        mockRequestContext = mock(PredictRequestContext::class.java).apply {
            whenever(keyValueStore).thenReturn(mockKeyValueStore)
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
        }

        mockRequestModelFactory = mock(PredictRequestModelFactory::class.java).apply {
            whenever(createRecommendationRequest()).thenReturn(mockRequestModel)
        }

        predictInternal = DefaultPredictInternal(mockRequestContext, mockRequestManager, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestContext_mustNotBeNull() {
        DefaultPredictInternal(null, mockRequestManager, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestManager_shouldNotBeNull() {
        DefaultPredictInternal(mockRequestContext, null, mockRequestModelFactory)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_requestModelFactory_shouldNotBeNull() {
        DefaultPredictInternal(mockRequestContext, mockRequestManager, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetContact_contactId_mustNotBeNull() {
        predictInternal.setContact(null)
    }

    @Test
    fun testSetContact_shouldPersistsWithKeyValueStore() {
        val contactId = "contactId"

        predictInternal.setContact(contactId)

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

    @Test(expected = IllegalArgumentException::class)
    fun testTrackCart_items_mustNotBeNull() {
        predictInternal.trackCart(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackCart_itemElements_mustNotBeNull() {
        predictInternal.trackCart(listOf(null))
    }

    @Test
    fun testTrackCart_returnsShardId() {
        Assert.assertEquals(ID1, predictInternal.trackCart(listOf()))
    }

    @Test
    fun testTrackCart_shouldCallRequestManager_withCorrectShardModel() {
        val expectedShardModel =
                ShardModel(ID1,
                        "predict_cart",
                        mapOf(
                                "cv" to 1,
                                "ca" to "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0"
                        ),
                        TIMESTAMP,
                        TTL)

        predictInternal.trackCart(listOf(
                PredictCartItem("itemId1", 200.0, 100.0),
                PredictCartItem("itemId2", 201.0, 101.0)
        ))

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackPurchase_orderId_mustNotBeNull() {
        predictInternal.trackPurchase(null, listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackPurchase_items_mustNotBeNull() {
        predictInternal.trackPurchase("orderId", null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackPurchase_itemElements_mustNotBeNull() {
        predictInternal.trackPurchase("orderId", listOf(
                mock(CartItem::class.java),
                null,
                mock(CartItem::class.java)
        ))
    }

    @Test
    fun testTrackPurchase_returnsShardId() {
        Assert.assertEquals(ID1, predictInternal.trackPurchase("orderId", listOf()))
    }

    @Test
    fun testTrackPurchase_shouldCallRequestManager_withCorrectShardModel() {
        val orderId = "orderId"

        val expectedShardModel =
                ShardModel(ID1,
                        "predict_purchase",
                        mapOf(
                                "oi" to orderId,
                                "co" to "i:itemId1,p:200.0,q:100.0|i:itemId2,p:201.0,q:101.0"
                        ),
                        TIMESTAMP,
                        TTL)

        predictInternal.trackPurchase(
                orderId,
                listOf(
                        PredictCartItem("itemId1", 200.0, 100.0),
                        PredictCartItem("itemId2", 201.0, 101.0)
                ))

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackItemView_itemId_mustNotBeNull() {
        predictInternal.trackItemView(null)
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
                TTL)

        predictInternal.trackItemView(itemId)

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackCategoryView_categoryPath_mustNotBeNull() {
        predictInternal.trackCategoryView(null)
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
                TTL)

        predictInternal.trackCategoryView(categoryPath)

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTrackSearchTerm_searchTerm_mustNotBeNull() {
        predictInternal.trackSearchTerm(null)
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
                TTL)

        predictInternal.trackSearchTerm(searchTerm)

        verify(mockRequestManager).submit(expectedShardModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRecommendProducts_resultListener_mustNotBeNull() {
        predictInternal.recommendProducts(null)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun testRecommendProducts_shouldCallRequestManager_withCorrectRequestModel() {
        val mockResultListener = mock(ResultListener::class.java) as ResultListener<Try<List<Product>>>

        predictInternal.recommendProducts(mockResultListener)

        verify(mockRequestManager).submitNow(eq(mockRequestModel), any())
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_success_shouldBeCalledOnMainThread() {
        whenever(mockResponseModel.body).thenReturn("""{
           "cohort":"AAAA",
           "visitor":"16BCC0D2745E6B36",
           "session":"24E844D1E58C1C2",
           "features":{
              "SEARCH":{
                 "hasMore":true,
                 "merchants":[
                    "1428C8EE286EC34B"
                 ],
                 "items":[
                    {
                       "id":"2119",
                       "id":"2120"
                    }
                 ]
              }
           },
           "products":{
              "2119":{
                 "item":"2119",
                 "category":"MEN>Shirts",
                 "title":"LSL Men Polo Shirt SE16",
                 "available":true,
                 "msrp":100.0,
                 "price":100.0,
                 "msrp_gpb":"83.2",
                 "price_gpb":"83.2",
                 "msrp_aed":"100",
                 "price_aed":"100",
                 "msrp_cad":"100",
                 "price_cad":"100",
                 "msrp_mxn":"2057.44",
                 "price_mxn":"2057.44",
                 "msrp_pln":"100",
                 "price_pln":"100",
                 "msrp_rub":"100",
                 "price_rub":"100",
                 "msrp_sek":"100",
                 "price_sek":"100",
                 "msrp_try":"339.95",
                 "price_try":"339.95",
                 "msrp_usd":"100",
                 "price_usd":"100",
                 "link":"http://lifestylelabels.com/lsl-men-polo-shirt-se16.html",
                 "image":"http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg",
                 "zoom_image":"http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg",
                 "description":"product Description",
                 "album":"album",
                 "actor":"actor",
                 "artist":"artist",
                 "author":"author",
                 "brand":"brand",
                 "year":"2000"
              },
              "2120":{
                 "item":"2120",
                 "title":"LSL Men Polo Shirt SE16",
                 "link":"http://lifestylelabels.com/lsl-men-polo-shirt-se16.html"
              }
           }
        }"""
        )
        predictInternal = DefaultPredictInternal(
                mockRequestContext,
                requestManagerWithRestClient(FakeRestClient(mockResponseModel, FakeRestClient.Mode.SUCCESS)),
                mockRequestModelFactory
        )
        val resultListener = FakeResultListener<List<Product>>(latch, FakeResultListener.Mode.MAIN_THREAD)
        predictInternal.recommendProducts(resultListener)

        latch.await()

        resultListener.resultStatus[0] shouldBe Product.Builder(
                "2119",
                "LSL Men Polo Shirt SE16",
                "http://lifestylelabels.com/lsl-men-polo-shirt-se16.html")
                .categoryPath("MEN>Shirts")
                .available(true)
                .msrp(100.0F)
                .price(100.0F)
                .imageUrl("http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg")
                .zoomImageUrl("http://lifestylelabels.com/pub/media/catalog/product/m/p/mp001.jpg")
                .productDescription("product Description")
                .album("album")
                .actor("actor")
                .artist("artist")
                .author("author")
                .brand("brand")
                .year(2000)
                .customFields(hashMapOf(
                        "msrp_gpb" to "83.2",
                        "price_gpb" to "83.2",
                        "msrp_aed" to "100",
                        "price_aed" to "100",
                        "msrp_cad" to "100",
                        "price_cad" to "100",
                        "msrp_mxn" to "2057.44",
                        "price_mxn" to "2057.44",
                        "msrp_pln" to "100",
                        "price_pln" to "100",
                        "msrp_rub" to "100",
                        "price_rub" to "100",
                        "msrp_sek" to "100",
                        "price_sek" to "100",
                        "msrp_try" to "339.95",
                        "price_try" to "339.95",
                        "msrp_usd" to "100",
                        "price_usd" to "100"
                ))
                .build()

        resultListener.resultStatus[1] shouldBe Product.Builder(
                "2120",
                "LSL Men Polo Shirt SE16",
                "http://lifestylelabels.com/lsl-men-polo-shirt-se16.html")
                .build()
        resultListener.successCount shouldBe 1
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_failure_shouldBeCalledOnMainThread() {
        predictInternal = DefaultPredictInternal(
                mockRequestContext,
                requestManagerWithRestClient(FakeRestClient(mockResponseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                mockRequestModelFactory
        )
        val resultListener = FakeResultListener<List<Product>>(latch, FakeResultListener.Mode.MAIN_THREAD)
        predictInternal.recommendProducts(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
    }

    @Test
    fun testRecommendProducts_shouldCallRequestManager_failureWithException_shouldBeCalledOnMainThread() {
        val mockException = mock(Exception::class.java)

        predictInternal = DefaultPredictInternal(
                mockRequestContext,
                requestManagerWithRestClient(FakeRestClient(mockException)),
                mockRequestModelFactory
        )
        val resultListener = FakeResultListener<List<Product>>(latch, FakeResultListener.Mode.MAIN_THREAD)
        predictInternal.recommendProducts(resultListener)

        latch.await()

        resultListener.errorCount shouldBe 1
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