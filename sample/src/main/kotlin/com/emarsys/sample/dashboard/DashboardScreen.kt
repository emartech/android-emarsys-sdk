package com.emarsys.sample.dashboard

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil.annotation.ExperimentalCoilApi
import com.emarsys.Emarsys
import com.emarsys.sample.R
import com.emarsys.sample.SampleApplication
import com.emarsys.sample.dashboard.button.CheckLocationPermissionsButton
import com.emarsys.sample.dashboard.button.CopyPushTokenButton
import com.emarsys.sample.dashboard.button.TrackPushTokenButton
import com.emarsys.sample.dashboard.button.trackPushToken
import com.emarsys.sample.main.sdkinfo.SetupInfo
import com.emarsys.sample.ui.component.ColumnWithTapGesture
import com.emarsys.sample.ui.component.button.GoogleSignInButton
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.divider.DividerWithBackgroundColor
import com.emarsys.sample.ui.component.divider.GreyLine
import com.emarsys.sample.ui.component.row.RowWithCenteredContent
import com.emarsys.sample.ui.component.row.RowWithEvenlySpacedContent
import com.emarsys.sample.ui.component.screen.DetailScreen
import com.emarsys.sample.ui.component.text.TitleText
import com.emarsys.sample.ui.component.textfield.StyledTextField
import com.emarsys.sample.ui.component.toast.ErrorDialog
import com.emarsys.sample.ui.component.toast.customTextToast
import com.emarsys.sample.ui.style.rowWithPointEightWidth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlin.system.exitProcess

class DashboardScreen(
    override val context: Context,
    override val application: Application
) : DetailScreen() {
    private val viewModel = DashboardViewModel()

    @OptIn(ExperimentalMaterialApi::class)
    @ExperimentalCoilApi
    @RequiresApi(Build.VERSION_CODES.Q)
    @ExperimentalComposeUiApi
    @Composable
    override fun Detail(paddingValues: PaddingValues) {
        val bottomPadding = remember { mutableStateOf(paddingValues.calculateBottomPadding()) }

        ColumnWithTapGesture(paddingValues = bottomPadding)
        {
            TitleText(stringResource(id = R.string.sdk_config_title))
            RowWithCenteredContent {
                StyledTextField(
                    fieldToEdit = viewModel.tfAppCode,
                    label = stringResource(id = R.string.app_code_label)
                )
            }
            DividerWithBackgroundColor()
            Row(
                modifier = Modifier.rowWithPointEightWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.change_env))
                Checkbox(
                    checked = viewModel.shouldChangeEnv(),
                    onCheckedChange = { checked -> viewModel.envChangeChecked.value = checked })
                StyledTextButton(buttonText = stringResource(id = R.string.app_code_button_label)) {
                    if (viewModel.getTfAppCodeValue().isNotEmpty()) {
                        onChangeAppCodeClicked(
                            context.getString(R.string.something_went_wrong),
                            context.getString(R.string.app_code_change_success)
                        )
                    }
                }
            }
            DividerWithBackgroundColor()
            RowWithCenteredContent {
                StyledTextField(
                    fieldToEdit = viewModel.tfMerchantId,
                    label = stringResource(id = R.string.merchant_id_label)
                )
            }
            DividerWithBackgroundColor()
            Row(
                modifier = Modifier.rowWithPointEightWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                StyledTextButton(buttonText = stringResource(id = R.string.merchant_id_button_label)) {

                    if (viewModel.isMerchantIdPresent()) {
                        Emarsys.config.changeMerchantId(
                            merchantId = viewModel.getTfMerchantIdValue(),
                        )
                        SetupInfo.merchantId = viewModel.getTfMerchantIdValue()
                        SetupInfo.notifyObservers()
                        customTextToast(
                            context,
                            context.getString(R.string.merchant_id_change_success)
                        )
                    }
                }
            }
            GreyLine()
            Row {
                TitleText(titleText = stringResource(id = R.string.push_token_title))
            }
            RowWithEvenlySpacedContent {
                TrackPushTokenButton(context = context)
                CopyPushTokenButton(context = context)
            }
            GreyLine()
            Row {
                TitleText(titleText = stringResource(id = R.string.geofence_title))
            }
            RowWithEvenlySpacedContent {
                CheckLocationPermissionsButton(context = context)
                Text(text = stringResource(id = R.string.enable))
                Switch(
                    checked = viewModel.isGeofenceEnabled(),
                    onCheckedChange = { enabled ->
                        if (!viewModel.isGeofenceEnabled()) {
                            Emarsys.geofence.enable {
                                if (it != null) {
                                    customTextToast(
                                        context,
                                        context.getString(R.string.something_went_wrong)
                                    )
                                } else {
                                    viewModel.geofenceEnabled.value = enabled
                                    customTextToast(
                                        context,
                                        context.getString(R.string.geofence_enabled)
                                    )
                                }
                            }
                        } else {
                            Emarsys.geofence.disable()
                            viewModel.geofenceEnabled.value = enabled
                            customTextToast(context, context.getString(R.string.geofence_disabled))
                        }
                    }
                )
            }
            GreyLine()
            Row { TitleText(titleText = stringResource(id = R.string.contact_identification_title)) }
            RowWithCenteredContent {
                StyledTextField(
                    fieldToEdit = viewModel.tfContactFieldId,
                    label = stringResource(id = R.string.contact_field_id)
                )
            }
            DividerWithBackgroundColor()
            RowWithCenteredContent {
                StyledTextField(
                    fieldToEdit = viewModel.tfContactFieldValue,
                    label = stringResource(id = R.string.contact_field_value)
                )
            }
            DividerWithBackgroundColor()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.rowWithPointEightWidth()
            ) {
                GoogleSignInButton(
                    text = stringResource(id = R.string.google_sign_in),
                    context = context
                ) { account ->
                    onGoogleSignInClicked(account)
                }
                StyledTextButton(
                    buttonText = if (!viewModel.hasLogin()) stringResource(id = R.string.login)
                    else stringResource(id = R.string.logout)
                ) {
                    onLoginClicked()
                }
            }
            if (viewModel.isErrorVisible()) {
                ErrorDialog(
                    message = viewModel.getErrorMessage(),
                    isVisible = viewModel.getErrorVisibleField()
                )
            }
        }
    }

    private fun onGoogleSignInClicked(
        account: GoogleSignInAccount?
    ) {
        if (account == null) {
            customTextToast(context, context.getString(R.string.google_sign_in_fail))
        } else {
            Emarsys.setAuthenticatedContact(
                viewModel.getTfContactFieldIdValue().toInt(),
                account.idToken!!
            ) {
                if (verifyLogin(it, context)) {
                    setupInfoSetContact(context)
                }
            }
        }
    }

    private fun onChangeAppCodeClicked(somethingWentWrong: String, appCodeChangeSuccess: String) {
        Emarsys.config.changeApplicationCode(
            applicationCode = viewModel.getTfAppCodeValue(),
            completionListener = { changeError ->
                if (viewModel.shouldChangeEnv()) {
                    if (changeError != null) {
                        customTextToast(context, somethingWentWrong)
                    }
                    exitProcess(0)
                } else if (changeError != null) {
                    customTextToast(context, somethingWentWrong)
                } else {
                    if (viewModel.hasLogin()) {
                        setupInfoClearContact()
                        customTextToast(context, appCodeChangeSuccess)
                    } else {
                        (application as SampleApplication).setupEventHandlers()
                        customTextToast(context, appCodeChangeSuccess)
                    }
                    viewModel.resetContactInfo()
                }
            }
        )
        SetupInfo.applicationCode = viewModel.getTfAppCodeValue()
        SetupInfo.notifyObservers()
    }

    private fun onLoginClicked() {
        if (!viewModel.hasLogin()) {
            if (viewModel.isContactDataPresent()
            ) {
                Emarsys.setContact(
                    contactFieldId = viewModel.getTfContactFieldIdValue().toInt(),
                    contactFieldValue = viewModel.getTfContactFieldValue()
                ) {
                    if (verifyLogin(it, context)) {
                        setupInfoSetContact(context)
                    }
                }
            }
        } else {
            Emarsys.clearContact {
                if (it != null) {
                    customTextToast(context, context.getString(R.string.log_out_fail))
                } else {
                    setupInfoClearContact()
                    viewModel.resetContactInfo()
                    customTextToast(context, context.getString(R.string.log_out_success))
                }
            }
        }
    }

    private fun verifyLogin(
        error: Throwable?,
        context: Context
    ): Boolean {
        return if (error != null) {
            customTextToast(context, context.getString(R.string.something_went_wrong))
            Log.e("SAMPLE", error.message.toString())
            false
        } else {
            customTextToast(context, context.getString(R.string.log_in_success))
            true
        }
    }

    private fun setupInfoClearContact() {
        SetupInfo.contactFieldId = 0.toString()
        SetupInfo.contactFieldValue = ""
        SetupInfo.loggedIn = false
        SetupInfo.notifyObservers()
    }

    private fun setupInfoSetContact(context: Context) {
        SetupInfo.contactFieldValue = viewModel.getTfContactFieldValue()
        SetupInfo.contactFieldId = viewModel.getTfContactFieldIdValue()
        SetupInfo.loggedIn = true
        SetupInfo.notifyObservers()
        viewModel.isLoggedIn.value = true
        trackPushToken(context)
    }
}






