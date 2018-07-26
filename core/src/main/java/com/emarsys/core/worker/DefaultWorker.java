package com.emarsys.core.worker;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.connection.ConnectionChangeListener;
import com.emarsys.core.connection.ConnectionState;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.RequestExpiredException;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.specification.FilterByRequestId;
import com.emarsys.core.request.model.specification.QueryNewestRequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;

import java.util.List;


public class DefaultWorker implements ConnectionChangeListener, Worker {

    Repository<RequestModel, SqlSpecification> requestRepository;
    ConnectionWatchDog connectionWatchDog;
    boolean locked;
    CoreCompletionHandler coreCompletionHandler;
    RestClient restClient;
    Handler coreSdkHandler;
    Handler uiHandler;

    public DefaultWorker(Repository<RequestModel, SqlSpecification> requestRepository, ConnectionWatchDog connectionWatchDog, Handler coreSdkHandler, CoreCompletionHandler coreCompletionHandler, RestClient restClient) {
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(connectionWatchDog, "ConnectionWatchDog must not be null!");
        Assert.notNull(coreSdkHandler, "Handler must not be null!");
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        Assert.notNull(restClient, "Restclient must not be null!");
        this.coreCompletionHandler = coreCompletionHandler;
        this.requestRepository = requestRepository;
        this.connectionWatchDog = connectionWatchDog;
        this.connectionWatchDog.registerReceiver(this);
        this.coreSdkHandler = coreSdkHandler;
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.restClient = restClient;
    }

    @Override
    public void lock() {
        EMSLogger.log(CoreTopic.OFFLINE, "Old value: %s, new value: %s", locked, true);
        locked = true;
    }

    @Override
    public void unlock() {
        EMSLogger.log(CoreTopic.OFFLINE, "Old value: %s, new value: %s", locked, false);
        locked = false;
    }

    @Override
    public boolean isLocked() {
        EMSLogger.log(CoreTopic.OFFLINE, "Current locked status: %s", locked);
        return locked;
    }

    @Override
    public void run() {
        EMSLogger.log(CoreTopic.OFFLINE, "Entered run");

        if (!isLocked() && connectionWatchDog.isConnected() && !requestRepository.isEmpty()) {
            EMSLogger.log(CoreTopic.OFFLINE, "Connection is OK and queue is not empty");
            lock();
            RequestModel model = findFirstNonExpiredModel();
            EMSLogger.log(CoreTopic.OFFLINE, "First non expired model: %s", model);
            if(model != null){
                restClient.execute(
                        model,
                        new CoreCompletionHandlerMiddleware(
                                this,
                                requestRepository,
                                coreSdkHandler,
                                coreCompletionHandler));
            } else {
                unlock();
            }
        }
    }

    @Override
    public void onConnectionChanged(ConnectionState connectionState, boolean isConnected) {
        if (isConnected) {
            run();
        }
    }

    private RequestModel findFirstNonExpiredModel() {
        while (!requestRepository.isEmpty()) {
            List<RequestModel> result = requestRepository.query(new QueryNewestRequestModel());
            if (!result.isEmpty()) {
                RequestModel model = result.get(0);
                if (isExpired(model)) {
                    EMSLogger.log(CoreTopic.OFFLINE, "Model expired: %s", model);
                    handleExpiration(model);
                } else {
                    return model;
                }
            } else {
                break;
            }
        }
        return null;
    }

    private boolean isExpired(RequestModel model) {
        long now = System.currentTimeMillis();
        return now - model.getTimestamp() > model.getTtl();
    }

    private void handleExpiration(final RequestModel expiredModel) {
        requestRepository.remove(new FilterByRequestId(expiredModel));
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                coreCompletionHandler.onError(expiredModel.getId(), new RequestExpiredException("Request expired", expiredModel.getUrl().getPath()));
            }
        });
    }
}