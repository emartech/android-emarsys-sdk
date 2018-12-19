package com.emarsys.core.util.log;

import android.os.Handler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.di.DependencyContainer;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.log.entry.LogEntry;

public class Logger {

    public static void log(final LogEntry logEntry) {
        coreSdkHandler().post(new Runnable() {
            @Override
            public void run() {
                ShardModel shard = new ShardModel.Builder(timestampProvider(), uuidProvider())
                        .type(logEntry.getTopic())
                        .payloadEntries(logEntry.getData())
                        .build();
                shardRepository().add(shard);
            }
        });
    }

    private static DependencyContainer container() {
        return DependencyInjection.getContainer();
    }

    private static Handler coreSdkHandler() {
        return container().getCoreSdkHandler();
    }

    private static Repository<ShardModel, SqlSpecification> shardRepository() {
        return container().getShardRepository();
    }

    private static TimestampProvider timestampProvider() {
        return container().getTimestampProvider();
    }

    private static UUIDProvider uuidProvider() {
        return container().getUuidProvider();
    }

}
