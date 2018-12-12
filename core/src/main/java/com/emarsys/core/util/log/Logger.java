package com.emarsys.core.util.log;

import android.os.Handler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.di.DependencyInjection;

public class Logger {

    public static void log(final LogShard logShard) {
        coreSdkHandler().post(new Runnable() {
            @Override
            public void run() {
                logRepository().add(logShard);
            }
        });
    }

    private static Handler coreSdkHandler() {
        return DependencyInjection.getContainer().getCoreSdkHandler();
    }

    private static Repository<LogShard, SqlSpecification> logRepository() {
        return DependencyInjection.getContainer().getLogRepository();
    }

}
