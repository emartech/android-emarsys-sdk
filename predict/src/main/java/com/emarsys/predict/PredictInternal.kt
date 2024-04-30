package com.emarsys.predict

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter

interface PredictInternal {
    fun setContact(contactFieldId: Int, contactFieldValue: String, completionListener: CompletionListener?)
    fun clearPredictOnlyContact(completionListener: CompletionListener?)
    fun clearContact()
    fun trackCart(items: List<CartItem>): String
    fun trackPurchase(orderId: String, items: List<CartItem>): String
    fun trackItemView(itemId: String): String
    fun trackCategoryView(categoryPath: String): String
    fun trackSearchTerm(searchTerm: String): String
    fun trackTag(tag: String, attributes: Map<String, String>?)
    fun recommendProducts(recommendationLogic: Logic, limit: Int? = null, recommendationFilters: List<RecommendationFilter>? = null, availabilityZone: String? = null, resultListener: ResultListener<Try<List<Product>>>)
    fun trackRecommendationClick(product: Product): String
}