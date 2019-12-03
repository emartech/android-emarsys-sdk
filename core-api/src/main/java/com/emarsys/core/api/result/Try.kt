package com.emarsys.core.api.result

class Try<T>(val result: T?, val errorCause: Throwable?) {

    companion object {
        @JvmStatic
        fun <T> success(result: T): Try<T> {
            return Try(result, null)
        }

        @JvmStatic
        fun <T> failure(errorCause: Exception?): Try<T> {
            return Try(null as T?, errorCause)
        }
    }
}