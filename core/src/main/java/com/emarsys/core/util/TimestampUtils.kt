package com.emarsys.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimestampUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private val formatter = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)

    @JvmStatic
    fun formatTimestampWithUTC(timestamp: Long): String {
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(timestamp))
    }
}
