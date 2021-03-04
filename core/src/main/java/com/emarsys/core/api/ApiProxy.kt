package com.emarsys.core.api

import com.emarsys.core.handler.CoreSdkHandler


inline fun <reified T : Any> T.proxyApi(handler: CoreSdkHandler): T {
    return this.proxyWithLogExceptions().proxyWithHandler(handler)
}