package com.emarsys.core.request

import com.emarsys.core.Mockable
import com.emarsys.core.api.result.Try
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.util.JsonUtils.fromMap
import com.emarsys.core.util.filterNotNull
import com.emarsys.core.util.log.Logger.Companion.debug
import com.emarsys.core.util.log.Logger.Companion.info
import com.emarsys.core.util.log.entry.RequestLog
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

@Mockable
open class RequestTask(
    private val requestModel: RequestModel,
    private val connectionProvider: ConnectionProvider,
    private val timestampProvider: TimestampProvider
) {

    companion object {
        private const val TIMEOUT = 30000
    }

    open fun execute(): Try<ResponseModel> {
        var responseModel: ResponseModel? = null
        var exception: Exception? = null
        val dbEnd = timestampProvider.provideTimestamp()
        var connection: HttpsURLConnection? = null
        try {
            connection = connectionProvider.provideConnection(requestModel)
            initializeConnection(connection, requestModel)
            connection.connectTimeout = 20000
            connection.connect()

            sendBody(connection, requestModel)
            responseModel = readResponse(connection)

            debug(RequestLog(responseModel, dbEnd, requestModel))
            info(RequestLog(responseModel, dbEnd))

        } catch (e: Exception) {
            exception = e
        } finally {
            connection?.disconnect()
        }
        return Try(responseModel, exception)
    }

    private fun initializeConnection(connection: HttpsURLConnection, model: RequestModel) {
        connection.requestMethod = model.method.name
        setHeaders(connection, model.headers)
        connection.connectTimeout = TIMEOUT
        if (model.method != RequestMethod.GET && model.payload != null) {
            connection.doOutput = true
        }
    }

    private fun setHeaders(connection: HttpsURLConnection, headers: Map<String, String>) {
        for ((key, value) in headers) {
            connection.setRequestProperty(key, value)
        }
    }

    private fun sendBody(connection: HttpsURLConnection, model: RequestModel) {
        if (model.payload != null) {
            val payload = fromMap(model.payload!!.filterNotNull()).toString()
                .toByteArray(StandardCharsets.UTF_8)
            val writer = BufferedOutputStream(connection.outputStream)
            writer.write(payload)
            writer.close()
        }
    }

    private fun readResponse(connection: HttpsURLConnection?): ResponseModel {
        val statusCode = connection!!.responseCode
        val message = connection.responseMessage
        val headers = connection.headerFields
        val body = readBody(connection)
        return ResponseModel.Builder(timestampProvider)
            .statusCode(statusCode)
            .message(message)
            .headers(headers)
            .body(body)
            .requestModel(requestModel)
            .build()
    }

    private fun readBody(connection: HttpsURLConnection?): String {
        val responseCode = connection!!.responseCode
        val inputStream: InputStream = if (isStatusCodeOK(responseCode)) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val reader = BufferedReader(InputStreamReader(inputStream))
        var inputLine: String?
        val sb = StringBuilder()
        while (reader.readLine().also { inputLine = it } != null) {
            sb.append(inputLine)
        }
        reader.close()
        return sb.toString()
    }

    private fun isStatusCodeOK(responseCode: Int): Boolean {
        return responseCode in 200..299
    }
}