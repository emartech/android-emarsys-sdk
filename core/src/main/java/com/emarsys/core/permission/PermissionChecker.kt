package com.emarsys.core.permission

import android.content.Context
import androidx.core.content.ContextCompat
import com.emarsys.core.Mockable

@Mockable
class PermissionChecker(private val context: Context) {
    fun checkSelfPermission(permission: String): Int {
        return ContextCompat.checkSelfPermission(context, permission)
    }
}