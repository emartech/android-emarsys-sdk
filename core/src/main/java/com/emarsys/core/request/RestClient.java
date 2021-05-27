package com.emarsys.core.request;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.Mapper;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.util.Assert;

import java.util.List;

public class RestClient {

    private ConnectionProvider connectionProvider;
    private TimestampProvider timestampProvider;
    private ResponseHandlersProcessor responseHandlersProcessor;
    private List<Mapper<RequestModel, RequestModel>> requestModelMappers;
    private Handler uiHandler;
    private CoreSdkHandler coreSdkHandler;

    public RestClient(
            ConnectionProvider connectionProvider,
            TimestampProvider timestampProvider,
            ResponseHandlersProcessor responseHandlersProcessor,
            List<Mapper<RequestModel, RequestModel>> requestModelMappers,
            Handler uiHandler,
            CoreSdkHandler coreSdkHandler) {
        Assert.notNull(connectionProvider, "ConnectionProvider must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(responseHandlersProcessor, "ResponseHandlersProcessor must not be null!");
        Assert.notNull(requestModelMappers, "RequestModelMappers must not be null!");
        Assert.notNull(uiHandler, "UiHandler must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");

        this.connectionProvider = connectionProvider;
        this.timestampProvider = timestampProvider;
        this.responseHandlersProcessor = responseHandlersProcessor;
        this.requestModelMappers = requestModelMappers;
        this.uiHandler = uiHandler;
        this.coreSdkHandler = coreSdkHandler;
    }

    public void execute(RequestModel model, CoreCompletionHandler completionHandler) {
        Assert.notNull(model, "Model must not be null!");
        Assert.notNull(completionHandler, "CoreCompletionHandler must not be null!");

        final RequestTask task = new RequestTask(
                model,
                completionHandler,
                connectionProvider,
                timestampProvider,
                responseHandlersProcessor,
                requestModelMappers,
                coreSdkHandler);

        if (Looper.myLooper() == uiHandler.getLooper()) {
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                }
            });
        }
    }
}
