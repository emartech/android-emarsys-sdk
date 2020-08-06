package com.emarsys.core.worker;

import android.os.Handler;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.connection.ConnectionChangeListener;
import com.emarsys.core.connection.ConnectionState;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.specification.Everything;
import com.emarsys.core.request.RequestExpiredException;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.specification.FilterByRequestIds;
import com.emarsys.core.request.model.specification.QueryLatestRequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.OfflineQueueSize;

import java.util.List;


public class DefaultWorker implements ConnectionChangeListener, Worker {

    private final CompletionHandlerProxyProvider proxyProvider;
    Repository<RequestModel, SqlSpecification> requestRepository;
    ConnectionWatchDog connectionWatchDog;
    private boolean locked;
    CoreCompletionHandler coreCompletionHandler;
    RestClient restClient;
    private Handler uiHandler;

    public DefaultWorker(Repository<RequestModel, SqlSpecification> requestRepository, ConnectionWatchDog connectionWatchDog, Handler uiHandler, CoreCompletionHandler coreCompletionHandler, RestClient restClient, CompletionHandlerProxyProvider proxyProvider) {
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(connectionWatchDog, "ConnectionWatchDog must not be null!");
        Assert.notNull(uiHandler, "UiHandler must not be null!");
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(proxyProvider, "ProxyProvider must not be null!");

        this.coreCompletionHandler = coreCompletionHandler;
        this.requestRepository = requestRepository;
        this.connectionWatchDog = connectionWatchDog;
        this.connectionWatchDog.registerReceiver(this);
        this.uiHandler = uiHandler;
        this.restClient = restClient;
        this.proxyProvider = proxyProvider;
    }

    @Override
    public void lock() {
        locked = true;
    }

    @Override
    public void unlock() {
        locked = false;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void run() {
        if (!isLocked() && connectionWatchDog.isConnected() && !requestRepository.isEmpty()) {
            lock();
            RequestModel model = findFirstNonExpiredModel();
            if (model != null) {
                restClient.execute(
                        model,
                        proxyProvider.provideProxy(this, coreCompletionHandler));
            } else {
                unlock();
            }
        }
    }

    @Override
    public void onConnectionChanged(ConnectionState connectionState, boolean isConnected) {
        if (isConnected) {
            Logger.debug(new OfflineQueueSize(requestRepository.query(new Everything()).size()));
            run();
        }
    }

    private RequestModel findFirstNonExpiredModel() {
        while (!requestRepository.isEmpty()) {
            List<RequestModel> result = requestRepository.query(new QueryLatestRequestModel());
            if (!result.isEmpty()) {
                RequestModel model = result.get(0);
                if (isExpired(model)) {
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
        String[] ids = {expiredModel.getId()};
        requestRepository.remove(new FilterByRequestIds(ids));
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                coreCompletionHandler.onError(expiredModel.getId(), new RequestExpiredException("Request expired", expiredModel.getUrl().getPath()));
            }
        });
    }
}