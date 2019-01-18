package com.emarsys.core.request;

import android.os.Handler;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.Registry;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.factory.DefaultRunnableFactory;
import com.emarsys.core.request.factory.RunnableFactory;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.core.worker.Worker;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {

    private final RestClient restClient;
    private Map<String, String> defaultHeaders;

    Worker worker;
    Handler coreSDKHandler;
    RunnableFactory runnableFactory;
    private Repository<RequestModel, SqlSpecification> requestRepository;
    private Repository<ShardModel, SqlSpecification> shardRepository;
    private Registry<RequestModel, CompletionListener> callbackRegistry;
    private CoreCompletionHandler defaultCompletionHandler;

    public RequestManager(
            Handler coreSDKHandler,
            Repository<RequestModel, SqlSpecification> requestRepository,
            Repository<ShardModel, SqlSpecification> shardRepository,
            Worker worker,
            RestClient restClient,
            Registry<RequestModel, CompletionListener> callbackRegistry,
            CoreCompletionHandler defaultCompletionHandler) {
        Assert.notNull(coreSDKHandler, "CoreSDKHandler must not be null!");
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(shardRepository, "ShardRepository must not be null!");
        Assert.notNull(worker, "Worker must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(callbackRegistry, "CallbackRegistry must not be null!");
        Assert.notNull(defaultCompletionHandler, "DefaultCompletionHandler must not be null!");
        defaultHeaders = new HashMap<>();
        this.requestRepository = requestRepository;
        this.shardRepository = shardRepository;
        this.coreSDKHandler = coreSDKHandler;
        this.worker = worker;
        this.restClient = restClient;
        this.runnableFactory = new DefaultRunnableFactory();
        this.callbackRegistry = callbackRegistry;
        this.defaultCompletionHandler = defaultCompletionHandler;
    }

    public void setDefaultHeaders(Map<String, String> defaultHeaders) {
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", defaultHeaders);
        this.defaultHeaders = defaultHeaders;
    }

    public void submit(final RequestModel model, final CompletionListener callback) {
        Assert.notNull(model, "RequestModel must not be null!");
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", model);

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
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", model);

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
        submitNow(requestModel, defaultCompletionHandler);
    }

    public void submitNow(RequestModel requestModel, CoreCompletionHandler completionHandler) {
        Assert.notNull(requestModel, "RequestModel must not be null!");
        Assert.notNull(completionHandler, "CompletionHandler must not be null!");

        restClient.execute(requestModel, completionHandler);
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
