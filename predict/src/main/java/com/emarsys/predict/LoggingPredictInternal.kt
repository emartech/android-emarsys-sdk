package com.emarsys.predict

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.util.SystemUtils
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.entry.MethodNotAllowed
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter

class LoggingPredictInternal(private val klass: Class<*>) : PredictInternal {
    override fun setContact(
        contactFieldId: Int,
        contactFieldValue: String,
        completionListener: CompletionListener?
    ) {
        val parameters: Map<String, Any?> = mapOf(
            "contact_field_value" to contactFieldValue,
            "contact_field_id" to contactFieldId,
            "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun clearPredictOnlyContact(completionListener: CompletionListener?) {
        val parameters: Map<String, Any?> = mapOf(
            "completion_listener" to (completionListener != null)
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun clearContact() {
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, null))
    }

    override fun trackCart(items: List<CartItem>): String {
        val parameters: Map<String, Any?> = mapOf(
            "items" to items.toString()
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return ""
    }

    override fun trackPurchase(orderId: String, items: List<CartItem>): String {
        val parameters: Map<String, Any?> = mapOf(
            "order_id" to orderId,
            "items" to items.toString()
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return ""
    }

    override fun trackItemView(itemId: String): String {
        val parameters: Map<String, Any?> = mapOf(
            "item_id" to itemId
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return ""
    }

    override fun trackCategoryView(categoryPath: String): String {
        val parameters: Map<String, Any?> = mapOf(
            "category_path" to categoryPath
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return ""
    }

    override fun trackSearchTerm(searchTerm: String): String {
        val parameters: Map<String, Any?> = mapOf(
            "search_term" to searchTerm
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return ""
    }

    override fun trackTag(tag: String, attributes: Map<String, String>?) {
        val parameters: Map<String, Any?> = mapOf(
            "tag" to tag,
            "attributes" to attributes.toString()
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun recommendProducts(
        recommendationLogic: Logic,
        limit: Int?,
        recommendationFilters: List<RecommendationFilter>?,
        availabilityZone: String?,
        resultListener: ResultListener<Try<List<Product>>>
    ) {
        val parameters: Map<String, Any?> = mapOf(
            "recommendation_logic" to recommendationLogic.toString(),
            "result_listener" to true,
            "limit" to limit,
            "recommendation_filter" to recommendationFilters?.toTypedArray()?.contentToString(),
            "availabilityZone" to availabilityZone
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
    }

    override fun trackRecommendationClick(product: Product): String {
        val parameters: Map<String, Any?> = mapOf(
            "product" to product.toString()
        )
        val callerMethodName = SystemUtils.getCallerMethodName()
        debug(MethodNotAllowed(klass, callerMethodName, parameters))
        return ""
    }
}