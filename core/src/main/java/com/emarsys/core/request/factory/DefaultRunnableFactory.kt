package com.emarsys.core.request.factory

class DefaultRunnableFactory : RunnableFactory {
    override fun runnableFrom(runnable: Runnable): Runnable = runnable
}
