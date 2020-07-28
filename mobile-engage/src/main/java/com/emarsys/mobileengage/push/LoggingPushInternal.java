package com.emarsys.mobileengage.push;

import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;
import com.emarsys.mobileengage.api.event.EventHandler;
import com.emarsys.mobileengage.api.push.NotificationInformationListener;

import java.util.HashMap;
import java.util.Map;

public class LoggingPushInternal implements PushInternal {

    private final Class klass;

    public LoggingPushInternal(Class klass) {
        this.klass = klass;
    }

    @Override
    public void setPushToken(String pushToken, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("push_token", pushToken);
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters));
    }

    @Override
    public void clearPushToken(CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters));
    }

    @Override
    public void trackMessageOpen(Intent intent, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("intent", intent.toString());
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters));
    }

    @Override
    public void setNotificationEventHandler(EventHandler notificationEventHandler) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("notification_event_handler", notificationEventHandler != null);

        String callerMethodName = SystemUtils.getCallerMethodName();
        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters));
    }

    @Override
    public void setSilentMessageEventHandler(EventHandler silentMessageEventHandler) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("silent_message_event_handler", silentMessageEventHandler != null);

        String callerMethodName = SystemUtils.getCallerMethodName();
        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters));

    }

    @Override
    public void setNotificationInformationListener(NotificationInformationListener notificationInformationListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("notification_information_listener", notificationInformationListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();
        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters));
    }

    @Override
    public void setSilentNotificationInformationListener(NotificationInformationListener silentNotificationInformationListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("silent_notification_information_listener", silentNotificationInformationListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();
        Logger.debug(new MethodNotAllowed(klass, callerMethodName, parameters));
    }
}
