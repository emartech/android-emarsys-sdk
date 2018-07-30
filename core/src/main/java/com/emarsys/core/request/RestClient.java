package com.emarsys.core.request;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.CoreTopic;
import com.emarsys.core.util.log.EMSLogger;

import java.util.Map;

public class RestClient {

    private Repository<Map<String, Object>, SqlSpecification> logRepository;
    private TimestampProvider timestampProvider;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public RestClient(
            Repository<Map<String, Object>, SqlSpecification> logRepository,
            TimestampProvider timestampProvider) {
        Assert.notNull(logRepository, "LogRepository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.logRepository = logRepository;
        this.timestampProvider = timestampProvider;
    }

    public void execute(RequestModel model, CoreCompletionHandler completionHandler) {
        Assert.notNull(model, "Model must not be null!");
        Assert.notNull(completionHandler, "CoreCompletionHandler must not be null!");
        EMSLogger.log(CoreTopic.NETWORKING, "Argument: %s", model);

        final RequestTask task = new RequestTask(model, completionHandler, logRepository, timestampProvider);

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
