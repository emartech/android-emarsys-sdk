package com.emarsys.core.resource

import android.content.Context
import android.content.pm.PackageManager
import com.emarsys.core.Mockable


@Mockable
class MetaDataReader {

    fun getInt(context: Context, key: String): Int {
        var result = 0
        try {
            val ai = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            if (ai.metaData.containsKey(key)) {
                result = ai.metaData.getInt(key)
            }
        } catch (ignored: PackageManager.NameNotFoundException) {
        }
        return result
    }

    fun getInt(context: Context, key: String, defaultValue: Int): Int {
        val result = getInt(context, key)
        return if (result == 0) defaultValue else result
    }
}
