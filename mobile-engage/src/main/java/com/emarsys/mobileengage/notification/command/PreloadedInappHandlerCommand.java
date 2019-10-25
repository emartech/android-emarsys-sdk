package com.emarsys.mobileengage.notification.command;

import android.content.Intent;
import android.os.Bundle;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.FileUtils;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.iam.PushToInAppAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PreloadedInappHandlerCommand implements Runnable {

    private final Intent intent;
    private final MobileEngageDependencyContainer dependencyContainer;

    public PreloadedInappHandlerCommand(Intent intent, MobileEngageDependencyContainer dependencyContainer) {
        Assert.notNull(intent, "Intent must not be null!");
        Assert.notNull(dependencyContainer, "DependencyContainer must not be null!");

        this.intent = intent;
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public void run() {
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Bundle payload = extras.getBundle("payload");
                if (payload != null) {
                    String ems = payload.getString("ems");
                    if (ems != null) {
                        JSONObject emsJson = new JSONObject(ems);
                        JSONObject inAppDescriptor = new JSONObject(emsJson.getString("inapp"));

                        final String campaignId = inAppDescriptor.getString("campaignId");
                        final String url = inAppDescriptor.optString("url", null);
                        final String fileUrl = inAppDescriptor.optString("fileUrl", null);
                        final String sid = extractSid(payload);

                        dependencyContainer.getCoreSdkHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                String html = null;
                                if (fileUrl != null) {
                                    html = FileUtils.readFileIntoString(fileUrl);
                                    new File(fileUrl).delete();
                                }
                                if (html == null && url != null) {
                                    html = FileUtils.readURLIntoString(url);
                                }
                                if (campaignId != null && html != null) {
                                    scheduleInAppDisplay(campaignId, html, dependencyContainer, sid, url);
                                }
                            }
                        });

                    }
                }
            }
        } catch (JSONException ignored) {
        }
    }

    private void scheduleInAppDisplay(String campaignId, String html, MobileEngageDependencyContainer container, String sid, String url) {
        PushToInAppAction pushToInAppAction = new PushToInAppAction(container.getInAppPresenter(), campaignId, html, sid, url, container.getTimestampProvider());
        container.getActivityLifecycleWatchdog().addTriggerOnActivityAction(pushToInAppAction);
    }

    private String extractSid(Bundle bundle) {
        String sid = null;
        if (bundle != null && bundle.containsKey("u")) {
            try {
                sid = new JSONObject(bundle.getString("u")).getString("sid");
            } catch (JSONException ignore) {

            }
        }

        return sid;
    }
}
