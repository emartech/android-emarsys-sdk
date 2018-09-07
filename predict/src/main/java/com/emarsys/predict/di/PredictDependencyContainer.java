package com.emarsys.predict.di;

import com.emarsys.core.di.DependencyContainer;
import com.emarsys.predict.PredictInternal;

public interface PredictDependencyContainer extends DependencyContainer {

    PredictInternal getPredictInternal();

}
