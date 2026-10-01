package com.emarsys.predict.shard

import com.emarsys.core.Mapper
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.predict.DefaultPredictInternal
import com.emarsys.predict.provider.PredictRequestModelBuilderProvider
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.request.PredictRequestModelBuilder

class PredictShardListMerger(
    private val predictRequestContext: PredictRequestContext,
    predictRequestModelBuilderProvider: PredictRequestModelBuilderProvider
) : Mapper<List<ShardModel>, RequestModel> {

    private val predictRequestModelBuilder: PredictRequestModelBuilder =
        predictRequestModelBuilderProvider.providePredictRequestModelBuilder()

    override fun map(shards: List<ShardModel>): RequestModel {
        require(shards.isNotEmpty()) { "Shards must not be empty!" }

        val shardData = mergeShardData(shards)
        return predictRequestModelBuilder.withShardData(shardData).build()
    }

    private fun mergeShardData(shards: List<ShardModel>): Map<String, Any> {
        return linkedMapOf<String, Any>().also { result ->
            insertBaseParameters(result)
            shards.forEach { shard ->
                @Suppress("UNCHECKED_CAST")
                result.putAll(shard.data as Map<String, Any>)
            }
        }
    }

    private fun insertBaseParameters(result: LinkedHashMap<String, Any>) {
        result["cp"] = 1
        val visitorId = predictRequestContext.keyValueStore.getString(DefaultPredictInternal.VISITOR_ID_KEY)
        if (visitorId != null) {
            result["vi"] = visitorId
        }
    }
}
