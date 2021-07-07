package com.emarsys.core.api.result

fun interface CompletionListener {
    fun onCompleted(errorCause: Throwable?)
}