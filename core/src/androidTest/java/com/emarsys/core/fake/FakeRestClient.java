package com.emarsys.core.fake;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.RequestTask;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseHandlersProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

public class FakeRestClient extends RestClient {

    private final List<Object> fakeResults;

    @SuppressWarnings("unchecked")
    public FakeRestClient(Object... fakeResults) {
        super(mock(ConnectionProvider.class), mock(TimestampProvider.class), mock(ResponseHandlersProcessor.class), mock(List.class));
        for (Object o : fakeResults) {
            if (!(o instanceof Integer || o instanceof Exception)) {
                throw new IllegalArgumentException("FakeResults list can only contain Integers and Exceptions!");
            }
        }
        this.fakeResults = new ArrayList<>(Arrays.asList(fakeResults));
    }

    @Override
    public void execute(RequestModel model, CoreCompletionHandler completionHandler) {
        create(model, completionHandler).execute();
    }

    private RequestTask create(RequestModel model, CoreCompletionHandler completionHandler) {
        if (fakeResults.isEmpty()) {
            throw new IllegalStateException("No more predefined fake responses!");
        } else {
            return new FakeRequestTask(model, completionHandler, fakeResults.remove(0));
        }
    }
}
