package com.emarsys.predict.di;

import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.storage.Storage;
import com.emarsys.predict.PredictInternal;

public interface PredictDependencyContainer extends DependencyContainer {

    PredictInternal getPredictInternal();

    PredictInternal getLoggingPredictInternal();

    Runnable getPredictShardTrigger();

    ServiceEndpointProvider getPredictServiceProvider();

    Storage<String> getPredictServiceStorage();
}
