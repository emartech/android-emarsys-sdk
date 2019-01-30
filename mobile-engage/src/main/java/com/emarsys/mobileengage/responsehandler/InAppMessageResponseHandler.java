package com.emarsys.mobileengage.responsehandler;

import android.annotation.TargetApi;
import android.os.Build;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.InAppLoadingTime;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener;
import com.emarsys.mobileengage.util.AndroidVersionUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    private Repository<Map<String, Object>, SqlSpecification> logRepository;
    private InAppPresenter inAppPresenter;
    private TimestampProvider timestampProvider;

    public InAppMessageResponseHandler(InAppPresenter inAppPresenter,
                                       Repository<Map<String, Object>, SqlSpecification> logRepository,
                                       TimestampProvider timestampProvider) {
        Assert.notNull(logRepository, "LogRepository must not be null!");
        Assert.notNull(inAppPresenter, "InAppPresenter must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.logRepository = logRepository;
        this.inAppPresenter = inAppPresenter;
        this.timestampProvider = timestampProvider;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        JSONObject responseBody = responseModel.getParsedBody();
        boolean responseBodyNotNull = responseBody != null;
        boolean shouldHandle = false;

        if (AndroidVersionUtils.isKitKatOrAbove() && responseBodyNotNull) {
            try {
                JSONObject message = responseBody.getJSONObject("message");
                shouldHandle = message.has("html");
            } catch (JSONException ignored) {
            }
        }

        return shouldHandle;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void handleResponse(final ResponseModel responseModel) {
        JSONObject responseBody = responseModel.getParsedBody();
        try {
            JSONObject message = responseBody.getJSONObject("message");
            String html = message.getString("html");
            final String id = message.getString("id");
            final String requestId = responseModel.getRequestModel().getId();
            inAppPresenter.present(id, requestId, html, new MessageLoadedListener() {
                @Override
                public void onMessageLoaded() {
                    long loadingTime = timestampProvider.provideTimestamp() - responseModel.getTimestamp();
                    Logger.log(new InAppLoadingTime(loadingTime, id, requestId));
                }
            });
        } catch (JSONException je) {
            EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, responseBody);
        }
    }
}
