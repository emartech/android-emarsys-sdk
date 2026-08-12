package com.emarsys.core.util

import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Exception.rootCause(): Throwable? {
    val visited = HashSet<Throwable>()
    var rootCause = this.cause
    while (rootCause?.cause != null) {
        if (!visited.add(rootCause)) break
        rootCause = rootCause.cause
    }
    return rootCause
}

fun Throwable.isNetworkException(): Boolean {
    var cause: Throwable? = this
    while (cause != null) {
        if (cause is UnknownHostException ||
            cause is SocketException ||
            cause is SocketTimeoutException ||
            cause is ConnectException
        ) return true
        cause = cause.cause
    }
    return false
}