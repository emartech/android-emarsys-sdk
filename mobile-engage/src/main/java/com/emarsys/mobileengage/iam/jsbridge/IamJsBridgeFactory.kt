package com.emarsys.mobileengage.iam.jsbridge

import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder

@Mockable
class IamJsBridgeFactory(private val concurrentHandlerHolder: ConcurrentHandlerHolder) {

    fun createJsBridge(
        jsCommandFactory: JSCommandFactory
    ): IamJsBridge {
        return IamJsBridge(concurrentHandlerHolder, jsCommandFactory)
    }
}