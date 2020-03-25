package com.emarsys.mobileengage.request;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider;
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.core.worker.Worker;
import com.emarsys.mobileengage.RefreshTokenInternal;

public class CoreCompletionHandlerRefreshTokenProxyProvider implements CompletionHandlerProxyProvider {
    private final CoreCompletionHandlerMiddlewareProvider coreCompletionHandlerMiddlewareProvider;
    private final RefreshTokenInternal refreshTokenInternal;
    private final RestClient restClient;
    private final Storage<String> contactTokenStorage;
    private final ServiceEndpointProvider clientServiceProvider;
    private final ServiceEndpointProvider eventServiceProvider;
    private final ServiceEndpointProvider messageInboxServiceProvider;

    public CoreCompletionHandlerRefreshTokenProxyProvider(CoreCompletionHandlerMiddlewareProvider coreCompletionHandlerMiddlewareProvider, RefreshTokenInternal refreshTokenInternal, RestClient restClient, Storage<String> contactTokenStorage, ServiceEndpointProvider clientServiceProvider, ServiceEndpointProvider eventServiceProvider, ServiceEndpointProvider messageInboxServiceProvider) {
        Assert.notNull(coreCompletionHandlerMiddlewareProvider, "CoreCompletionHandlerMiddlewareProvider must not be null!");
        Assert.notNull(refreshTokenInternal, "RefreshTokenInternal must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(contactTokenStorage, "ContactTokenStorage must not be null!");
        Assert.notNull(clientServiceProvider, "ClientServiceProvider must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");
        Assert.notNull(messageInboxServiceProvider, "MessageInboxServiceProvider must not be null!");

        this.coreCompletionHandlerMiddlewareProvider = coreCompletionHandlerMiddlewareProvider;
        this.refreshTokenInternal = refreshTokenInternal;
        this.restClient = restClient;
        this.contactTokenStorage = contactTokenStorage;
        this.clientServiceProvider = clientServiceProvider;
        this.eventServiceProvider = eventServiceProvider;
        this.messageInboxServiceProvider = messageInboxServiceProvider;
    }

    @Override
    public CoreCompletionHandlerRefreshTokenProxy provideProxy(Worker worker) {
        Assert.notNull(worker, "Worker must not be null!");

        CoreCompletionHandler coreCompletionHandler = coreCompletionHandlerMiddlewareProvider.provideProxy(worker);

        return new CoreCompletionHandlerRefreshTokenProxy(coreCompletionHandler, refreshTokenInternal, restClient, contactTokenStorage, clientServiceProvider, eventServiceProvider, messageInboxServiceProvider);
    }
}
