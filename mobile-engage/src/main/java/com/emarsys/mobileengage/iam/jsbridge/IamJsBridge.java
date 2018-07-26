package com.emarsys.mobileengage.iam.jsbridge;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.JsonUtils;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.EventHandler;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IamJsBridge {
    private InAppMessageHandlerProvider messageHandlerProvider;
    private WebView webView;
    private Handler uiHandler;
    private Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private String campaignId;
    private Handler coreSdkHandler;
    private MobileEngageInternal mobileEngageInternal;

    public IamJsBridge(
            InAppMessageHandlerProvider messageHandlerProvider,
            Repository<ButtonClicked, SqlSpecification> buttonClickedRepository,
            String campaignId,
            Handler coreSdkHandler,
            MobileEngageInternal mobileEngageInternal) {
        Assert.notNull(messageHandlerProvider, "MessageHandlerProvider must not be null!");
        Assert.notNull(buttonClickedRepository, "ButtonClickedRepository must not be null!");
        Assert.notNull(campaignId, "CampaignId must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        this.messageHandlerProvider = messageHandlerProvider;
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.buttonClickedRepository = buttonClickedRepository;
        this.campaignId = campaignId;
        this.coreSdkHandler = coreSdkHandler;
        this.mobileEngageInternal = mobileEngageInternal;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    @JavascriptInterface
    public void close(String jsonString) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Activity currentActivity = CurrentActivityWatchdog.getCurrentActivity();
                if (currentActivity != null) {
                    Fragment fragment = currentActivity.getFragmentManager().findFragmentByTag(IamDialog.TAG);
                    if (fragment instanceof DialogFragment) {
                        ((DialogFragment) fragment).dismiss();
                    }
                }
            }
        });
    }

    @JavascriptInterface
    public void triggerAppEvent(final String jsonString) {
        final EventHandler inAppEventHandler = messageHandlerProvider.provideHandler();
        if (inAppEventHandler != null) {
            handleJsBridgeEvent(jsonString, "name", uiHandler, new JsBridgeEventAction() {
                @Override
                public JSONObject execute(String property, JSONObject json) throws Exception {
                    final JSONObject payload = json.optJSONObject("payload");
                    inAppEventHandler.handleEvent(property, payload);
                    return null;
                }
            });
        }
    }

    @JavascriptInterface
    public void triggerMEEvent(String jsonString) {
        handleJsBridgeEvent(jsonString, "name", coreSdkHandler, new JsBridgeEventAction() {
            @Override
            public JSONObject execute(String property, JSONObject json) throws Exception {
                Map<String, String> attributes = extractAttributes(json);
                String eventId = mobileEngageInternal.trackCustomEvent(property, attributes);
                return new JSONObject().put("meEventId", eventId);
            }
        });
    }

    @JavascriptInterface
    public void buttonClicked(String jsonString) {
        handleJsBridgeEvent(jsonString, "buttonId", coreSdkHandler, new JsBridgeEventAction() {
            @Override
            public JSONObject execute(String property, JSONObject json) {
                buttonClickedRepository.add(new ButtonClicked(campaignId, property, System.currentTimeMillis()));
                String eventName = "inapp:click";
                Map<String, String> attributes = new HashMap<>();
                attributes.put("message_id", campaignId);
                attributes.put("button_id", property);
                mobileEngageInternal.trackInternalCustomEvent(eventName, attributes);
                return null;
            }
        });
    }

    @JavascriptInterface
    public void openExternalLink(String jsonString) {
        handleJsBridgeEvent(jsonString, "url", uiHandler, new JsBridgeEventAction() {
            @Override
            public JSONObject execute(String property, JSONObject json) throws Exception {
                Activity activity = CurrentActivityWatchdog.getCurrentActivity();
                if (activity != null) {
                    Uri link = Uri.parse(property);
                    Intent intent = new Intent(Intent.ACTION_VIEW, link);
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        activity.startActivity(intent);
                    } else {
                        throw new Exception("Url cannot be handled by any application!");
                    }
                } else {
                    throw new Exception("UI unavailable!");
                }
                return null;
            }
        });
    }

    private interface JsBridgeEventAction {

        JSONObject execute(String property, JSONObject json) throws Exception;
    }

    private void handleJsBridgeEvent(String jsonString, final String property, Handler handler, final JsBridgeEventAction jsBridgeEventAction) {
        try {
            final JSONObject json = new JSONObject(jsonString);
            final String id = json.getString("id");
            if (json.has(property)) {
                final String propertyValue = json.getString(property);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject resultPayload = jsBridgeEventAction.execute(propertyValue, json);
                            sendSuccess(id, resultPayload);
                        } catch (Exception e) {
                            sendError(id, e.getMessage());
                        }
                    }
                });
            } else {
                sendError(id, String.format("Missing %s!", property));
            }
        } catch (JSONException je) {
            EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, jsonString);
        }
    }

    void sendSuccess(String id, JSONObject resultPayload) {
        try {
            JSONObject message = new JSONObject()
                    .put("id", id)
                    .put("success", true);
            JSONObject result = JsonUtils.merge(message, resultPayload);
            sendResult(result);
        } catch (JSONException ignore) {
        }
    }

    void sendError(String id, String error) {
        try {
            sendResult(new JSONObject()
                    .put("id", id)
                    .put("success", false)
                    .put("error", error));
        } catch (JSONException ignore) {
        }
    }

    void sendResult(final JSONObject payload) {
        Assert.notNull(payload, "Payload must not be null!");
        if (!payload.has("id")) {
            throw new IllegalArgumentException("Payload must have an id!");
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            webView.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", payload), null);
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    webView.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", payload), null);
                }
            });
        }
    }

    private Map<String, String> extractAttributes(JSONObject json) throws JSONException {
        Map<String, String> result = null;
        JSONObject payload = json.optJSONObject("payload");
        if (payload != null) {
            result = new HashMap<>();
            Iterator<String> keys = payload.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                result.put(key, payload.getString(key));
            }
        }
        return result;
    }

}