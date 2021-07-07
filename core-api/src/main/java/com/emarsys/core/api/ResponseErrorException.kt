package com.emarsys.core.api

import java.lang.Exception

data class ResponseErrorException(val statusCode: Int, val statusMessage: String?, val body: String?) : Exception(statusMessage)