package com.emarsys.core.util

fun Exception.rootCause(): Throwable? {
    var rootCause = this.cause
    while (rootCause?.cause != null && rootCause.cause != rootCause) {
        rootCause = rootCause.cause
    }
    return rootCause
}