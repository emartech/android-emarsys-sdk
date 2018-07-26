package com.emarsys.mobileengage.notification;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.JsonUtils;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.notification.command.AppEventCommand;
import com.emarsys.mobileengage.notification.command.CompositeCommand;
import com.emarsys.mobileengage.notification.command.CustomEventCommand;
import com.emarsys.mobileengage.notification.command.HideNotificationShadeCommand;
import com.emarsys.mobileengage.notification.command.LaunchApplicationCommand;
import com.emarsys.mobileengage.notification.command.OpenExternalUrlCommand;
import com.emarsys.mobileengage.notification.command.TrackActionClickCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;

public class NotificationCommandFactory {

    private Context context;
    private MobileEngageInternal mobileEngageInternal;

    public NotificationCommandFactory(Context context, MobileEngageInternal mobileEngageInternal) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        this.context = context;
        this.mobileEngageInternal = mobileEngageInternal;
    }

    public Runnable createNotificationCommand(Intent intent) {
        Runnable result = null;
        String actionId = intent.getAction();
        Bundle bundle = intent.getBundleExtra("payload");

        if (bundle != null) {
            String emsPayload = bundle.getString("ems");

            if (actionId != null && emsPayload != null) {
                try {
                    JSONArray actions = new JSONObject(emsPayload).getJSONArray("actions");
                    JSONObject action = findActionWithId(actions, actionId);
                    String type = action.getString("type");
                    String title = action.getString("title");

                    Runnable trackActionClickCommand = new TrackActionClickCommand(mobileEngageInternal, actionId, title);

                    if ("MEAppEvent".equals(type)) {
                        String name = action.getString("name");
                        JSONObject payload = action.optJSONObject("payload");
                        result = new CompositeCommand(Arrays.asList(
                                trackActionClickCommand,
                                new HideNotificationShadeCommand(context),
                                new AppEventCommand(context, name, payload)));
                    }
                    if ("OpenExternalUrl".equals(type)) {
                        Uri link = Uri.parse(action.getString("url"));
                        Intent externalCommandIntent = new Intent(Intent.ACTION_VIEW, link);
                        externalCommandIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (externalCommandIntent.resolveActivity(context.getPackageManager()) != null) {
                            result = new CompositeCommand(Arrays.asList(
                                    trackActionClickCommand,
                                    new HideNotificationShadeCommand(context),
                                    new OpenExternalUrlCommand(externalCommandIntent, context)));
                        }
                    }
                    if ("MECustomEvent".equals(type)) {
                        String name = action.getString("name");
                        JSONObject payload = action.optJSONObject("payload");
                        Map<String, String> eventAttribute = null;
                        if (payload != null) {
                            eventAttribute = JsonUtils.toFlatMap(payload);
                        }
                        result = new CompositeCommand(Arrays.asList(
                                trackActionClickCommand,
                                new CustomEventCommand(name, eventAttribute)));
                    }
                } catch (JSONException ignored) {
                }
            }
        }

        if (result == null) {
            result = new LaunchApplicationCommand(intent, context);
        }

        return result;
    }

    private JSONObject findActionWithId(JSONArray actions, String actionId) throws JSONException {
        for (int i = 0; i < actions.length(); ++i) {
            JSONObject action = actions.optJSONObject(i);
            if (action != null && actionId.equals(action.optString("id"))) {
                return action;
            }
        }
        throw new JSONException("Cannot find action with id: " + actionId);
    }

}
