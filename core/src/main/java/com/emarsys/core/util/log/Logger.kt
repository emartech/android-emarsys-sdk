package com.emarsys.core.util.log

import android.os.Handler
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.endpoint.Endpoint.LOG_URL
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.util.log.entry.LogEntry
import com.emarsys.core.util.log.entry.dataWithLogLevel

@Mockable
class Logger(private val coreSdkHandler: Handler,
             private val shardRepository: Repository<ShardModel, SqlSpecification>,
             private val timestampProvider: TimestampProvider,
             private val uuidProvider: UUIDProvider) {

    companion object {

        @JvmStatic
        fun log(logEntry: LogEntry) {
            info(logEntry)
        }

        @JvmStatic
        fun info(logEntry: LogEntry) {
            if (DependencyInjection.isSetup()) {
                DependencyInjection.getContainer<DependencyContainer>().logger.persistLog(LogLevel.INFO, logEntry)
            }
        }

        @JvmStatic
        fun error(logEntry: LogEntry) {
            if (DependencyInjection.isSetup()) {
                DependencyInjection.getContainer<DependencyContainer>().logger.persistLog(LogLevel.ERROR, logEntry)
            }
        }

        @JvmStatic
        fun debug(logEntry: LogEntry) {
            if (DependencyInjection.isSetup()) {
                DependencyInjection.getContainer<DependencyContainer>().logger.persistLog(LogLevel.DEBUG, logEntry)
            }
        }
    }

    fun persistLog(logLevel: LogLevel, logEntry: LogEntry) {
        if (logEntry.topic != "log_request" || logEntry.data["url"] != LOG_URL) {
            coreSdkHandler.post {
                val shard = ShardModel.Builder(timestampProvider, uuidProvider)
                        .type(logEntry.topic)
                        .payloadEntries(logEntry.dataWithLogLevel(logLevel))
                        .build()
                shardRepository.add(shard)
            }
        }
    }
}