package com.emarsys.core.request.factory

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable
import com.emarsys.core.ScopeDelegatorCompletionHandler
import kotlinx.coroutines.CoroutineScope

@Mockable
class ScopeDelegatorCompletionHandlerProvider {
    fun provide(
        completionHandler: CoreCompletionHandler,
        scope: CoroutineScope
    ): CoreCompletionHandler {
        return ScopeDelegatorCompletionHandler(completionHandler, scope)
    }
}