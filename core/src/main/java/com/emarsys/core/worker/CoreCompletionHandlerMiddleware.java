package com.emarsys.core.worker;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.factory.DefaultRunnableFactory;
import com.emarsys.core.request.factory.RunnableFactory;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.specification.FilterByRequestId;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoreCompletionHandlerMiddleware implements CoreCompletionHandler {
    CoreCompletionHandler coreCompletionHandler;
    Repository<RequestModel, SqlSpecification> requestRepository;
    Worker worker;
    Handler coreSDKHandler;
    Handler uiHandler;
    RunnableFactory runnableFactory;

    public CoreCompletionHandlerMiddleware(final Worker worker, Repository<RequestModel, SqlSpecification> requestRepository, Handler coreSDKHandler, CoreCompletionHandler coreCompletionHandler) {
        Assert.notNull(requestRepository, "RequestRepository must not be null!");
        Assert.notNull(worker, "Worker must not be null!");
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        Assert.notNull(coreSDKHandler, "coreSDKHandler must not be null!");
        this.coreCompletionHandler = coreCompletionHandler;
        this.requestRepository = requestRepository;
        this.worker = worker;
        this.coreSDKHandler = coreSDKHandler;
        this.runnableFactory = new DefaultRunnableFactory();
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onSuccess(final String id, final ResponseModel responseModel) {
        EMSLogger.log(CoreTopic.OFFLINE, "Id: %s, response model: %s", id, responseModel);

        coreSDKHandler.post(runnableFactory.runnableFrom(new Runnable() {
            @Override
            public void run() {
                requestRepository.remove(new FilterByRequestId(responseModel.getRequestModel()));
                worker.unlock();
                worker.run();

                handleSuccess(responseModel);
            }
        }));
    }

    @Override
    public void onError(final String id, final ResponseModel responseModel) {
        EMSLogger.log(CoreTopic.OFFLINE, "Id: %s, response model: %s", id, responseModel);

        coreSDKHandler.post(runnableFactory.runnableFrom(new Runnable() {
            @Override
            public void run() {
                if (isNonRetriableError(responseModel.getStatusCode())) {
                    requestRepository.remove(new FilterByRequestId(responseModel.getRequestModel()));
                    worker.unlock();
                    worker.run();
                    handleError(responseModel);
                } else {
                    worker.unlock();
                }
            }
        }));
    }

    @Override
    public void onError(final String id, final Exception cause) {
        EMSLogger.log(CoreTopic.OFFLINE, "Id: %s, exception: %s", id, cause);

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
        if (statusCode == 408) {
            return false;
        } else {
            return 400 <= statusCode && statusCode < 500;
        }
    }

    private void handleSuccess(final ResponseModel responseModel) {
        for (final String id : extractIds(responseModel.getRequestModel())) {
            uiHandler.post(runnableFactory.runnableFrom(new Runnable() {
                @Override
                public void run() {
                    coreCompletionHandler.onSuccess(id, responseModel);
                }
            }));
        }
    }

    private void handleError(final ResponseModel responseModel) {
        for (final String id : extractIds(responseModel.getRequestModel())) {
            uiHandler.post(runnableFactory.runnableFrom(new Runnable() {
                @Override
                public void run() {
                    coreCompletionHandler.onError(id, responseModel);
                }
            }));
        }
    }

    private List<String> extractIds(RequestModel requestModel) {
        List<String> ids = new ArrayList<>();
        if (requestModel instanceof CompositeRequestModel) {
            ids.addAll(Arrays.asList(((CompositeRequestModel) requestModel).getOriginalRequestIds()));
        } else {
            ids.add(requestModel.getId());
        }
        return ids;
    }
}
