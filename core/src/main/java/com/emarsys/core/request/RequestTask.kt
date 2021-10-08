package com.emarsys.core.request

import android.os.AsyncTask
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.Mapper
import com.emarsys.core.connection.ConnectionProvider
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseHandlersProcessor
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

open class RequestTask(
        private val requestModel: RequestModel,
        private val coreCompletionHandler: CoreCompletionHandler,
        private val connectionProvider: ConnectionProvider,
        private val timestampProvider: TimestampProvider,
        private val responseHandlersProcessor: ResponseHandlersProcessor,
        private val requestModelMappers: List<Mapper<RequestModel, RequestModel>>,
        private val coreSdkHandler: CoreSdkHandler) : AsyncTask<Void, Long, Void>() {

    companion object {
        private const val TIMEOUT = 30000
    }

    private var responseModel: ResponseModel? = null
    private var exception: Exception? = null

    public override fun doInBackground(vararg params: Void?): Void? {
        val dbEnd = timestampProvider.provideTimestamp()
        var connection: HttpsURLConnection? = null
        try {
            val updatedRequestModel = mapRequestModel(requestModel)
            connection = connectionProvider.provideConnection(updatedRequestModel)
            initializeConnection(connection, updatedRequestModel)
            connection.connectTimeout = 20000
            connection.connect()
            sendBody(connection, updatedRequestModel)
            responseModel = readResponse(connection)

            debug(RequestLog(responseModel!!, dbEnd, updatedRequestModel))
            info(RequestLog(responseModel!!, dbEnd), strict = true)

        } catch (e: Exception) {
            exception = e
        } finally {
            connection?.disconnect()
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        coreSdkHandler.post {
            if (exception != null) {
                coreCompletionHandler.onError(requestModel.id, exception!!)
            } else if (responseModel != null) {
                responseHandlersProcessor.process(responseModel)
                if (isStatusCodeOK(responseModel!!.statusCode)) {
                    coreCompletionHandler.onSuccess(requestModel.id, responseModel!!)
                } else {
                    coreCompletionHandler.onError(requestModel.id, responseModel!!)
                }
            }
        }
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
            val payload = fromMap(model.payload!!.filterNotNull()).toString().toByteArray(StandardCharsets.UTF_8)
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
        val inputStream: InputStream
        inputStream = if (isStatusCodeOK(responseCode)) {
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

    private fun mapRequestModel(requestModel: RequestModel): RequestModel {
        var updatedRequestModel = requestModel
        for (mapper in requestModelMappers) {
            updatedRequestModel = mapper.map(updatedRequestModel)
        }
        return updatedRequestModel
    }
}