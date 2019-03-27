package com.emarsys.core.request.factory;

import android.os.Handler;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.worker.CoreCompletionHandlerMiddleware;
import com.emarsys.core.worker.Worker;

public class CoreCompletionHandlerMiddlewareProvider implements CompletionHandlerProxyProvider {

    private Repository<RequestModel, SqlSpecification> requestRepository;
    private Handler uiHandler;
    private Handler coreSdkHandler;
    private CoreCompletionHandler defaultCoreCompletionHandler;

    public CoreCompletionHandlerMiddlewareProvider(CoreCompletionHandler defaultCoreCompletionHandler, Repository<RequestModel, SqlSpecification> requestRepository, Handler uiHandler, Handler coreSdkHandler) {
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(uiHandler, "UiHandler must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");
        Assert.notNull(defaultCoreCompletionHandler, "DefaultCoreCompletionHandler must not be null!");

        this.requestRepository = requestRepository;
        this.uiHandler = uiHandler;
        this.coreSdkHandler = coreSdkHandler;
        this.defaultCoreCompletionHandler = defaultCoreCompletionHandler;
    }

    @Override
    public CoreCompletionHandler provideProxy(Worker worker) {
        return new CoreCompletionHandlerMiddleware(worker, requestRepository, uiHandler, coreSdkHandler, defaultCoreCompletionHandler);
    }

}
