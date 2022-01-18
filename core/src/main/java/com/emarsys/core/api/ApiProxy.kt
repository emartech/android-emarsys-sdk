package com.emarsys.core.api

import com.emarsys.core.handler.ConcurrentHandlerHolder


inline fun <reified T : Any> T.proxyApi(handlerHolder: ConcurrentHandlerHolder): T {
    return this.proxyWithLogExceptions().proxyWithHandler(handlerHolder)
}