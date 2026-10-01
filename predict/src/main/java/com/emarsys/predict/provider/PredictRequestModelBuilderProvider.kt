package com.emarsys.predict.provider

import com.emarsys.core.Mockable
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.predict.request.PredictHeaderFactory
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.predict.request.PredictRequestModelBuilder

@Mockable
class PredictRequestModelBuilderProvider(
    private val requestContext: PredictRequestContext,
    private val headerFactory: PredictHeaderFactory,
    private val predictServiceProvider: ServiceEndpointProvider
) {
    fun providePredictRequestModelBuilder(): PredictRequestModelBuilder =
        PredictRequestModelBuilder(requestContext, headerFactory, predictServiceProvider)
}
