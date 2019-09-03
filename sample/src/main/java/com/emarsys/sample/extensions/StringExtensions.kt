package com.emarsys.sample.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun String.copyToClipboard(context: Context) = run {
    val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(this, this)
    clipBoard.setPrimaryClip(clipData)
}