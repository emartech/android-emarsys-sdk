package com.emarsys.sample.dashboard.button

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.emarsys.sample.R
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.toast.customTextToast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.huawei.hms.aaid.HmsInstanceId

@Composable
fun CopyPushTokenButton(context: Context) {
    val myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    StyledTextButton(buttonText = stringResource(id = R.string.copy_push_token_button_label)) {
        if (GoogleApiAvailabilityLight.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        ) {
            FirebaseApp.initializeApp(context)
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                run {
                    task.addOnSuccessListener {
                        val pushToken = it
                        val clip = ClipData.newPlainText("copied text", it)
                        myClipboard.setPrimaryClip(clip)
                        context.copyToClipboard("copied text", it)
                        customTextToast(
                            context,
                            "${context.getString(R.string.push_token_copied)} $pushToken"
                        )
                    }
                    task.addOnFailureListener {
                        it.printStackTrace()
                        customTextToast(context, context.getString(R.string.something_went_wrong))
                    }
                }
            }
        } else {
            customTextToast(context, context.getString(R.string.something_went_wrong))
            Thread {
                val pushToken = HmsInstanceId.getInstance(context)
                    .getToken(
                        context.getString(R.string.app_id),
                        context.getString(R.string.push_scope)
                    )
                val clip = ClipData.newPlainText("copied text", pushToken)
                myClipboard.setPrimaryClip(clip)
                customTextToast(
                    context,
                    "${context.getString(R.string.push_token_copied)} $pushToken"
                )
            }.start()
        }
    }
}

fun Context.copyToClipboard(clipLabel: String, text: CharSequence) {
    val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(clipLabel, text))
}
