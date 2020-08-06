package com.emarsys.core.request.factory

import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware
import com.emarsys.core.worker.Worker

@Mockable
class CoreCompletionHandlerMiddlewareProvider(
        private val requestRepository: Repository<RequestModel, SqlSpecification>,
        private val uiHandler: Handler,
        private val coreSdkHandler: Handler) : CompletionHandlerProxyProvider {


    override fun provideProxy(worker: Worker?, completionHandler: CoreCompletionHandler?): CoreCompletionHandler {
        return CoreCompletionHandlerMiddleware(worker, requestRepository, uiHandler, coreSdkHandler, completionHandler)
    }
}