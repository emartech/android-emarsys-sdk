package com.emarsys.core.response

import com.emarsys.core.Mockable
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestModel
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpCookie

@Mockable
data class ResponseModel(
    val statusCode: Int,
    val message: String,
    val headers: Map<String?, String>,
    val cookies: Map<String, HttpCookie>,
    val body: String?,
    val timestamp: Long,
    val requestModel: RequestModel) {

    init {
        checkStatusCode(statusCode)
    }

    private fun checkStatusCode(statusCode: Int) {
        require(!(statusCode < 200 || statusCode >= 600)) { "Status code must be between 2xx and 5xx!" }
    }

    val parsedBody: JSONObject?
        get() {
            var result : JSONObject? = null
            if (body != null) {
                try {
                    result = JSONObject(body)
                } catch (ignored: JSONException) {
                }
            }
            return result
        }

    class Builder @JvmOverloads constructor(private val timestampProvider: TimestampProvider = TimestampProvider()) {
        private var statusCode: Int? = null
        private var message: String? = null
        private var headers: Map<String?, String> = HashMap()
        private var cookies: Map<String, HttpCookie> = HashMap()
        private var body: String? = null
        private var requestModel: RequestModel? = null

        fun statusCode(statusCode: Int): Builder {
            this.statusCode = statusCode
            return this
        }

        fun message(message: String?): Builder {
            this.message = message
            return this
        }

        fun headers(headers: Map<String?, List<String>>): Builder {
            this.headers = convertHeaders(headers)
            cookies = extractCookies(headers)
            return this
        }

        fun body(body: String?): Builder {
            this.body = body
            return this
        }

        fun requestModel(requestModel: RequestModel?): Builder {
            this.requestModel = requestModel
            return this
        }

        fun build(): ResponseModel {
            return ResponseModel(
                statusCode!!,
                message!!,
                headers,
                cookies,
                body,
                timestampProvider.provideTimestamp(),
                requestModel!!
            )
        }

        fun convertHeaders(headers: Map<String?, List<String?>>): Map<String?, String> {
            val result: MutableMap<String?, String> = HashMap()
            val entries = headers.entries
            for ((key, value) in entries) {
                val header = value.joinToString(", ")
                result[key] = header
            }
            return result
        }

        private fun extractCookies(headers: Map<String?, List<String?>>): Map<String, HttpCookie> {
            val result: MutableMap<String, HttpCookie> = HashMap()
            val cookieKey = "set-cookie"

            headers.entries.forEach {
                if (it.key.equals(cookieKey, true)) {
                    it.value.forEach { rawCookies ->
                        val cookies = HttpCookie.parse(rawCookies)
                        for (cookie in cookies) {
                            result[cookie.name] = cookie
                        }
                    }
                }
            }
            return result
        }
    }
}