package com.emarsys.sample.ui.component.toast

import android.content.Context
import android.widget.Toast

fun customTextToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}