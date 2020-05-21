package com.emarsys

import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.di.DependencyInjection
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.predict.Predict
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.testUtil.RandomTestUtils.randomNumberString
import com.emarsys.testUtil.RandomTestUtils.randomString
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import java.util.*

class PredictTest {
    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var predict: Predict
    private lateinit var mockResultListener: ResultListener<Try<List<Product>>>
    private lateinit var mockLogic: Logic
    private lateinit var mockRecommendationFilters: List<RecommendationFilter>

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockPredictInternal = mock()

        val dependencyContainer = FakeDependencyContainer(predictInternal = mockPredictInternal)

        DependencyInjection.setup(dependencyContainer)

        predict = Predict()
        mockResultListener = mock()
        mockLogic = mock()
        val mockRecommendationFilter: RecommendationFilter = mock()
        mockRecommendationFilters = listOf(mockRecommendationFilter)
    }

    @Test
    fun testPredict_trackCart_delegatesTo_Predict_Internal() {
        val itemList = listOf(
                createItem("itemId0", 200.0, 100.0),
                createItem("itemId1", 201.0, 101.0),
                createItem("itemId2", 202.0, 102.0))
        predict.trackCart(itemList)
        Mockito.verify(mockPredictInternal).trackCart(itemList)
    }

    @Test
    fun testPredict_trackPurchase_delegatesTo_Predict_Internal() {
        val orderId = "id"
        val itemList = Arrays.asList(
                createItem("itemId0", 200.0, 100.0),
                createItem("itemId1", 201.0, 101.0),
                createItem("itemId2", 202.0, 102.0))
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
        Mockito.verify(mockPredictInternal).trackCategoryView(categoryPath)
    }

    @Test
    fun testPredict_trackSearchTerm_delegatesTo_predictInternal() {
        val searchTerm = randomString()
        predict.trackSearchTerm(searchTerm)
        Mockito.verify(mockPredictInternal).trackSearchTerm(searchTerm)
    }


    @Test
    fun testPredict_trackTag_delegatesTo_predictInternal() {
        predict.trackTag("testTag", HashMap())
        Mockito.verify(mockPredictInternal).trackTag("testTag", HashMap())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPredict_recommendProducts_limit_mustBeAPositiveInteger() {
        predict.recommendProducts(mockLogic, -5, mockResultListener)
    }

    @Test
    fun testPredict_recommendProductWithLimit_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, 5, mockResultListener)
        Mockito.verify(mockPredictInternal).recommendProducts(mockLogic, 5, null, mockResultListener)
    }


    @Test
    fun testPredict_recommendProductsWithFilters_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, mockRecommendationFilters, mockResultListener)
        Mockito.verify(mockPredictInternal).recommendProducts(mockLogic, null, mockRecommendationFilters, mockResultListener)
    }


    @Test(expected = IllegalArgumentException::class)
    fun testPredict_recommendProductsWithFiltersAndLimit_limit_mustBeAPositiveInteger() {
        predict.recommendProducts(mockLogic, mockRecommendationFilters, -5, mockResultListener)
    }


    @Test
    fun testPredict_recommendProductsWithFiltersAndLimit_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, mockRecommendationFilters, 123, mockResultListener)
        Mockito.verify(mockPredictInternal).recommendProducts(mockLogic, 123, mockRecommendationFilters, mockResultListener)
    }

    @Test
    fun testPredict_recommendProducts_delegatesTo_predictInternal() {
        predict.recommendProducts(mockLogic, mockResultListener)
        Mockito.verify(mockPredictInternal).recommendProducts(mockLogic, null, null, mockResultListener)
    }

    @Test
    fun testPredict_trackRecommendationClick_delegatesTo_predictInternal() {
        val product = Product.Builder(randomString(), randomString(), "https://emarsys.com", randomString(), randomNumberString()).build()
        predict.trackRecommendationClick(product)
        Mockito.verify(mockPredictInternal).trackRecommendationClick(product)
    }

    private fun createItem(id: String, price: Double, quantity: Double): CartItem {
        return object : CartItem {
            override fun getItemId(): String {
                return id
            }

            override fun getPrice(): Double {
                return price
            }

            override fun getQuantity(): Double {
                return quantity
            }
        }
    }
}