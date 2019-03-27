package com.emarsys.core.request.factory;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.worker.Worker;

public interface CompletionHandlerProxyProvider {

    CoreCompletionHandler provideProxy(Worker worker);
}
