package com.emarsys.mobileengage.request

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider
import com.emarsys.core.storage.Storage
import com.emarsys.core.worker.Worker
import com.emarsys.mobileengage.responsehandler.MobileEngageTokenResponseHandler
import com.emarsys.mobileengage.util.RequestModelHelper

@Mockable
class CoreCompletionHandlerRefreshTokenProxyProvider(
    private val coreCompletionHandlerMiddlewareProvider: CoreCompletionHandlerMiddlewareProvider,
    private val restClient: RestClient,
    private val contactTokenStorage: Storage<String?>,
    private val pushTokenStorage: Storage<String?>,
    private val defaultHandler: CoreCompletionHandler,
    private val requestModelHelper: RequestModelHelper,
    private val tokenResponseHandler: MobileEngageTokenResponseHandler,
    private val requestModelFactory: MobileEngageRequestModelFactory
) : CompletionHandlerProxyProvider {

    override fun provideProxy(
        worker: Worker?,
        completionHandler: CoreCompletionHandler?
    ): CoreCompletionHandlerRefreshTokenProxy {
        var coreCompletionHandler = defaultHandler
        if (completionHandler != null) {
            coreCompletionHandler = completionHandler
        }
        if (worker != null) {
            coreCompletionHandler =
                coreCompletionHandlerMiddlewareProvider.provideProxy(worker, coreCompletionHandler)
        }
        return CoreCompletionHandlerRefreshTokenProxy(
            coreCompletionHandler, restClient, contactTokenStorage,
            pushTokenStorage, tokenResponseHandler, requestModelHelper, requestModelFactory
        )
    }
}