package com.emarsys.sample.dashboard.button

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat.checkSelfPermission
import com.emarsys.sample.ui.component.button.StyledTextButton

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CheckLocationPermissionsButton(context: Context) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("PERMISSION", "Permission granted: $isGranted")
    }

    StyledTextButton(buttonText = "REQUEST PERMISSIONS") {
        if (checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            launcher.launch(ACCESS_FINE_LOCATION)
        }
        if (checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            launcher.launch(ACCESS_COARSE_LOCATION)
        }
        if (checkSelfPermission(context, ACCESS_BACKGROUND_LOCATION) != PERMISSION_GRANTED) {
            launcher.launch(ACCESS_BACKGROUND_LOCATION)
        }
    }
}
