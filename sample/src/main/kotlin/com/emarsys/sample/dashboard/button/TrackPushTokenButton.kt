package com.emarsys.sample.dashboard.button

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.emarsys.Emarsys
import com.emarsys.sample.R
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.toast.customTextToast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.huawei.hms.aaid.HmsInstanceId

@Composable
fun TrackPushTokenButton(context: Context) {
    StyledTextButton(buttonText = stringResource(id = R.string.track_push_token_button_label)) {
        trackPushToken(context)
    }
}

fun trackPushToken(context: Context) {
    if (GoogleApiAvailabilityLight.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    ) {
        FirebaseApp.initializeApp(context)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            task.addOnSuccessListener {
                val pushToken = it
                Emarsys.push.pushToken = pushToken
                customTextToast(context, context.getString(R.string.track_push_token_success))
            }
            task.addOnFailureListener {
                it.printStackTrace()
                customTextToast(context, context.getString(R.string.track_push_token_failed))
            }
        }
    } else {
        Thread {
            val pushToken = HmsInstanceId.getInstance(context)
                .getToken(context.getString(R.string.app_id), context.getString(R.string.push_scope))
            Emarsys.push.pushToken = pushToken
        }.start()
        customTextToast(context, context.getString(R.string.track_push_token_success))
    }
}