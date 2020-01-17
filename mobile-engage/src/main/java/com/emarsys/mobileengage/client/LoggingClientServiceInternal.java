package com.emarsys.mobileengage.client;

import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;

public class LoggingClientServiceInternal implements ClientServiceInternal {
    private final Class klass;

    public LoggingClientServiceInternal(Class klass) {
        this.klass = klass;
    }

    @Override
    public void trackDeviceInfo() {
        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.debug(new MethodNotAllowed(klass, callerMethodName, null));
    }
}
