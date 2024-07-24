package com.emarsys.predict

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.api.ResponseErrorException
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.api.result.Try.Companion.failure
import com.emarsys.core.api.result.Try.Companion.success
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.core.util.Assert
import com.emarsys.core.util.JsonUtils.fromMap
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.model.LastTrackedItemContainer
import com.emarsys.predict.provider.PredictRequestModelBuilderProvider
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.util.CartItemUtils
import java.net.URLEncoder

class DefaultPredictInternal(
    requestContext: PredictRequestContext,
    private val requestManager: RequestManager,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val requestModelBuilderProvider: PredictRequestModelBuilderProvider,
    private val responseMapper: PredictResponseMapper,
    private val lastTrackedContainer: LastTrackedItemContainer = LastTrackedItemContainer()
) : PredictInternal {

    companion object {
        const val VISITOR_ID_KEY = "predict_visitor_id"
        const val XP_KEY = "xp"
        const val CONTACT_FIELD_VALUE_KEY = "predict_contact_id"
        const val CONTACT_FIELD_ID_KEY = "predict_contact_field_id"
        private const val TYPE_CART = "predict_cart"
        private const val TYPE_PURCHASE = "predict_purchase"
        private const val TYPE_ITEM_VIEW = "predict_item_view"
        private const val TYPE_CATEGORY_VIEW = "predict_category_view"
        private const val TYPE_SEARCH_TERM = "predict_search_term"
        private const val TYPE_TAG = "predict_tag"
    }

    private val uuidProvider: UUIDProvider = requestContext.uuidProvider
    private val timestampProvider: TimestampProvider = requestContext.timestampProvider
    private val keyValueStore: KeyValueStore = requestContext.keyValueStore


    override fun setContact(contactFieldId: Int, contactFieldValue: String) {
        keyValueStore.putString(CONTACT_FIELD_VALUE_KEY, contactFieldValue)
        keyValueStore.putInt(CONTACT_FIELD_ID_KEY, contactFieldId)
    }

    override fun clearContact() {
        keyValueStore.remove(CONTACT_FIELD_VALUE_KEY)
        keyValueStore.remove(CONTACT_FIELD_ID_KEY)
        keyValueStore.remove(VISITOR_ID_KEY)
    }

    override fun trackCart(items: List<CartItem>): String {
        val shard = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE_CART)
            .payloadEntry("cv", 1)
            .payloadEntry("ca", CartItemUtils.cartItemsToQueryParam(items))
            .build()
        requestManager.submit(shard)
        lastTrackedContainer.lastCartItems = items
        return shard.id
    }

    override fun trackPurchase(orderId: String, items: List<CartItem>): String {
        Assert.notEmpty(items, "Items must not be empty!")

        val shard = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE_PURCHASE)
            .payloadEntry("oi", orderId)
            .payloadEntry("co", CartItemUtils.cartItemsToQueryParam(items))
            .build()
        requestManager.submit(shard)
        lastTrackedContainer.lastCartItems = items
        return shard.id
    }

    override fun trackItemView(itemId: String): String {
        val shard = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE_ITEM_VIEW)
            .payloadEntry("v", "i:${URLEncoder.encode(itemId, Charsets.UTF_8)}")
            .build()
        requestManager.submit(shard)
        lastTrackedContainer.lastItemView = itemId
        return shard.id
    }

    override fun trackCategoryView(categoryPath: String): String {
        val shard = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE_CATEGORY_VIEW)
            .payloadEntry("vc", categoryPath)
            .build()
        requestManager.submit(shard)
        lastTrackedContainer.lastCategoryPath = categoryPath
        return shard.id
    }

    override fun trackSearchTerm(searchTerm: String): String {
        val shard = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE_SEARCH_TERM)
            .payloadEntry("q", searchTerm)
            .build()
        requestManager.submit(shard)
        lastTrackedContainer.lastSearchTerm = searchTerm
        return shard.id
    }

    override fun trackTag(tag: String, attributes: Map<String, String>?) {
        val shardBuilder = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE_TAG)
        if (attributes == null) {
            shardBuilder.payloadEntry("t", tag)
        } else {
            val payload: Map<String, Any> = mapOf(
                "name" to tag,
                "attributes" to attributes
            )
            shardBuilder.payloadEntry("ta", fromMap(payload).toString())
        }
        val shard = shardBuilder.build()
        requestManager.submit(shard)
    }

    override fun recommendProducts(
        recommendationLogic: Logic,
        limit: Int?,
        recommendationFilters: List<RecommendationFilter>?,
        availabilityZone: String?,
        resultListener: ResultListener<Try<List<Product>>>
    ) {
        val requestModel = requestModelBuilderProvider.providePredictRequestModelBuilder()
            .withLogic(recommendationLogic, lastTrackedContainer)
            .withLimit(limit)
            .withAvailabilityZone(availabilityZone)
            .withFilters(recommendationFilters)
            .build()
        requestManager.submitNow(requestModel, object : CoreCompletionHandler {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                val products = responseMapper.map(responseModel)
                concurrentHandlerHolder.postOnMain {
                    resultListener.onResult(success(products))
                }
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                concurrentHandlerHolder.postOnMain {
                    resultListener.onResult(
                        failure(
                            ResponseErrorException(
                                responseModel.statusCode,
                                responseModel.message,
                                responseModel.body
                            )
                        )
                    )
                }
            }

            override fun onError(id: String, cause: Exception) {
                concurrentHandlerHolder.postOnMain { resultListener.onResult(failure(cause)) }
            }
        })
    }

    override fun trackRecommendationClick(product: Product): String {
        val shard = ShardModel.Builder(timestampProvider, uuidProvider)
            .type(TYPE_ITEM_VIEW)
            .payloadEntry(
                "v",
                "i:" + URLEncoder.encode(product.productId, Charsets.UTF_8) + ",t:" + product.feature + ",c:" + product.cohort
            )
            .build()
        requestManager.submit(shard)
        lastTrackedContainer.lastItemView = product.productId
        return shard.id
    }
}