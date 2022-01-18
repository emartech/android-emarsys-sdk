package com.emarsys.core.util.batch

import com.emarsys.core.Mapper
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.shard.specification.FilterByShardIds
import com.emarsys.core.util.predicate.Predicate
import kotlinx.coroutines.runBlocking

class BatchingShardTrigger(
    private val repository: Repository<ShardModel, SqlSpecification>,
    private val predicate: Predicate<List<ShardModel>>,
    private val querySpecification: SqlSpecification,
    private val chunker: Mapper<List<ShardModel>, List<List<ShardModel>>>,
    private val merger: Mapper<List<ShardModel>, RequestModel>,
    private val requestManager: RequestManager,
    private val requestStrategy: RequestStrategy,
    private val connectionWatchDog: ConnectionWatchDog
) : Runnable {

    enum class RequestStrategy {
        PERSISTENT, TRANSIENT
    }

    override fun run() {
        if (connectionWatchDog.isConnected) {
            val shards = repository.query(querySpecification)
            if (predicate.evaluate(shards)) {
                val chunks = chunker.map(shards)
                chunks.forEach {
                    submit(merger.map(it))
                    runBlocking {
                        repository.remove(FilterByShardIds(it))
                    }
                }
            }
        }
    }

    private fun submit(requestModel: RequestModel) {
        if (requestStrategy == RequestStrategy.PERSISTENT) {
            requestManager.submit(requestModel, null)
        } else if (requestStrategy == RequestStrategy.TRANSIENT) {
            requestManager.submitNow(requestModel)
        }
    }
}