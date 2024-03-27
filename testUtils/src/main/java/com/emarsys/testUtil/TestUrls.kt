package com.emarsys.testUtil

object TestUrls {
    private const val DENNA_CUSTOM_BASE = "https://denna.gservice.emarsys.net/customResponseCode/"

    const val DENNA_ECHO = "https://denna.gservice.emarsys.net/echo"
    const val LARGE_IMAGE =
        "https://mobile-sdk-config-staging.gservice.emarsys.com/testing/Emarsys.png"

    @JvmStatic
    fun customResponse(statusCode: Int) = "$DENNA_CUSTOM_BASE$statusCode"
}