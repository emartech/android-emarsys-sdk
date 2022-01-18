package com.emarsys.core.fake;

import static org.mockito.Mockito.mock;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.RequestTask;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.response.ResponseModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FakeRestClient extends RestClient {

    private final List<Object> fakeResults;

    @SuppressWarnings("unchecked")
    public FakeRestClient(Object... fakeResults) {
        super(mock(ConnectionProvider.class), mock(TimestampProvider.class), mock(ResponseHandlersProcessor.class), mock(List.class),
                new ConcurrentHandlerHolderFactory(new Handler(Looper.getMainLooper())).create());
        for (Object o : fakeResults) {
            if (!(o instanceof Integer || o instanceof Exception)) {
                throw new IllegalArgumentException("FakeResults list can only contain Integers and Exceptions!");
            }
        }
        this.fakeResults = new ArrayList<>(Arrays.asList(fakeResults));
    }

    @Override
    public void execute(RequestModel model, CoreCompletionHandler completionHandler) {
        Try<ResponseModel> result = create(model).execute();
        if (result.getErrorCause() != null && result.getResult() != null) {
            completionHandler.onError(model.getId(), result.getResult());
        } else if (result.getErrorCause() != null) {
            completionHandler.onError(model.getId(), (Exception) result.getErrorCause());
        } else {
            completionHandler.onSuccess(model.getId(), result.getResult());
        }
    }

    private RequestTask create(RequestModel model) {
        if (fakeResults.isEmpty()) {
            throw new IllegalStateException("No more predefined fake responses!");
        } else {
            return new FakeRequestTask(model, fakeResults.remove(0));
        }
    }
}
