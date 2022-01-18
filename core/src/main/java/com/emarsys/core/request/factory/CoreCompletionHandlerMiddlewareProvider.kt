package com.emarsys.core.request.factory

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware
import com.emarsys.core.worker.Worker

@Mockable
class CoreCompletionHandlerMiddlewareProvider(
    private val requestRepository: Repository<RequestModel, SqlSpecification>,
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
) : CompletionHandlerProxyProvider {


    override fun provideProxy(
        worker: Worker?,
        completionHandler: CoreCompletionHandler?
    ): CoreCompletionHandler {
        return CoreCompletionHandlerMiddleware(
            worker,
            requestRepository,
            concurrentHandlerHolder,
            completionHandler
        )
    }
}