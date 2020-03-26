package com.emarsys.mobileengage.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.di.MobileEngageDependencyContainer;
import com.emarsys.mobileengage.event.EventServiceInternal;
import com.emarsys.mobileengage.notification.command.CompositeCommand;
import com.emarsys.mobileengage.notification.command.DismissNotificationCommand;
import com.emarsys.mobileengage.notification.command.HideNotificationShadeCommand;
import com.emarsys.mobileengage.notification.command.LaunchApplicationCommand;
import com.emarsys.mobileengage.notification.command.PreloadedInappHandlerCommand;
import com.emarsys.mobileengage.notification.command.TrackActionClickCommand;
import com.emarsys.mobileengage.notification.command.TrackMessageOpenCommand;
import com.emarsys.mobileengage.push.PushInternal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationCommandFactory {

    private final Context context;
    private final MobileEngageDependencyContainer dependencyContainer;
    private final EventServiceInternal eventServiceInternal;
    private final PushInternal pushInternal;
    private final ActionCommandFactory actionCommandFactory;

    public NotificationCommandFactory(
            Context context,
            MobileEngageDependencyContainer dependencyContainer) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(dependencyContainer, "DependencyContainer must not be null!");

        this.context = context;
        this.dependencyContainer = dependencyContainer;
        this.eventServiceInternal = dependencyContainer.getEventServiceInternal();
        this.pushInternal = dependencyContainer.getPushInternal();
        this.actionCommandFactory = dependencyContainer.getNotificationActionCommandFactory();

        Assert.notNull(eventServiceInternal, "EventServiceInternal from dependency container must not be null!");
        Assert.notNull(pushInternal, "PushInternal from dependency container must not be null!");
    }

    public Runnable createNotificationCommand(Intent intent) {
        String actionId = intent.getAction();
        Bundle bundle = intent.getBundleExtra("payload");
        JSONObject action = getAction(bundle, actionId);
        List<Runnable> commands = createMandatoryCommands(intent, bundle);

        if (action == null || !action.optString("type").equals("Dismiss")) {
            if (dependencyContainer.getCurrentActivityProvider().get() == null) {
                commands.add(new LaunchApplicationCommand(intent, context));
            }
        }

        Runnable inappCommand = handleInapp(intent, bundle);
        if (inappCommand != null) {
            commands.add(inappCommand);
        }

        Runnable trackingCommand = handleTracking(intent, actionId, bundle, action);
        if (trackingCommand != null) {
            commands.add(trackingCommand);
        }

        Runnable actionCommand = handleAction(action);
        if (actionCommand != null) {
            commands.add(actionCommand);
        }

        return new CompositeCommand(commands);
    }

    private List<Runnable> createMandatoryCommands(Intent intent, Bundle bundle) {
        List<Runnable> commands = new ArrayList<>();
        commands.add(new HideNotificationShadeCommand(context));
        commands.add(new DismissNotificationCommand(context, intent));
        commands.add(actionCommandFactory.createActionCommand(createActionCommandPayload(bundle)));

        return commands;
    }

    private Runnable handleAction(JSONObject action) {
        Runnable result = null;
        if (action != null) {
            Runnable actionCommand = actionCommandFactory.createActionCommand(action);
            if (actionCommand != null) {
                result = actionCommand;
            }
        }
        return result;
    }

    private Runnable handleTracking(Intent intent, String actionId, Bundle bundle, JSONObject action) {
        Runnable result;
        if (action != null && actionId != null) {
            result = new TrackActionClickCommand(eventServiceInternal, actionId, extractSid(bundle));
        } else {
            result = new TrackMessageOpenCommand(pushInternal, intent);
        }
        return result;
    }

    private Runnable handleInapp(Intent intent, Bundle bundle) {
        Runnable result = null;
        if (hasInapp(bundle)) {
            result = new PreloadedInappHandlerCommand(intent, dependencyContainer);
        }
        return result;
    }

    private boolean hasInapp(Bundle payload) {
        boolean result = false;
        try {
            if (payload != null) {
                String ems = payload.getString("ems");
                if (ems != null) {
                    JSONObject emsJson = new JSONObject(ems);
                    new JSONObject(emsJson.getString("inapp"));
                    result = true;
                }
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    private JSONObject getAction(Bundle bundle, String actionId) {
        JSONObject result = null;
        if (bundle != null) {
            String emsPayload = bundle.getString("ems");
            if (emsPayload != null) {
                try {
                    if (actionId != null) {
                        JSONArray actions = new JSONObject(emsPayload).getJSONArray("actions");
                        result = actionCommandFactory.findActionWithId(actions, actionId);
                    } else {
                        result = new JSONObject(emsPayload).getJSONObject("default_action");
                    }
                } catch (JSONException ignored) {
                }
            }
        }
        return result;
    }

    private String extractSid(Bundle bundle) {
        String sid = null;
        if (bundle != null && bundle.containsKey("u")) {
            try {
                sid = new JSONObject(bundle.getString("u")).getString("sid");
            } catch (JSONException ignore) {

            }
        }

        if (sid == null) {
            sid = "Missing sid";
        }
        return sid;
    }

    private JSONObject createActionCommandPayload(Bundle bundle) {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("type", "MEAppEvent");
        payloadMap.put("name", "push:payload");
        payloadMap.put("payload", extractMandatoryActionPayload(bundle));

        return new JSONObject(payloadMap);
    }

    private JSONObject extractMandatoryActionPayload(Bundle bundle) {
        JSONObject json = new JSONObject();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            for (String key : keys) {
                try {
                    json.put(key, JSONObject.wrap(bundle.get(key)));
                } catch (JSONException ignored) {
                }
            }
        }
        return json;
    }

}
