package com.emarsys.core.request.model

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider


class CompositeRequestModel(
        id: String,
        url: String,
        method: RequestMethod,
        payload: Map<String, Any?>?,
        headers: Map<String, String>,
        timestamp: Long,
        ttl: Long,
        val originalRequestIds: Array<String>) : RequestModel(url, method, payload, headers, timestamp, ttl, id) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as CompositeRequestModel
        return originalRequestIds.contentEquals(that.originalRequestIds)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + originalRequestIds.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "CompositeRequestModel{" +
                "request=" + super.toString() +
                "originalRequestIds=" + originalRequestIds.contentToString() +
                '}'
    }

    class Builder : RequestModel.Builder {
        private var originalRequestIds: Array<String> = arrayOf()

        constructor(timestampProvider: TimestampProvider, uuidProvider: UUIDProvider) :
                super(timestampProvider, uuidProvider)

        constructor(requestModel: RequestModel) : super(requestModel) {
            originalRequestIds = (requestModel as CompositeRequestModel).originalRequestIds
        }

        override fun url(url: String): Builder {
            super.url(url)
            return this
        }

        override fun queryParams(queryParams: Map<String, String>): Builder {
            super.queryParams(queryParams)
            return this
        }

        override fun method(method: RequestMethod): Builder {
            super.method(method)
            return this
        }

        override fun payload(payload: Map<String, Any?>): Builder {
            super.payload(payload)
            return this
        }

        override fun headers(headers: Map<String, String>): Builder {
            super.headers(headers)
            return this
        }

        override fun ttl(ttl: Long): Builder {
            super.ttl(ttl)
            return this
        }

        fun originalRequestIds(originalRequestIds: Array<String>): Builder {
            this.originalRequestIds = originalRequestIds
            return this
        }

        override fun build(): CompositeRequestModel {
            return CompositeRequestModel(id, buildUrl(), method, payload, headers, timestamp, ttl, originalRequestIds)
        }
    }

}