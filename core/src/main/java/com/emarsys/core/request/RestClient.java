package com.emarsys.core.request;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.Mapper;
import com.emarsys.core.connection.ConnectionProvider;
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
    private final Handler handler = new Handler(Looper.getMainLooper());

    public RestClient(
            ConnectionProvider connectionProvider,
            TimestampProvider timestampProvider,
            ResponseHandlersProcessor responseHandlersProcessor,
            List<Mapper<RequestModel, RequestModel>> requestModelMappers) {
        Assert.notNull(connectionProvider, "ConnectionProvider must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(responseHandlersProcessor, "ResponseHandlersProcessor must not be null!");
        Assert.notNull(requestModelMappers, "RequestModelMappers must not be null!");

        this.connectionProvider = connectionProvider;
        this.timestampProvider = timestampProvider;
        this.responseHandlersProcessor = responseHandlersProcessor;
        this.requestModelMappers = requestModelMappers;
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
                requestModelMappers);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                }
            });
        }
    }
}
