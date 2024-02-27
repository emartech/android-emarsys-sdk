package com.emarsys


import android.os.Looper
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.emarsys
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.di.tearDownEmarsysComponent
import com.emarsys.predict.Predict
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.RandomTestUtils.randomNumberString
import com.emarsys.testUtil.RandomTestUtils.randomString
import com.emarsys.testUtil.mockito.anyNotNull
import io.kotest.assertions.throwables.shouldThrow
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class PredictTest : AnnotationSpec() {
    companion object {
        const val AVAILABILITY_ZONE = "HU"
    }

    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var predict: Predict
    private lateinit var mockResultListener: ResultListener<Try<List<Product>>>
    private lateinit var mockLogic: Logic
    private lateinit var mockRecommendationFilters: List<RecommendationFilter>
    private lateinit var resultListenerCallback: (Try<List<Product>>) -> Unit


    @Before
    fun setUp() {
        mockPredictInternal = mock()
        resultListenerCallback = mock()

        val dependencyContainer = FakeDependencyContainer(predictInternal = mockPredictInternal)

        setupEmarsysComponent(dependencyContainer)

        predict = Predict()
        mockResultListener = mock()
        mockLogic = mock()
        val mockRecommendationFilter: RecommendationFilter = mock()
        mockRecommendationFilters = listOf(mockRecommendationFilter)
    }

    @After
    fun tearDown() {
        try {
            val handler = emarsys().concurrentHandlerHolder
            val looper: Looper = handler.coreLooper
            looper.quit()
            tearDownEmarsysComponent()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testPredict_trackCart_delegatesTo_Predict_Internal() {
        val itemList = listOf(
            createItem("itemId0", 200.0, 100.0),
            createItem("itemId1", 201.0, 101.0),
            createItem("itemId2", 202.0, 102.0)
        )
        predict.trackCart(itemList)
        Mockito.verify(mockPredictInternal).trackCart(itemList)
    }

    @Test
    fun testPredict_trackPurchase_delegatesTo_Predict_Internal() {
        val orderId = "id"
        val itemList = listOf(
            createItem("itemId0", 200.0, 100.0),
            createItem("itemId1", 201.0, 101.0),
            createItem("itemId2", 202.0, 102.0)
        )
        predict.trackPurchase(orderId, itemList)
        Mockito.verify(mockPredictInternal).trackPurchase(orderId, itemList)
    }


    @Test
    fun testPredict_trackItemView_delegatesTo_predictInternal() {
        val itemId = randomString()
        predict.trackItemView(itemId)
        Mockito.verify(mockPredictInternal).trackItemView(itemId)
    }


    @Test
    fun testPredict_trackCategoryView_delegatesTo_predictInternal() {
        val categoryPath = randomString()
        predict.trackCategoryView(categoryPath)
        verify(mockPredictInternal).trackCategoryView(categoryPath)
    }

    @Test
    fun testPredict_trackSearchTerm_delegatesTo_predictInternal() {
        val searchTerm = randomString()
        predict.trackSearchTerm(searchTerm)
        verify(mockPredictInternal).trackSearchTerm(searchTerm)
    }

    @Test
    fun testPredict_trackTag_delegatesTo_predictInternal() {
        predict.trackTag("testTag", HashMap())
        verify(mockPredictInternal).trackTag("testTag", HashMap())
    }

    @Test
    fun testPredict_recommendProducts_limit_mustBeAPositiveInteger() {
        shouldThrow<IllegalArgumentException> {
            predict.recommendProducts(mockLogic, -5, mockResultListener)
        }
    }

    @Test
    fun testPredict_recommendProductWithLimit_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, 5, mockResultListener)
        verify(mockPredictInternal).recommendProducts(mockLogic, 5, null, null, mockResultListener)
    }

    @Test
    fun testPredict_recommendProductWithLimitAndResultListenerCallback_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, 5, resultListenerCallback)
        verify(mockPredictInternal).recommendProducts(
            eq(mockLogic),
            eq(5),
            isNull(),
            isNull(),
            anyNotNull()
        )
    }

    @Test
    fun testPredict_recommendProductWithLimitAndAvailabilityZone_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, 5, AVAILABILITY_ZONE, mockResultListener)
        verify(mockPredictInternal).recommendProducts(mockLogic, 5, null, "HU", mockResultListener)
    }

    @Test
    fun testPredict_recommendProductWithLimitAndAvailabilityZoneAndResultListenerCallback_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, 5, AVAILABILITY_ZONE, resultListenerCallback)
        verify(mockPredictInternal).recommendProducts(
            eq(mockLogic),
            eq(5),
            isNull(),
            eq("HU"),
            anyNotNull()
        )
    }

    @Test
    fun testPredict_recommendProductWithAvailabilityZone_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, AVAILABILITY_ZONE, mockResultListener)
        verify(mockPredictInternal).recommendProducts(
            mockLogic,
            null,
            null,
            "HU",
            mockResultListener
        )
    }

    @Test
    fun testPredict_recommendProductWithAvailabilityZoneAndResultListenerCallback_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, AVAILABILITY_ZONE, resultListenerCallback)
        verify(mockPredictInternal).recommendProducts(
            eq(mockLogic),
            isNull(),
            isNull(),
            eq("HU"),
            anyNotNull()
        )
    }

    @Test
    fun testPredict_recommendProductsWithFilters_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, mockRecommendationFilters, mockResultListener)
        verify(mockPredictInternal).recommendProducts(
            mockLogic,
            null,
            mockRecommendationFilters,
            null,
            mockResultListener
        )
    }

    @Test
    fun testPredict_recommendProductsWithFiltersAndResultListenerCallback_delegatesTo_predictInternal() {
        val resultListenerCallback: (Try<List<Product>>) -> Unit = mock()
        predict.recommendProducts(mockLogic, mockRecommendationFilters, resultListenerCallback)
        verify(mockPredictInternal).recommendProducts(
            eq(mockLogic),
            isNull(),
            eq(mockRecommendationFilters),
            isNull(),
            anyNotNull()
        )
    }

    @Test
    fun testPredict_recommendProductsWithFiltersAndAvailabilityZone_delegatesTo_predictInternal() {
        predict.recommendProducts(
            mockLogic,
            mockRecommendationFilters,
            AVAILABILITY_ZONE,
            mockResultListener
        )
        verify(mockPredictInternal).recommendProducts(
            mockLogic,
            null,
            mockRecommendationFilters,
            "HU",
            mockResultListener
        )
    }

    @Test
    fun testPredict_recommendProductsWithFiltersAndAvailabilityZoneAndResultListenerCallback_delegatesTo_predictInternal() {
        predict.recommendProducts(
            mockLogic,
            mockRecommendationFilters,
            AVAILABILITY_ZONE,
            resultListenerCallback
        )
        verify(mockPredictInternal).recommendProducts(
            eq(mockLogic),
            isNull(),
            eq(mockRecommendationFilters),
            eq("HU"),
            anyNotNull()
        )
    }

    @Test
    fun testPredict_recommendProductsWithFiltersAndLimit_limit_mustBeAPositiveInteger() {
        shouldThrow<IllegalArgumentException> {
            predict.recommendProducts(mockLogic, mockRecommendationFilters, -5, mockResultListener)
        }
    }

    @Test
    fun testPredict_recommendProductsWithFiltersAndLimit_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, mockRecommendationFilters, 123, mockResultListener)
        verify(mockPredictInternal).recommendProducts(
            mockLogic,
            123,
            mockRecommendationFilters,
            null,
            mockResultListener
        )
    }

    @Test
    fun testPredict_recommendProductsWithFiltersAndLimitAndResultListenerCallback_delegatesTo_predictInternal() {
        val resultListenerCallback: (Try<List<Product>>) -> Unit = mock()
        predict.recommendProducts(mockLogic, mockRecommendationFilters, 123, resultListenerCallback)
        verify(mockPredictInternal).recommendProducts(
            eq(mockLogic),
            eq(123),
            eq(mockRecommendationFilters),
            isNull(),
            anyNotNull()
        )
    }

    @Test
    fun testPredict_recommendProductsWithFiltersAndLimitAndAvailabilityZone_delegatesTo_predictInternal() {
        predict.recommendProducts(
            mockLogic,
            mockRecommendationFilters,
            123,
            AVAILABILITY_ZONE,
            mockResultListener
        )
        verify(mockPredictInternal).recommendProducts(
            mockLogic,
            123,
            mockRecommendationFilters,
            "HU",
            mockResultListener
        )
    }

    @Test
    fun testPredict_recommendProducts_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, mockResultListener)
        verify(mockPredictInternal).recommendProducts(
            mockLogic,
            null,
            null,
            null,
            mockResultListener
        )
    }

    @Test
    fun testPredict_recommendProductsWithResultListenerCallback_delegatesTo_predictInternal() {
        val resultListenerCallback: (Try<List<Product>>) -> Unit = mock()
        predict.recommendProducts(mockLogic, resultListenerCallback)
        verify(mockPredictInternal).recommendProducts(
            eq(mockLogic),
            isNull(),
            isNull(),
            isNull(),
            anyNotNull()
        )
    }

    @Test
    fun testPredict_recommendProductsWithFunction_delegatesTo_predictInternal() {
        val resultListenerCallback: (Try<List<Product>>) -> Unit = mock()
        predict.recommendProducts(
            mockLogic,
            mockRecommendationFilters,
            123,
            AVAILABILITY_ZONE,
            resultListenerCallback
        )
        verify(mockPredictInternal).recommendProducts(
            eq(mockLogic),
            eq(123),
            eq(mockRecommendationFilters),
            eq("HU"),
            anyNotNull()
        )
    }

    @Test
    fun testPredict_trackRecommendationClick_delegatesTo_predictInternal() {
        val product = Product(
            randomString(),
            randomString(),
            "https://emarsys.com",
            randomString(),
            randomNumberString()
        )
        predict.trackRecommendationClick(product)
        verify(mockPredictInternal).trackRecommendationClick(product)
    }

    private fun createItem(id: String, price: Double, quantity: Double): CartItem {
        return object : CartItem {
            override val itemId: String
                get() = id
            override val price: Double
                get() = price
            override val quantity: Double
                get() = quantity
        }
    }
}