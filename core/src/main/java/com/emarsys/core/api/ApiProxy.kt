package com.emarsys.core.api

import android.os.Handler


inline fun <reified T : Any> T.proxyApi(handler: Handler): T {
    return this.proxyWithLogExceptions().proxyWithHandler(handler)
}