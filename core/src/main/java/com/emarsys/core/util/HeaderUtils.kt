package com.emarsys.core.util

import android.util.Base64

object HeaderUtils {

    fun createBasicAuth(username: String): String {
        val credentials = "$username:"
        return "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    }
}
