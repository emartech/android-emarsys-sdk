package com.emarsys.predict

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.util.Assert
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.di.predict

@Mockable
class Predict(private val loggingInstance: Boolean = false) : PredictApi {

    override fun trackCart(items: List<CartItem>) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .trackCart(items)

    }

    override fun trackPurchase(orderId: String,
                               items: List<CartItem>) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .trackPurchase(orderId, items)

    }

    override fun trackItemView(itemId: String) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .trackItemView(itemId)
    }

    override fun trackCategoryView(categoryPath: String) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .trackCategoryView(categoryPath)
    }

    override fun trackSearchTerm(searchTerm: String) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .trackSearchTerm(searchTerm)
    }

    override fun trackTag(tag: String, attributes: Map<String, String>?) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .trackTag(tag, attributes)
    }

    override fun recommendProducts(logic: Logic, resultListener: ResultListener<Try<List<Product>>>) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .recommendProducts(logic, null, null, null, resultListener)
    }

    override fun recommendProducts(logic: Logic, limit: Int, resultListener: ResultListener<Try<List<Product>>>) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .recommendProducts(logic, limit, null, null, resultListener)
    }

    override fun recommendProducts(logic: Logic,
                                   filters: List<RecommendationFilter>,
                                   limit: Int,
                                   availabilityZone: String,
                                   resultListener: ResultListener<Try<List<Product>>>) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .recommendProducts(logic, limit, filters, availabilityZone, resultListener)
    }

    override fun recommendProducts(logic: Logic, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .recommendProducts(logic, null, null, availabilityZone, resultListener)
    }

    override fun recommendProducts(logic: Logic, limit: Int, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .recommendProducts(logic, limit, null, availabilityZone, resultListener)
    }

    override fun recommendProducts(logic: Logic, filters: List<RecommendationFilter>, resultListener: ResultListener<Try<List<Product>>>) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .recommendProducts(logic, null, filters, null, resultListener)
    }

    override fun recommendProducts(logic: Logic, filters: List<RecommendationFilter>, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .recommendProducts(logic, null, filters, availabilityZone, resultListener)
    }

    override fun recommendProducts(logic: Logic, filters: List<RecommendationFilter>, limit: Int, resultListener: ResultListener<Try<List<Product>>>) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .recommendProducts(logic, limit, filters, null, resultListener)
    }

    override fun trackRecommendationClick(product: Product) {
        (if (loggingInstance) predict().loggingPredictInternal else predict().predictInternal)
                .trackRecommendationClick(product)
    }

}