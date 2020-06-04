package com.emarsys.core.request.model

import android.net.Uri
import com.emarsys.core.Mockable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.util.Assert
import java.io.Serializable
import java.net.URL


public fun RequestModel.collectRequestIds(): Array<String> {
    return if (this is CompositeRequestModel) {
        originalRequestIds
    } else {
        arrayOf(id)
    }
}

@Mockable
class RequestModel @JvmOverloads constructor(private val urlStr: String,
                                                  val method: RequestMethod,
                                                  val payload: Map<String, Any?>?,
                                                  val headers: Map<String, String>,
                                                  val timestamp: Long,
                                                  val ttl: Long,
                                                  val id: String,
                                                  val url: URL = URL(urlStr)) : Serializable {

    open class Builder {
        protected lateinit var url: String
        protected var method: RequestMethod = RequestMethod.POST
        protected var payload: Map<String, Any?>? = null
        protected var headers: Map<String, String> = mapOf()

        protected var timestamp: Long
        protected var ttl: Long = Long.MAX_VALUE

        protected var id: String
        protected var queryParams: Map<String, String>? = null

        constructor(timestampProvider: TimestampProvider, uuidProvider: UUIDProvider) {
            timestamp = timestampProvider.provideTimestamp()
            id = uuidProvider.provideId()
        }

        constructor(requestModel: RequestModel) {
            Assert.notNull(requestModel, "RequestModel must not be null!")
            url = requestModel.url.toString()
            method = requestModel.method
            payload = requestModel.payload
            headers = requestModel.headers
            timestamp = requestModel.timestamp
            ttl = requestModel.ttl
            id = requestModel.id
        }

        open fun url(url: String): Builder {
            this.url = url
            return this
        }

        open fun queryParams(queryParams: Map<String, String>): Builder {
            this.queryParams = queryParams
            return this
        }

        open fun method(method: RequestMethod): Builder {
            this.method = method
            return this
        }

        open fun payload(payload: Map<String, Any?>): Builder {
            this.payload = payload
            return this
        }

        open fun headers(headers: Map<String, String>): Builder {
            this.headers = headers
            return this
        }

        open fun ttl(ttl: Long): Builder {
            this.ttl = ttl
            return this
        }

        open fun build(): RequestModel {
            return RequestModel(buildUrl(), method, payload, headers, timestamp, ttl, id)
        }

        fun buildUrl(): String {
            val uriBuilder = Uri.parse(url).buildUpon()
            if (queryParams != null && queryParams!!.isNotEmpty()) {
                for (key in queryParams!!.keys) {
                    uriBuilder.appendQueryParameter(key, queryParams!![key])
                }
            }
            return uriBuilder.build().toString()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestModel

        if (method != other.method) return false
        if (payload != other.payload) return false
        if (headers != other.headers) return false
        if (timestamp != other.timestamp) return false
        if (ttl != other.ttl) return false
        if (id != other.id) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + payload.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }
}