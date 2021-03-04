package com.emarsys.core.request;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.Registry;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider;
import com.emarsys.core.request.factory.DefaultRunnableFactory;
import com.emarsys.core.request.factory.RunnableFactory;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.worker.Worker;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {

    Worker worker;
    RunnableFactory runnableFactory;

    private Map<String, String> defaultHeaders;
    private final RestClient restClient;
    private final CoreSdkHandler coreSDKHandler;
    private final Repository<RequestModel, SqlSpecification> requestRepository;
    private final Repository<ShardModel, SqlSpecification> shardRepository;
    private final Registry<RequestModel, CompletionListener> callbackRegistry;
    private final CoreCompletionHandler defaultCoreCompletionHandler;
    private final CompletionHandlerProxyProvider completionHandlerProxyProvider;

    public RequestManager(
            CoreSdkHandler coreSDKHandler,
            Repository<RequestModel, SqlSpecification> requestRepository,
            Repository<ShardModel, SqlSpecification> shardRepository,
            Worker worker,
            RestClient restClient,
            Registry<RequestModel, CompletionListener> callbackRegistry,
            CoreCompletionHandler defaultCoreCompletionHandler,
            CompletionHandlerProxyProvider completionHandlerProxyProvider) {
        Assert.notNull(coreSDKHandler, "CoreSDKHandler must not be null!");
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(shardRepository, "ShardRepository must not be null!");
        Assert.notNull(worker, "Worker must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(callbackRegistry, "CallbackRegistry must not be null!");
        Assert.notNull(defaultCoreCompletionHandler, "DefaultCoreCompletionHandler must not be null!");
        Assert.notNull(completionHandlerProxyProvider, "CompletionHandlerProxyProvider must not be null!");

        defaultHeaders = new HashMap<>();
        this.requestRepository = requestRepository;
        this.shardRepository = shardRepository;
        this.coreSDKHandler = coreSDKHandler;
        this.worker = worker;
        this.restClient = restClient;
        this.runnableFactory = new DefaultRunnableFactory();
        this.callbackRegistry = callbackRegistry;
        this.defaultCoreCompletionHandler = defaultCoreCompletionHandler;
        this.completionHandlerProxyProvider = completionHandlerProxyProvider;
    }

    public void setDefaultHeaders(Map<String, String> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public void submit(final RequestModel model, final CompletionListener callback) {
        Assert.notNull(model, "RequestModel must not be null!");

        coreSDKHandler.post(runnableFactory.runnableFrom(new Runnable() {
            @Override
            public void run() {
                injectDefaultHeaders(model);
                requestRepository.add(model);
                callbackRegistry.register(model, callback);
                worker.run();
            }
        }));

    }

    public void submit(final ShardModel model) {
        Assert.notNull(model, "ShardModel must not be null!");

        coreSDKHandler.post(runnableFactory.runnableFrom(
                new Runnable() {
                    @Override
                    public void run() {
                        shardRepository.add(model);
                    }
                }
        ));

    }

    public void submitNow(RequestModel requestModel) {
        CoreCompletionHandler handler = completionHandlerProxyProvider.provideProxy(null, defaultCoreCompletionHandler);

        submitNow(requestModel, handler);
    }

    public void submitNow(RequestModel requestModel, CoreCompletionHandler completionHandler) {
        Assert.notNull(requestModel, "RequestModel must not be null!");
        Assert.notNull(completionHandler, "CompletionHandler must not be null!");

        CoreCompletionHandler handler = completionHandlerProxyProvider.provideProxy(null, completionHandler);

        restClient.execute(requestModel, handler);
    }

    void injectDefaultHeaders(RequestModel model) {
        Map<String, String> modelHeaders = model.getHeaders();
        for (Map.Entry<String, String> defaultHeader : defaultHeaders.entrySet()) {
            String key = defaultHeader.getKey();
            String value = defaultHeader.getValue();

            if (!modelHeaders.containsKey(key)) {
                modelHeaders.put(key, value);
            }
        }
    }
}
