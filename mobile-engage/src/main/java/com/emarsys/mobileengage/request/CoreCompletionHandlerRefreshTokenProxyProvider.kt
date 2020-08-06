package com.emarsys.mobileengage.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.worker.Worker
import com.emarsys.mobileengage.RefreshTokenInternal

class CoreCompletionHandlerRefreshTokenProxyProvider(
        private val coreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider,
        private val refreshTokenInternal: RefreshTokenInternal,
        private val restClient: RestClient,
        private val contactTokenStorage: StringStorage,
        private val pushTokenStorage: StringStorage,
        private val clientServiceProvider: ServiceEndpointProvider,
        private val eventServiceProvider: ServiceEndpointProvider,
        private val messageInboxServiceProvider: ServiceEndpointProvider,
        private val defaultHandler: CoreCompletionHandler) : CompletionHandlerProxyProvider {

    override fun provideProxy(worker: Worker?, completionHandler: CoreCompletionHandler?): CoreCompletionHandlerRefreshTokenProxy {
        var coreCompletionHandler = defaultHandler
        if (completionHandler != null) {
            coreCompletionHandler = completionHandler
        }
        if (worker != null) {
            coreCompletionHandler = coreCompletionHandlerMiddlewareProvider.provideProxy(worker, coreCompletionHandler)
        }
        return CoreCompletionHandlerRefreshTokenProxy(coreCompletionHandler, refreshTokenInternal, restClient, contactTokenStorage,
                pushTokenStorage, clientServiceProvider, eventServiceProvider, messageInboxServiceProvider)
    }
}