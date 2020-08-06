package com.emarsys.core.request.factory

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.worker.Worker

interface CompletionHandlerProxyProvider {
    fun provideProxy(worker: Worker?, completionHandler: CoreCompletionHandler?): CoreCompletionHandler
}