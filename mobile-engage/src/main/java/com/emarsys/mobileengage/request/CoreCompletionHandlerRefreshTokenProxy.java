package com.emarsys.mobileengage.request;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.RefreshTokenInternal;
import com.emarsys.mobileengage.util.RequestModelUtils;

import java.util.Map;

import androidx.annotation.Nullable;

public class CoreCompletionHandlerRefreshTokenProxy implements CoreCompletionHandler {
    private final CoreCompletionHandler coreCompletionHandler;
    private final RefreshTokenInternal refreshTokenInternal;
    private final RestClient restClient;
    private final Storage<String> contactTokenStorage;

    public CoreCompletionHandlerRefreshTokenProxy(CoreCompletionHandler coreCompletionHandler, RefreshTokenInternal refreshTokenInternal, RestClient restClient, Storage<String> contactTokenStorage) {
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        Assert.notNull(refreshTokenInternal, "RefreshTokenInternal must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(contactTokenStorage, "ContactTokenStorage must not be null!");

        this.coreCompletionHandler = coreCompletionHandler;
        this.refreshTokenInternal = refreshTokenInternal;
        this.restClient = restClient;
        this.contactTokenStorage = contactTokenStorage;

    }

    @Override
    public void onSuccess(String id, ResponseModel responseModel) {
        coreCompletionHandler.onSuccess(id, responseModel);
    }

    @Override
    public void onError(final String originalId, final ResponseModel originalResponseModel) {
        if (originalResponseModel.getStatusCode() == 401 && RequestModelUtils.isMobileEngageV3Request(originalResponseModel.getRequestModel())) {
            refreshTokenInternal.refreshContactToken(new CompletionListener() {
                @Override
                public void onCompleted(@Nullable Throwable errorCause) {
                    if (errorCause == null) {
                        RequestModel updatedRequestModel = getUpdatedRequestModel(originalResponseModel);

                        restClient.execute(updatedRequestModel, CoreCompletionHandlerRefreshTokenProxy.this);
                    } else {
                        coreCompletionHandler.onError(originalId, new Exception(errorCause));
                    }
                }
            });
        } else {
            coreCompletionHandler.onError(originalId, originalResponseModel);
        }
    }

    private RequestModel getUpdatedRequestModel(ResponseModel originalResponseModel) {
        String updatedRefreshToken = contactTokenStorage.get();
        RequestModel originalRequestModel = originalResponseModel.getRequestModel();
        Map<String, String> headers = originalRequestModel.getHeaders();
        headers.put("X-Contact-Token", updatedRefreshToken);
        if (originalRequestModel instanceof CompositeRequestModel) {
            return new CompositeRequestModel.Builder(originalRequestModel).headers(headers).build();
        } else {
            return new RequestModel.Builder(originalRequestModel).headers(headers).build();
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

        if (coreCompletionHandler != null ? !coreCompletionHandler.equals(that.coreCompletionHandler) : that.coreCompletionHandler != null)
            return false;
        if (refreshTokenInternal != null ? !refreshTokenInternal.equals(that.refreshTokenInternal) : that.refreshTokenInternal != null)
            return false;
        if (restClient != null ? !restClient.equals(that.restClient) : that.restClient != null)
            return false;
        return contactTokenStorage != null ? contactTokenStorage.equals(that.contactTokenStorage) : that.contactTokenStorage == null;
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
