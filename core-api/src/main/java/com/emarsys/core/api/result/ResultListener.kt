package com.emarsys.core.api.result

fun interface ResultListener<T> {
    fun onResult(result: T)
}