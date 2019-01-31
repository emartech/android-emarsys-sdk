package com.emarsys.core;

import com.emarsys.core.util.log.Logger;
import com.emarsys.core.util.log.entry.CrashLog;

public class RunnerProxy {

    public void safeRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception exception) {
            Logger.log(new CrashLog(exception));
            throw exception;
        }
    }

}
