package com.emarsys.core.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.ScopeDelegatorCompletionHandlerProvider
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.worker.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Mockable
class RequestManager(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val requestRepository: Repository<RequestModel, SqlSpecification>,
    private val shardRepository: Repository<ShardModel, SqlSpecification>,
    private val worker: Worker,
    private val restClient: RestClient,
    private val callbackRegistry: Registry<RequestModel, CompletionListener?>,
    private val defaultCoreCompletionHandler: CoreCompletionHandler,
    private val completionHandlerProxyProvider: CompletionHandlerProxyProvider,
    private val scopeDelegatorCompletionHandlerProvider: ScopeDelegatorCompletionHandlerProvider,
) {
    fun submit(model: RequestModel, callback: CompletionListener?) {
        concurrentHandlerHolder.sdkScope.launch {
            requestRepository.add(
                model
            )
            callbackRegistry.register(model, callback)
            worker.run()
        }
    }

    fun submit(model: ShardModel) {
        concurrentHandlerHolder.sdkScope.launch {
            shardRepository.add(model)
        }
    }

    fun submitNow(requestModel: RequestModel) {
        val handler =
            completionHandlerProxyProvider.provideProxy(null, defaultCoreCompletionHandler)
        submitNow(requestModel, handler)
    }

    fun submitNow(
        requestModel: RequestModel,
        completionHandler: CoreCompletionHandler,
        scope: CoroutineScope = concurrentHandlerHolder.sdkScope
    ) {
        val scopedHandler =
            scopeDelegatorCompletionHandlerProvider.provide(completionHandler, scope)
        val handler = completionHandlerProxyProvider.provideProxy(null, scopedHandler)
        restClient.execute(requestModel, handler)
    }
}