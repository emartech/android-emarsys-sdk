package com.emarsys.mobileengage.request;

import androidx.annotation.Nullable;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RefreshTokenInternal;
import com.emarsys.mobileengage.util.RequestModelUtils;

import java.util.Objects;


public class CoreCompletionHandlerRefreshTokenProxy implements CoreCompletionHandler {
    private final CoreCompletionHandler coreCompletionHandler;
    private final RefreshTokenInternal refreshTokenInternal;
    private final RestClient restClient;
    private final Storage<String> contactTokenStorage;
    private final ServiceEndpointProvider clientServiceProvider;
    private final ServiceEndpointProvider eventServiceProvider;
    private final ServiceEndpointProvider messageInboxServiceProvider;

    public CoreCompletionHandlerRefreshTokenProxy(CoreCompletionHandler coreCompletionHandler, RefreshTokenInternal refreshTokenInternal, RestClient restClient, Storage<String> contactTokenStorage, ServiceEndpointProvider clientServiceProvider, ServiceEndpointProvider eventServiceProvider, ServiceEndpointProvider messageInboxServiceProvider) {
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        Assert.notNull(refreshTokenInternal, "RefreshTokenInternal must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(contactTokenStorage, "ContactTokenStorage must not be null!");
        Assert.notNull(clientServiceProvider, "ClientServiceProvider must not be null!");
        Assert.notNull(eventServiceProvider, "EventServiceProvider must not be null!");
        Assert.notNull(messageInboxServiceProvider, "MessageInboxServiceProvider must not be null!");

        this.coreCompletionHandler = coreCompletionHandler;
        this.refreshTokenInternal = refreshTokenInternal;
        this.restClient = restClient;
        this.contactTokenStorage = contactTokenStorage;
        this.clientServiceProvider = clientServiceProvider;
        this.eventServiceProvider = eventServiceProvider;
        this.messageInboxServiceProvider = messageInboxServiceProvider;
    }

    @Override
    public void onSuccess(String id, ResponseModel responseModel) {
        coreCompletionHandler.onSuccess(id, responseModel);
    }

    @Override
    public void onError(final String originalId, final ResponseModel originalResponseModel) {
        if (originalResponseModel.getStatusCode() == 401 && RequestModelUtils.isMobileEngageV3Request(originalResponseModel.getRequestModel(), eventServiceProvider, clientServiceProvider, messageInboxServiceProvider)) {
            refreshTokenInternal.refreshContactToken(new CompletionListener() {
                @Override
                public void onCompleted(@Nullable Throwable errorCause) {
                    if (errorCause == null) {
                        restClient.execute(originalResponseModel.getRequestModel(), CoreCompletionHandlerRefreshTokenProxy.this);
                    } else {
                        for (String id : com.emarsys.core.util.RequestModelUtils.extractIdsFromCompositeRequestModel(originalResponseModel.getRequestModel())) {
                            coreCompletionHandler.onError(id, new Exception(errorCause));
                        }
                    }
                }
            });
        } else {
            coreCompletionHandler.onError(originalId, originalResponseModel);
        }
    }

    @Override
    public void onError(String id, Exception cause) {
        coreCompletionHandler.onError(id, cause);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoreCompletionHandlerRefreshTokenProxy that = (CoreCompletionHandlerRefreshTokenProxy) o;

        if (!Objects.equals(coreCompletionHandler, that.coreCompletionHandler))
            return false;
        if (!Objects.equals(refreshTokenInternal, that.refreshTokenInternal))
            return false;
        if (!Objects.equals(restClient, that.restClient))
            return false;
        return Objects.equals(contactTokenStorage, that.contactTokenStorage);
    }

    @Override
    public int hashCode() {
        int result = coreCompletionHandler != null ? coreCompletionHandler.hashCode() : 0;
        result = 31 * result + (refreshTokenInternal != null ? refreshTokenInternal.hashCode() : 0);
        result = 31 * result + (restClient != null ? restClient.hashCode() : 0);
        result = 31 * result + (contactTokenStorage != null ? contactTokenStorage.hashCode() : 0);
        return result;
    }
}
