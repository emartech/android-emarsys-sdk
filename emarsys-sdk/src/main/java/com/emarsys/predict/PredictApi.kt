package com.emarsys.predict

import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter

interface PredictApi {
    fun trackCart(items: List<CartItem>)
    fun trackPurchase(orderId: String, items: List<CartItem>)

    fun trackItemView(itemId: String)
    fun trackCategoryView(categoryPath: String)
    fun trackSearchTerm(searchTerm: String)
    fun trackTag(tag: String, attributes: Map<String, String>?)
    fun recommendProducts(recommendationLogic: Logic, resultListener: ResultListener<Try<List<Product>>>)
    fun recommendProducts(recommendationLogic: Logic, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>)
    fun recommendProducts(recommendationLogic: Logic, limit: Int, resultListener: ResultListener<Try<List<Product>>>)
    fun recommendProducts(recommendationLogic: Logic, limit: Int, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>)
    fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, resultListener: ResultListener<Try<List<Product>>>)
    fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>)
    fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, resultListener: ResultListener<Try<List<Product>>>)
    fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>)

    fun trackRecommendationClick(product: Product)
}