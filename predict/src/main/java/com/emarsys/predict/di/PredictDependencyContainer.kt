package com.emarsys.predict.di

import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.storage.Storage
import com.emarsys.predict.PredictInternal

interface PredictDependencyContainer : DependencyContainer {
    fun getPredictInternal(): PredictInternal

    fun getLoggingPredictInternal(): PredictInternal

    fun getPredictShardTrigger(): Runnable

    fun getPredictServiceProvider(): ServiceEndpointProvider

    fun getPredictServiceStorage(): Storage<String?>
}