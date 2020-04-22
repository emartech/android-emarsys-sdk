package com.emarsys.core.request;

import android.os.AsyncTask;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.Mapper;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.JsonUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.RequestLog;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import kotlin.text.Charsets;


public class RequestTask extends AsyncTask<Void, Long, Void> {
    private static final int TIMEOUT = 30_000;

    private final RequestModel requestModel;
    private final CoreCompletionHandler handler;
    private final ConnectionProvider connectionProvider;
    private final ResponseHandlersProcessor responseHandlersProcessor;
    private final List<Mapper<RequestModel, RequestModel>> requestModelMappers;
    private TimestampProvider timestampProvider;

    private ResponseModel responseModel;
    private Exception exception;

    public RequestTask(
            RequestModel requestModel,
            CoreCompletionHandler handler,
            ConnectionProvider connectionProvider,
            TimestampProvider timestampProvider,
            ResponseHandlersProcessor responseHandlersProcessor,
            List<Mapper<RequestModel, RequestModel>> requestModelMappers) {
        Assert.notNull(requestModel, "RequestModel must not be null!");
        Assert.notNull(handler, "CoreCompletionHandler must not be null!");
        Assert.notNull(connectionProvider, "ConnectionProvider must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(responseHandlersProcessor, "ResponseHandlersProcessor must not be null!");
        Assert.notNull(requestModelMappers, "RequestModelMappers must not be null!");

        this.requestModel = requestModel;
        this.handler = handler;
        this.connectionProvider = connectionProvider;
        this.timestampProvider = timestampProvider;
        this.responseHandlersProcessor = responseHandlersProcessor;
        this.requestModelMappers = requestModelMappers;
    }

    @Override
    protected Void doInBackground(Void... params) {
        long dbEnd = timestampProvider.provideTimestamp();

        HttpsURLConnection connection = null;
        try {
            RequestModel updatedRequestModel = mapRequestModel(requestModel);

            connection = connectionProvider.provideConnection(updatedRequestModel);

            initializeConnection(connection, updatedRequestModel);
            connection.setConnectTimeout(20_000);
            connection.connect();
            sendBody(connection, updatedRequestModel);
            responseModel = readResponse(connection);
            Logger.info(new RequestLog(responseModel, dbEnd));
        } catch (Exception e) {
            exception = e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (exception != null) {
            handler.onError(requestModel.getId(), exception);
        } else if (responseModel != null) {
            responseHandlersProcessor.process(responseModel);
            if (isStatusCodeOK(responseModel.getStatusCode())) {
                handler.onSuccess(requestModel.getId(), responseModel);
            } else {
                handler.onError(requestModel.getId(), responseModel);
            }
        }
    }

    public RequestModel getRequestModel() {
        return requestModel;
    }

    public CoreCompletionHandler getHandler() {
        return handler;
    }

    private void initializeConnection(HttpsURLConnection connection, RequestModel model) throws IOException {
        connection.setRequestMethod(model.getMethod().name());
        setHeaders(connection, model.getHeaders());
        connection.setConnectTimeout(TIMEOUT);
        if (model.getMethod() != RequestMethod.GET && model.getPayload() != null) {
            connection.setDoOutput(true);
        }
    }

    private void setHeaders(HttpsURLConnection connection, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            connection.setRequestProperty(key, value);
        }
    }

    private void sendBody(HttpsURLConnection connection, RequestModel model) throws IOException {
        if (model.getPayload() != null) {
            byte[] payload = JsonUtils.fromMap(model.getPayload()).toString().getBytes(StandardCharsets.UTF_8);
            BufferedOutputStream writer = new BufferedOutputStream(connection.getOutputStream());
            writer.write(payload);
            writer.close();
        }
    }

    private ResponseModel readResponse(HttpsURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();
        String message = connection.getResponseMessage();
        Map<String, List<String>> headers = connection.getHeaderFields();
        byte[] bodyBytes = readByteArray(connection);
        return new ResponseModel.Builder(timestampProvider)
                .statusCode(statusCode)
                .message(message)
                .headers(headers)
                .body(new String(bodyBytes, Charsets.UTF_8))
                .bytes(bodyBytes)
                .requestModel(requestModel)
                .build();
    }

    private byte[] readByteArray(HttpsURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream;
        if (isStatusCodeOK(responseCode)) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();

    }

    private boolean isStatusCodeOK(int responseCode) {
        return 200 <= responseCode && responseCode < 300;
    }

    private RequestModel mapRequestModel(RequestModel requestModel) {
        RequestModel updatedRequestModel = requestModel;

        for (Mapper<RequestModel, RequestModel> mapper : requestModelMappers) {
            updatedRequestModel = mapper.map(updatedRequestModel);
        }

        return updatedRequestModel;
    }
}
