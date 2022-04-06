package com.emarsys.core.worker

import android.os.Handler
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mockable

@Mockable
class DelegatorCompletionHandlerProvider {

    fun provide(handler: Handler, completionHandler: CoreCompletionHandler): CoreCompletionHandler {
        return DelegatorCompletionHandler(handler, completionHandler)
    }
}