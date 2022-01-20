package com.emarsys.mobileengage.iam.jsbridge

import com.emarsys.core.Mockable
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.iam.model.InAppMessage

@Mockable
class IamJsBridgeFactory(private val concurrentHandlerHolder: ConcurrentHandlerHolder) {

    fun createJsBridge(
        jsCommandFactory: JSCommandFactory,
        inAppMessage: InAppMessage
    ): IamJsBridge {
        return IamJsBridge(concurrentHandlerHolder, jsCommandFactory, inAppMessage)
    }
}