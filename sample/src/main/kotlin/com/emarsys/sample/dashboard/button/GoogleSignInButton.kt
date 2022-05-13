package com.emarsys.sample.ui.component.button

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.emarsys.sample.R
import com.emarsys.sample.dashboard.GoogleAuthResult
import com.emarsys.sample.ui.component.toast.customTextToast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay


@ExperimentalMaterialApi
@Composable
fun GoogleSignInButton(
    text: String,
    context: Context,
    borderColor: Color = Color.LightGray,
    backgroundColor: Color = MaterialTheme.colors.surface,
    progressIndicatorColor: Color = MaterialTheme.colors.primary,
    onClick: (account: GoogleSignInAccount?) -> Unit
) {
    val signInRequestCode = 1
    val isLoading = remember { mutableStateOf(false) }

    val authResultLauncher =
        rememberLauncherForActivityResult(contract = GoogleAuthResult()) { task ->
            try {
                val account = task?.getResult(ApiException::class.java)
                onClick.invoke(account)
            } catch (e: ApiException) {
                customTextToast(context, "Google sign in failed")
            }
        }

    Surface(
        modifier = Modifier.clickable(
            enabled = !isLoading.value,
            onClick = {
                isLoading.value = true
                authResultLauncher.launch(signInRequestCode)
            }
        ),
        shape = Shapes().medium,
        border = BorderStroke(width = 1.dp, color = borderColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "SignInButton",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text)
            if (isLoading.value) {
                LaunchedEffect(key1 = isLoading.value) {
                    if (isLoading.value) {
                        delay(1000)
                        isLoading.value = false
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp,
                    color = progressIndicatorColor
                )
            }
        }
    }
}