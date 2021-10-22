package com.emarsys.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.emarsys.core.database.DatabaseContract
import com.emarsys.core.database.helper.CoreDbHelper


class SharedHardwareIdentificationContentProvider : ContentProvider() {
    private lateinit var coreDbHelper: CoreDbHelper

    override fun onCreate(): Boolean {
        this.context?.let {
            coreDbHelper = CoreDbHelper(it, mutableMapOf())
        }
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return if (context != null) {
            if (uri == DatabaseContract.getHardwareIdProviderUri(context!!.packageName)) {
                coreDbHelper.readableCoreDatabase.query(false, DatabaseContract.HARDWARE_IDENTIFICATION_TABLE_NAME,
                        arrayOf(DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID,
                                DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT,
                                DatabaseContract.HARDWARE_IDENTIFICATION_COLUMN_NAME_IV),
                        null, null, null, null, null, null)
            } else {
                null
            }
        } else {
            null
        }
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }
}