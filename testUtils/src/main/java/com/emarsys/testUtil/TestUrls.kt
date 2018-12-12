package com.emarsys.testUtil

object TestUrls {
    private const val DENNA_CUSTOM_BASE = "https://ems-denna.herokuapp.com/customResponseCode/"

    const val DENNA_ECHO = "https://ems-denna.herokuapp.com/echo"
    const val LARGE_IMAGE = "https://s3-eu-west-1.amazonaws.com/ems-mobileteam-artifacts/test-resources/Emarsys.png"

    @JvmStatic
    fun customResponse(statusCode: Int) = "$DENNA_CUSTOM_BASE$statusCode"
}