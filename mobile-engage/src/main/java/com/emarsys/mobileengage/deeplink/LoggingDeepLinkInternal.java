package com.emarsys.mobileengage.deeplink;

import android.app.Activity;
import android.content.Intent;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;

import java.util.HashMap;
import java.util.Map;

public class LoggingDeepLinkInternal implements DeepLinkInternal {
    @Override
    public void trackDeepLinkOpen(Activity activity, Intent intent, CompletionListener completionListener) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("activity", activity.toString());
        parameters.put("intent", intent.toString());
        parameters.put("completion_listener", completionListener != null);

        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(LoggingDeepLinkInternal.class, callerMethodName, parameters));
    }
}
