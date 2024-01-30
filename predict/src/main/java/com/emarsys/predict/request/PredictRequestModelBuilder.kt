package com.emarsys.predict.request

import android.net.Uri
import android.text.TextUtils
import com.emarsys.core.Mockable
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.util.JsonUtils
import com.emarsys.predict.DefaultPredictInternal
import com.emarsys.predict.api.model.Logic
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.api.model.RecommendationLogic
import com.emarsys.predict.model.LastTrackedItemContainer
import java.util.*

@Mockable
class PredictRequestModelBuilder(private val requestContext: PredictRequestContext,
                                 private val headerFactory: PredictHeaderFactory,
                                 private val predictServiceEndpointProvider: ServiceEndpointProvider) {

    companion object {
        private const val DEFAULT_LIMIT = 5
    }

    private var shardData: Map<String, Any>? = null
    private var logic: Logic? = null
    private var lastTrackedItemContainer: LastTrackedItemContainer? = null
    private var limit: Int? = null
    private var availabilityZone: String? = null
    private var filters: List<RecommendationFilter>? = null
    private val uriBuilder: Uri.Builder = Uri.parse(predictServiceEndpointProvider.provideEndpointHost())
            .buildUpon()
            .appendPath(requestContext.merchantId)

    fun withShardData(shardData: Map<String, Any>): PredictRequestModelBuilder {
        this.shardData = shardData
        return this
    }

    fun withLogic(logic: Logic, lastTrackedItemContainer: LastTrackedItemContainer): PredictRequestModelBuilder {
        this.logic = logic
        this.lastTrackedItemContainer = lastTrackedItemContainer
        return this
    }

    fun withLimit(limit: Int?): PredictRequestModelBuilder {
        require(limit ?: 1 > 0) { "Limit must be greater than zero, or can be Null!" }
        this.limit = limit
        return this
    }

    fun withAvailabilityZone(availabilityZone: String?): PredictRequestModelBuilder {
        this.availabilityZone = availabilityZone
        return this
    }

    fun withFilters(recommendationFilters: List<RecommendationFilter>?): PredictRequestModelBuilder {
        this.filters = recommendationFilters
        return this
    }

    fun build(): RequestModel {
        val requestModelBuilder = RequestModel.Builder(requestContext.timestampProvider, requestContext.uuidProvider)
                .method(RequestMethod.GET)
                .headers(headerFactory.createBaseHeader())

        if (logic != null) {
            requestModelBuilder.url(createRecommendationUrl(logic!!))
        } else {
            requestModelBuilder.url(createUrl(shardData!!))
        }
        return requestModelBuilder.build()
    }

    private fun createRecommendationUrl(logic: Logic): String {
        if (limit == null) {
            limit = DEFAULT_LIMIT
        }
        val keyValueStore = requestContext.keyValueStore

        val visitorId = keyValueStore.getString(DefaultPredictInternal.VISITOR_ID_KEY)

        if (visitorId != null) {
            uriBuilder.appendQueryParameter("vi", visitorId)
        }

        val contactId = keyValueStore.getString(DefaultPredictInternal.CONTACT_FIELD_VALUE_KEY)

        if (contactId != null) {
            uriBuilder.appendQueryParameter("ci", contactId)
        }
        if (availabilityZone != null) {
            uriBuilder.appendQueryParameter("az", availabilityZone)
        }

        if (this.filters != null) {
            uriBuilder.appendQueryParameter("ex", createRecommendationFilterQueryValues())
        }

        val url: String = if (RecommendationLogic.PERSONAL == logic.logicName
                || RecommendationLogic.HOME == logic.logicName) {
            createUrlWithVariants(logic)
        } else {
            createUrlWithData(logic)
        }

        uriBuilder.clearQuery()
        return url
    }

    private fun createUrlWithData(logic: Logic): String {
        uriBuilder.appendQueryParameter("f", "f:${logic.logicName},l:$limit,o:0")

        val data = logic.data.toMutableMap()

        if (data.isEmpty()) {
            when (logic.logicName) {
                RecommendationLogic.SEARCH -> if (lastTrackedItemContainer!!.lastSearchTerm != null) {
                    data.putAll(RecommendationLogic.search(lastTrackedItemContainer!!.lastSearchTerm).data)
                }
                RecommendationLogic.CART -> if (lastTrackedItemContainer!!.lastCartItems != null) {
                    data.putAll(RecommendationLogic.cart(lastTrackedItemContainer!!.lastCartItems).data)
                }
                RecommendationLogic.CATEGORY -> if (lastTrackedItemContainer!!.lastCategoryPath != null) {
                    data.putAll(RecommendationLogic.category(lastTrackedItemContainer!!.lastCategoryPath).data)
                }
                RecommendationLogic.POPULAR -> if (lastTrackedItemContainer!!.lastCategoryPath != null) {
                    data.putAll(RecommendationLogic.popular(lastTrackedItemContainer!!.lastCategoryPath).data)
                }
                RecommendationLogic.RELATED -> if (lastTrackedItemContainer!!.lastItemView != null) {
                    data.putAll(RecommendationLogic.related(lastTrackedItemContainer!!.lastItemView).data)
                }
                RecommendationLogic.ALSO_BOUGHT -> if (lastTrackedItemContainer!!.lastItemView != null) {
                    data.putAll(RecommendationLogic.alsoBought(lastTrackedItemContainer!!.lastItemView).data)
                }
            }
        }
        data.keys.forEach {
            uriBuilder.appendQueryParameter(it, data[it])
        }

        return uriBuilder.build().toString()
    }

    private fun createUrlWithVariants(logic: Logic): String {
        val variants = logic.variants

        if (variants.isEmpty()) {
            uriBuilder.appendQueryParameter("f", "f:${logic.logicName},l:$limit,o:0")
        } else {
            val params = ArrayList<String>()
            variants.forEach {
                params.add("f:${logic.logicName}_$it,l:$limit,o:0")
            }
            uriBuilder.appendQueryParameter("f", TextUtils.join("|", params))
        }
        return uriBuilder.build().toString()
    }

    private fun createUrl(shardData: Map<String, Any>): String {
        val uriBuilder = Uri.parse(predictServiceEndpointProvider.provideEndpointHost())
                .buildUpon()
                .appendPath(requestContext.merchantId)

        shardData.keys.forEach {
            uriBuilder.appendQueryParameter(it, shardData[it].toString())
        }

        return uriBuilder.build().toString()
    }

    private fun createRecommendationFilterQueryValues(): String {
        val recommendationFilterQueryValues = mutableListOf<Any>()

        filters?.forEach {
            val recommendationFilterQueryValue = mutableMapOf<String, Any>()
            recommendationFilterQueryValue["f"] = it.field
            recommendationFilterQueryValue["r"] = it.comparison
            recommendationFilterQueryValue["v"] = TextUtils.join("|", it.expectations)
            recommendationFilterQueryValue["n"] = it.type != "EXCLUDE"
            recommendationFilterQueryValues.add(recommendationFilterQueryValue)
        }

        return JsonUtils.fromList(recommendationFilterQueryValues).toString()
    }
}
