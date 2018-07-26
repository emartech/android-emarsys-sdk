package com.emarsys.mobileengage.service;

import android.content.Intent;
import android.os.Bundle;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.FileUtils;
import com.emarsys.mobileengage.di.DependencyContainer;
import com.emarsys.mobileengage.iam.PushToInAppAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PushToInAppUtils {

    public static void handlePreloadedInAppMessage(Intent intent, final DependencyContainer dependencyContainer) {
        Assert.notNull(intent, "Intent must not be null!");
        Assert.notNull(dependencyContainer, "DependencyContainer must not be null!");

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
                                    scheduleInAppDisplay(campaignId, html, dependencyContainer);
                                }
                            }
                        });

                    }
                }
            }
        } catch (JSONException ignored) {
        }
    }

    private static void scheduleInAppDisplay(String campaignId, String html, DependencyContainer container) {
        PushToInAppAction pushToInAppAction = new PushToInAppAction(container.getInAppPresenter(), campaignId, html);
        container.getActivityLifecycleWatchdog().addTriggerOnActivityAction(pushToInAppAction);
    }

}
