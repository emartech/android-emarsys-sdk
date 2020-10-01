package com.emarsys.predict

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.di.getDependency
import com.emarsys.core.util.Assert
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter

@Mockable
class Predict(private val loggingInstance: Boolean = false) : PredictApi {

    override fun trackCart(items: List<CartItem>) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .trackCart(items)

    }

    override fun trackPurchase(orderId: String,
                               items: List<CartItem>) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .trackPurchase(orderId, items)

    }

    override fun trackItemView(itemId: String) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .trackItemView(itemId)
    }

    override fun trackCategoryView(categoryPath: String) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .trackCategoryView(categoryPath)
    }

    override fun trackSearchTerm(searchTerm: String) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .trackSearchTerm(searchTerm)
    }

    override fun trackTag(tag: String, attributes: Map<String, String>?) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .trackTag(tag, attributes)
    }

    override fun recommendProducts(recommendationLogic: Logic, resultListener: ResultListener<Try<List<Product>>>) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, null, null, null, resultListener)
    }

    override fun recommendProducts(recommendationLogic: Logic, limit: Int, resultListener: ResultListener<Try<List<Product>>>) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, null, null, resultListener)
    }

    override fun recommendProducts(recommendationLogic: Logic,
                                   recommendationFilters: List<RecommendationFilter>,
                                   limit: Int,
                                   availabilityZone: String,
                                   resultListener: ResultListener<Try<List<Product>>>) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, recommendationFilters, availabilityZone, resultListener)
    }

    override fun recommendProducts(recommendationLogic: Logic, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, null, null, availabilityZone, resultListener)
    }

    override fun recommendProducts(recommendationLogic: Logic, limit: Int, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, null, availabilityZone, resultListener)
    }

    override fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, resultListener: ResultListener<Try<List<Product>>>) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, null, recommendationFilters, null, resultListener)
    }

    override fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, availabilityZone: String, resultListener: ResultListener<Try<List<Product>>>) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, null, recommendationFilters, availabilityZone, resultListener)
    }

    override fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, resultListener: ResultListener<Try<List<Product>>>) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, recommendationFilters, null, resultListener)
    }

    override fun recommendProducts(recommendationLogic: Logic, resultListener: (Try<List<Product>>) -> Unit) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, null, null, null, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun recommendProducts(recommendationLogic: Logic, availabilityZone: String, resultListener: (Try<List<Product>>) -> Unit) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, null, null, availabilityZone, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun recommendProducts(recommendationLogic: Logic, limit: Int, resultListener: (Try<List<Product>>) -> Unit) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, null, null, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun recommendProducts(recommendationLogic: Logic, limit: Int, availabilityZone: String, resultListener: (Try<List<Product>>) -> Unit) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, null, availabilityZone, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, resultListener: (Try<List<Product>>) -> Unit) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, null, recommendationFilters, null, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, availabilityZone: String, resultListener: (Try<List<Product>>) -> Unit) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, null, recommendationFilters, availabilityZone, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, resultListener: (Try<List<Product>>) -> Unit) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, recommendationFilters, null, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>, limit: Int, availabilityZone: String, resultListener: (Try<List<Product>>) -> Unit) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, recommendationFilters, availabilityZone, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun recommendProducts(recommendationLogic: Logic, recommendationFilters: List<RecommendationFilter>?, limit: Int?, availabilityZone: String?, resultListener: (Try<List<Product>>) -> Unit) {
        Assert.positiveInt(limit, "Limit must be greater than zero!")
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .recommendProducts(recommendationLogic, limit, recommendationFilters, availabilityZone, ResultListener { result -> resultListener.invoke(result) })
    }

    override fun trackRecommendationClick(product: Product) {
        (if (loggingInstance) getDependency("loggingInstance") else getDependency<PredictInternal>("defaultInstance"))
                .trackRecommendationClick(product)
    }

}