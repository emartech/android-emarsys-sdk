package com.emarsys.core.connection

import android.webkit.URLUtil
import com.emarsys.core.request.model.RequestModel
import java.util.*
import javax.net.ssl.HttpsURLConnection

class ConnectionProvider {

    fun provideConnection(requestModel: RequestModel): HttpsURLConnection {
        val url = requestModel.url
        require(URLUtil.isHttpsUrl(url.toString())) { "Expected HTTPS request model, but got: " + url.protocol.uppercase(Locale.getDefault()) }
        return requestModel.url.openConnection() as HttpsURLConnection
    }
}