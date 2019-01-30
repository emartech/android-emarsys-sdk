package com.emarsys.mobileengage.responsehandler;

import android.annotation.TargetApi;
import android.os.Build;

import com.emarsys.core.response.AbstractResponseHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.util.AndroidVersionUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    private InAppPresenter inAppPresenter;

    public InAppMessageResponseHandler(InAppPresenter inAppPresenter) {
        Assert.notNull(inAppPresenter, "InAppPresenter must not be null!");

        this.inAppPresenter = inAppPresenter;
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
            inAppPresenter.present(id, requestId, responseModel.getTimestamp(), html, null);
        } catch (JSONException je) {
            EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, responseBody);
        }
    }
}
