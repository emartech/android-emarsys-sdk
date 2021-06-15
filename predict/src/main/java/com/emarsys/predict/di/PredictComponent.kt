package com.emarsys.predict.di

import com.emarsys.core.di.CoreComponent
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.storage.Storage
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.provider.PredictRequestModelBuilderProvider
import com.emarsys.predict.request.PredictRequestContext

fun predict() = PredictComponent.instance
        ?: throw IllegalStateException("DependencyContainer has to be setup first!")

fun setupPredictComponent(predictComponent: PredictComponent) {
    PredictComponent.instance = predictComponent
    CoreComponent.instance = predictComponent
}

fun tearDownPredictComponent() {
    PredictComponent.instance = null
    CoreComponent.instance = null
}

fun isPredictComponentSetup() =
        PredictComponent.instance != null &&
                CoreComponent.instance != null


interface PredictComponent : CoreComponent {
    companion object {
        var instance: PredictComponent? = null
    }

    val predictInternal: PredictInternal

    val loggingPredictInternal: PredictInternal

    val predictShardTrigger: Runnable

    val predictServiceProvider: ServiceEndpointProvider

    val predictServiceStorage: Storage<String?>

    val predictRequestContext: PredictRequestContext

    val predictRequestModelBuilderProvider: PredictRequestModelBuilderProvider
}