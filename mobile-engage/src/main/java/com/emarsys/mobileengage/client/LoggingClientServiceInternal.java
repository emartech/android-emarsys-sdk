package com.emarsys.mobileengage.client;

import com.emarsys.core.util.SystemUtils;
import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.MethodNotAllowed;

public class LoggingClientServiceInternal implements ClientServiceInternal {
    @Override
    public void trackDeviceInfo() {
        String callerMethodName = SystemUtils.getCallerMethodName();

        Logger.log(new MethodNotAllowed(LoggingClientServiceInternal.class, callerMethodName, null));
    }
}
