package com.emarsys.mobileengage.fake;

import static org.mockito.Mockito.mock;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.handler.SdkHandler;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.response.ResponseModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FakeRestClient extends RestClient {

    private final Mode mode;
    private List<ResponseModel> responses;
    private List<Exception> exceptions;
    private SdkHandler handler;

    public enum Mode {SUCCESS, ERROR_RESPONSE_MODEL, ERROR_EXCEPTION}

    public FakeRestClient(ResponseModel returnValue, Mode mode) {
        this(Collections.singletonList(returnValue), mode);
    }

    @SuppressWarnings("unchecked")
    public FakeRestClient(List<ResponseModel> responses, Mode mode) {
        super(mock(ConnectionProvider.class), mock(TimestampProvider.class), mock(ResponseHandlersProcessor.class), mock(List.class),
                ConcurrentHandlerHolderFactory.INSTANCE.create());
        this.responses = new ArrayList<>(responses);
        this.mode = mode;
    }

    public FakeRestClient(Exception exception) {
        this(Collections.singletonList(exception));
    }

    public FakeRestClient(Exception exception, SdkHandler handler) {
        this(Collections.singletonList(exception));
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public FakeRestClient(List<Exception> exceptions) {
        super(mock(ConnectionProvider.class), mock(TimestampProvider.class), mock(ResponseHandlersProcessor.class), mock(List.class),
                ConcurrentHandlerHolderFactory.INSTANCE.create());
        this.exceptions = new ArrayList<>(exceptions);
        this.mode = Mode.ERROR_EXCEPTION;
    }

    public FakeRestClient(ResponseModel returnValue, Mode mode, SdkHandler handler) {
        this(returnValue, mode);
        this.handler = handler;
    }

    @Override
    public void execute(final RequestModel model, final CoreCompletionHandler completionHandler) {
        handler = handler == null ? new SdkHandler(new Handler(Looper.getMainLooper())) : handler;
        handler.postDelayed(() -> {
            if (mode == Mode.SUCCESS) {
                completionHandler.onSuccess(model.getId(), getCurrentItem(responses));
            } else if (mode == Mode.ERROR_RESPONSE_MODEL) {
                completionHandler.onError(model.getId(), getCurrentItem(responses));
            } else if (mode == Mode.ERROR_EXCEPTION) {
                completionHandler.onError(model.getId(), getCurrentItem(exceptions));
            }
        }, 100);
    }

    private <T> T getCurrentItem(List<T> list) {
        T result = list.get(0);
        if (list.size() > 1) {
            list.remove(0);
        }
        return result;
    }
}
