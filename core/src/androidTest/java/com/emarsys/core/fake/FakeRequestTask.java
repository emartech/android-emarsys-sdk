package com.emarsys.core.fake;

import static org.mockito.Mockito.mock;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.RequestTask;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import java.util.HashMap;
import java.util.List;


public class FakeRequestTask extends RequestTask {

    final Object fakeResult;
    final RequestModel requestModel;

    @SuppressWarnings("unchecked")
    public FakeRequestTask(RequestModel requestModel, Object fakeResult) {
        super(requestModel,
                mock(ConnectionProvider.class),
                mock(TimestampProvider.class)
        );
        this.fakeResult = fakeResult;
        this.requestModel = requestModel;
    }

    public Try<ResponseModel> execute() {
        Try<ResponseModel> result = null;

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (fakeResult instanceof Exception) {
            result = Try.failure((Exception) fakeResult);
        } else if (fakeResult instanceof Integer) {
            int statusCode = (Integer) fakeResult;
            ResponseModel responseModel = new ResponseModel.Builder()
                    .statusCode(statusCode)
                    .message("Fake message")
                    .headers(new HashMap<String, List<String>>())
                    .requestModel(requestModel)
                    .build();

            if (200 <= statusCode && statusCode < 400) {
                result = Try.success(responseModel);
            } else {
                result = new Try(responseModel, new Exception("Error"));
            }
        }
        return result;
    }
}
