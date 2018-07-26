package com.emarsys.mobileengage.fake;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.worker.Worker;

import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.mock;

public class FakeRequestManager extends RequestManager {

    public static enum ResponseType {
        SUCCESS,
        FAILURE
    }

    private final CoreCompletionHandler coreCompletionHandler;

    private ResponseType responseType;
    public CountDownLatch latch;

    @SuppressWarnings("unchecked")
    public FakeRequestManager(
            ResponseType responseType,
            CountDownLatch latch,
            CoreCompletionHandler coreCompletionHandler) {
        super(new Handler(Looper.getMainLooper()), mock(Repository.class), mock(Worker.class));
        this.coreCompletionHandler = coreCompletionHandler;
        this.responseType = responseType;
        this.latch = latch;
    }

    @Override
    public void submit(final RequestModel model) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (responseType == ResponseType.SUCCESS) {
                    coreCompletionHandler.onSuccess(
                            model.getId(),
                            new ResponseModel.Builder()
                                    .statusCode(200)
                                    .message("OK")
                                    .requestModel(mock(RequestModel.class))
                                    .build());
                } else {
                    coreCompletionHandler.onError(model.getId(), new Exception());
                }
                if (latch != null) {
                    latch.countDown();
                }
            }
        }).start();
    }


}
