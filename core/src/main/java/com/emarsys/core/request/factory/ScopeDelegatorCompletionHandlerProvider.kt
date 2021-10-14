package com.emarsys.core.request.factory

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.ScopeDelegatorCompletionHandler
import kotlinx.coroutines.CoroutineScope

class ScopeDelegatorCompletionHandlerProvider {

    fun provide(
        completionHandler: CoreCompletionHandler,
        scope: CoroutineScope
    ): ScopeDelegatorCompletionHandler {
        return ScopeDelegatorCompletionHandler(completionHandler, scope)
    }
}