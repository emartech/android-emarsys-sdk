package com.emarsys.core.util.log

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.emarsys.core.Mockable
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.CoreComponent
import com.emarsys.core.di.core
import com.emarsys.core.endpoint.Endpoint.LOG_URL
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.provider.wrapper.WrapperInfoContainer
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.log.LogLevel.*
import com.emarsys.core.util.log.entry.*

@Mockable
class Logger(
    private val concurrentHandlerHolder: ConcurrentHandlerHolder,
    private val shardRepository: Repository<ShardModel, SqlSpecification>,
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UUIDProvider,
    private val logLevelStorage: Storage<String?>,
    private val verboseConsoleLoggingEnabled: Boolean,
    private val context: Context
) {

    companion object {
        const val TAG = "Emarsys SDK"

        @JvmStatic
        fun log(logEntry: LogEntry) {
            info(logEntry)
        }

        @JvmStatic
        fun info(logEntry: LogEntry, strict: Boolean = false) {
            if (CoreComponent.isSetup()) {
                if (strict) {
                    if (core().logLevelStorage.get() == "INFO") {
                        core().logger.handleLog(INFO, logEntry)
                    }
                } else {
                    core().logger.handleLog(INFO, logEntry)
                }
            }
        }

        @JvmStatic
        fun error(logEntry: LogEntry) {
            if (CoreComponent.isSetup()) {
                core().logger.handleLog(ERROR, logEntry)
            }
        }

        @JvmStatic
        fun debug(logEntry: LogEntry, strict: Boolean = false) {
            if (CoreComponent.isSetup()) {
                if (strict) {
                    if (core().logger.logLevelStorage.get() == "DEBUG") {
                        core().logger.handleLog(DEBUG, logEntry)
                    }
                } else {
                    core().logger.handleLog(DEBUG, logEntry)
                }
            }
        }

        @JvmStatic
        fun metric(logEntry: LogEntry) {
            if (CoreComponent.isSetup()) {
                core().logger.handleLog(METRIC, logEntry)
            }
        }
    }

    fun handleLog(logLevel: LogLevel, logEntry: LogEntry, onCompleted: (() -> Unit)? = null) {
        val currentThreadName = Thread.currentThread().name
        core().concurrentHandlerHolder.coreHandler.post {
            val isDebugMode: Boolean =
                0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            if ((verboseConsoleLoggingEnabled || logEntry is MethodNotAllowed) && isDebugMode) {
                logToConsole(logLevel, logEntry)
            }
            persistLog(logLevel, logEntry, currentThreadName, onCompleted)
        }
    }

    private fun logToConsole(logLevel: LogLevel, logEntry: LogEntry) {
        when (logLevel) {
            DEBUG ->
                Log.d(TAG, logEntry.asString())
            TRACE ->
                Log.v(TAG, logEntry.asString())
            INFO ->
                Log.i(TAG, logEntry.asString())
            WARN ->
                Log.w(TAG, logEntry.asString())
            ERROR ->
                if (logEntry is CrashLog) {
                    Log.e(TAG, logEntry.asString(), logEntry.throwable)
                } else {
                    Log.e(TAG, logEntry.asString())
                }
            else -> {
            }
        }
    }

    fun persistLog(
        logLevel: LogLevel,
        logEntry: LogEntry,
        currentThreadName: String,
        onCompleted: (() -> Unit)? = null
    ) {
        if (isAppStartLog(logEntry)
            || (isNotLogLog(logEntry) && shouldLogBasedOnRemoteConfig(logLevel))
        ) {
            concurrentHandlerHolder.coreHandler.post {
                val shard = ShardModel.Builder(timestampProvider, uuidProvider)
                    .type(logEntry.topic)
                    .payloadEntries(
                        logEntry.toData(
                            logLevel,
                            currentThreadName,
                            WrapperInfoContainer.wrapperInfo
                        )
                    )
                    .build()
                shardRepository.add(shard)
                onCompleted?.invoke()
            }
        } else {
            onCompleted?.invoke()
        }
    }

    private fun shouldLogBasedOnRemoteConfig(logLevel: LogLevel): Boolean {
        val savedLogLevel: LogLevel =
            if (logLevelStorage.get() == null) ERROR else valueOf(logLevelStorage.get()!!)

        return logLevel.priority >= savedLogLevel.priority
    }

    private fun isNotLogLog(logEntry: LogEntry) = logEntry.data["url"] != LOG_URL

    private fun isAppStartLog(logEntry: LogEntry) = logEntry.topic == "app:start"
}