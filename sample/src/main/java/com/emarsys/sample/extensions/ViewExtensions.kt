package com.emarsys.sample.extensions

import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.emarsys.sample.R
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(message: String) = run {
    val snackBar: Snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    val snackBarView = snackBar.view
    val snackTextView = snackBarView
            .findViewById<TextView>(R.id.snackbar_text)
    snackTextView.gravity = Gravity.CENTER_HORIZONTAL
    snackBar.show()
}