package com.emarsys.core.worker;

import android.os.Handler;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.core.request.factory.DefaultRunnableFactory;
import com.emarsys.core.request.factory.RunnableFactory;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelKt;
import com.emarsys.core.request.model.specification.FilterByRequestIds;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.RequestModelUtils;

import java.util.Arrays;

public class CoreCompletionHandlerMiddleware implements CoreCompletionHandler {
    CoreCompletionHandler coreCompletionHandler;
    Repository<RequestModel, SqlSpecification> requestRepository;
    Worker worker;
    CoreSdkHandler coreSDKHandler;
    Handler uiHandler;
    RunnableFactory runnableFactory;

    public CoreCompletionHandlerMiddleware(
            Worker worker,
            Repository<RequestModel, SqlSpecification> requestRepository,
            Handler uiHandler,
            CoreSdkHandler coreSDKHandler,
            CoreCompletionHandler coreCompletionHandler) {
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(worker, "Worker must not be null!");
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        Assert.notNull(uiHandler, "uiHandler must not be null!");
        Assert.notNull(coreSDKHandler, "coreSDKHandler must not be null!");
        this.coreCompletionHandler = coreCompletionHandler;
        this.requestRepository = requestRepository;
        this.worker = worker;
        this.coreSDKHandler = coreSDKHandler;
        this.runnableFactory = new DefaultRunnableFactory();
        this.uiHandler = uiHandler;
    }

    @Override
    public void onSuccess(final String id, final ResponseModel responseModel) {
        coreSDKHandler.post(runnableFactory.runnableFrom(new Runnable() {
            @Override
            public void run() {
                removeRequestModel(responseModel);

                worker.unlock();
                worker.run();

                handleSuccess(responseModel);
            }
        }));
    }

    @Override
    public void onError(final String id, final ResponseModel responseModel) {
        coreSDKHandler.post(runnableFactory.runnableFrom(new Runnable() {
            @Override
            public void run() {
                if (isNonRetriableError(responseModel.getStatusCode())) {
                    removeRequestModel(responseModel);

                    handleError(responseModel);
                    worker.unlock();
                    worker.run();
                } else {
                    worker.unlock();
                }
            }
        }));
    }

    private void removeRequestModel(ResponseModel responseModel) {
        String[] ids = RequestModelKt.collectRequestIds(responseModel.getRequestModel());

        int noOfIterations = ids.length % 500 == 0 ? ids.length / 500 : ids.length / 500 + 1;
        for (int i = 0; i < noOfIterations; i++) {
            int noOfElements = Math.min(ids.length, (i + 1) * 500);
            requestRepository.remove(new FilterByRequestIds(Arrays.copyOfRange(ids, i * 500, noOfElements)));
        }
    }

    @Override
    public void onError(final String id, final Exception cause) {
        coreSDKHandler.post(runnableFactory.runnableFrom(new Runnable() {
            @Override
            public void run() {
                worker.unlock();
                uiHandler.post(runnableFactory.runnableFrom(new Runnable() {
                    @Override
                    public void run() {
                        coreCompletionHandler.onError(id, cause);
                    }
                }));
            }
        }));
    }

    private boolean isNonRetriableError(int statusCode) {
        if (statusCode == 408 || statusCode == 429) {
            return false;
        } else {
            return 400 <= statusCode && statusCode < 500;
        }
    }

    private void handleSuccess(final ResponseModel responseModel) {
        for (final String id : RequestModelUtils.extractIdsFromCompositeRequestModel(responseModel.getRequestModel())) {
            uiHandler.post(runnableFactory.runnableFrom(new Runnable() {
                @Override
                public void run() {
                    coreCompletionHandler.onSuccess(id, responseModel);
                }
            }));
        }
    }

    private void handleError(final ResponseModel responseModel) {
        for (final String id : RequestModelUtils.extractIdsFromCompositeRequestModel(responseModel.getRequestModel())) {
            uiHandler.post(runnableFactory.runnableFrom(new Runnable() {
                @Override
                public void run() {
                    coreCompletionHandler.onError(id, responseModel);
                }
            }));
        }
    }
}
