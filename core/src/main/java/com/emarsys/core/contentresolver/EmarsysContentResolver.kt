package com.emarsys.core.contentresolver

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.emarsys.core.Mockable

@Mockable
class EmarsysContentResolver(private val context: Context) {

    fun query(
        uri: Uri,
        projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
    }
}