package com.emarsys.core.util

object DatabaseUtil {

    @JvmStatic
    fun generateInStatement(columnName: String, args: Array<String>): String {
        return buildString {
            append("$columnName IN (?")
            for (i in 1 until args.size) {
                append(", ?")
            }
            append(")")
        }
    }
}
