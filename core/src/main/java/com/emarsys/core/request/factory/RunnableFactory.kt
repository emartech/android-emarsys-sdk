package com.emarsys.core.request.factory

fun interface RunnableFactory {
    fun runnableFrom(runnable: Runnable): Runnable
}
