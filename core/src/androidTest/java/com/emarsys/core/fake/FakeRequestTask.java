package com.emarsys.core.fake;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.request.RequestTask;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.provider.timestamp.TimestampProvider;

import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;


public class FakeRequestTask extends RequestTask {

    final Object fakeResult;
    final RequestModel requestModel;
    final CoreCompletionHandler handler;

    @SuppressWarnings("unchecked")
    public FakeRequestTask(RequestModel requestModel, CoreCompletionHandler handler, Object fakeResult) {
        super(requestModel, handler, mock(Repository.class), mock(TimestampProvider.class));
        this.fakeResult = fakeResult;
        this.requestModel = requestModel;
        this.handler = handler;
    }

    public FakeRequestTask(RequestModel requestModel, CoreCompletionHandler handler) {
        this(requestModel, handler, Integer.MIN_VALUE);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (fakeResult instanceof Exception) {
            handler.onError(requestModel.getId(), (Exception) fakeResult);

        } else if (fakeResult instanceof Integer) {
            int statusCode = (Integer) fakeResult;
            ResponseModel responseModel = new ResponseModel.Builder()
                    .statusCode(statusCode)
                    .message("Fake message")
                    .headers(new HashMap<String, List<String>>())
                    .requestModel(requestModel)
                    .build();

            if (200 <= statusCode && statusCode < 400) {
                handler.onSuccess(requestModel.getId(), responseModel);

            } else {
                handler.onError(requestModel.getId(), responseModel);
            }
        }
    }
}
