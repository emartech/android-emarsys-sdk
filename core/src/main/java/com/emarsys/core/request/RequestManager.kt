package com.emarsys.core.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.Registry
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.DefaultRunnableFactory
import com.emarsys.core.request.factory.RunnableFactory
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.worker.Worker

@Mockable
class RequestManager(
    private val coreSDKHandler: CoreSdkHandler,
    private val requestRepository: Repository<RequestModel, SqlSpecification>,
    private val shardRepository: Repository<ShardModel, SqlSpecification>,
    private val worker: Worker,
    private val restClient: RestClient,
    private val callbackRegistry: Registry<RequestModel, CompletionListener?>,
    private val defaultCoreCompletionHandler: CoreCompletionHandler,
    private val completionHandlerProxyProvider: CompletionHandlerProxyProvider
) {
    var runnableFactory : RunnableFactory =  DefaultRunnableFactory()

    fun submit(model: RequestModel, callback: CompletionListener?) {
        coreSDKHandler.post(runnableFactory.runnableFrom {
            requestRepository.add(model)
            callbackRegistry.register(model, callback)
            worker.run()
        })
    }

    fun submit(model: ShardModel) {
        coreSDKHandler.post(runnableFactory.runnableFrom { shardRepository.add(model) })
    }

    fun submitNow(requestModel: RequestModel) {
        val handler =
            completionHandlerProxyProvider.provideProxy(null, defaultCoreCompletionHandler)
        submitNow(requestModel, handler)
    }

    fun submitNow(requestModel: RequestModel, completionHandler: CoreCompletionHandler) {
        val handler = completionHandlerProxyProvider.provideProxy(null, completionHandler)
        restClient.execute(requestModel, handler)
    }
}