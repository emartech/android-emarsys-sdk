package com.emarsys.sample.dashboard.button

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
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
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("PERMISSION", "Permission granted: $isGranted")
    }

    StyledTextButton(buttonText = stringResource(id = R.string.track_push_token_button_label)) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
                .getToken(
                    context.getString(R.string.app_id),
                    context.getString(R.string.push_scope)
                )
            Emarsys.push.pushToken = pushToken
        }.start()
        customTextToast(context, context.getString(R.string.track_push_token_success))
    }
}