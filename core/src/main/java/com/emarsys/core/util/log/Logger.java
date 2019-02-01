package com.emarsys.core.util.log;

import android.os.Handler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.entry.LogEntry;

public class Logger {

    private final Handler coreSdkHandler;
    private final Repository<ShardModel, SqlSpecification> shardRepository;
    private final TimestampProvider timestampProvider;
    private final UUIDProvider uuidProvider;

    public Logger(Handler coreSdkHandler,
                  Repository<ShardModel, SqlSpecification> shardRepository,
                  TimestampProvider timestampProvider,
                  UUIDProvider uuidProvider) {
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");
        Assert.notNull(shardRepository, "ShardRepository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(uuidProvider, "UuidProvider must not be null!");

        this.coreSdkHandler = coreSdkHandler;
        this.shardRepository = shardRepository;
        this.timestampProvider = timestampProvider;
        this.uuidProvider = uuidProvider;
    }

    public static void log(final LogEntry logEntry) {
        if (DependencyInjection.isSetup()) {
            DependencyInjection.getContainer().getLogger().persistLog(logEntry);
        }
    }

    public void persistLog(final LogEntry logEntry) {
        coreSdkHandler.post(new Runnable() {
            @Override
            public void run() {
                ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                        .type(logEntry.getTopic())
                        .payloadEntries(logEntry.getData())
                        .build();
                shardRepository.add(shard);
            }
        });
    }
}
