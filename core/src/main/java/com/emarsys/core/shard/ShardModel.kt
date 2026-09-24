package com.emarsys.core.shard

import com.emarsys.core.Mockable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider

@Mockable
data class ShardModel(
    val id: String,
    val type: String,
    val data: Map<String, Any?>,
    val timestamp: Long,
    val ttl: Long
) {
    class Builder(timestampProvider: TimestampProvider, uuidProvider: UUIDProvider) {
        private val id: String = uuidProvider.provideId()
        private val timestamp: Long = timestampProvider.provideTimestamp()
        private var ttl: Long = Long.MAX_VALUE
        private var type: String? = null
        private val payload: MutableMap<String, Any?> = mutableMapOf()

        fun type(type: String): Builder {
            this.type = type
            return this
        }

        fun ttl(ttl: Long): Builder {
            this.ttl = ttl
            return this
        }

        fun payloadEntry(key: String, value: Any?): Builder {
            payload[key] = value
            return this
        }

        fun payloadEntries(entries: Map<String, Any?>): Builder {
            payload.putAll(entries)
            return this
        }

        fun build(): ShardModel {
            requireNotNull(type) { "Type must not be null!" }
            return ShardModel(id, type!!, payload, timestamp, ttl)
        }
    }
}
