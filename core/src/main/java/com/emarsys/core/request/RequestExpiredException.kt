package com.emarsys.core.request

class RequestExpiredException(message: String, val endpoint: String?) : Exception(message)
