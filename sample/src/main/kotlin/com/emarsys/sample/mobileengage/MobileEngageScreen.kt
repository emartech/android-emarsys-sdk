package com.emarsys.sample.mobileengage

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.emarsys.Emarsys
import com.emarsys.sample.R
import com.emarsys.sample.ui.component.ColumnWithTapGesture
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.divider.DividerWithBackgroundColor
import com.emarsys.sample.ui.component.divider.GreyLine
import com.emarsys.sample.ui.component.screen.DetailScreen
import com.emarsys.sample.ui.component.text.TitleText
import com.emarsys.sample.ui.component.textfield.EventPayloadTextArea
import com.emarsys.sample.ui.component.textfield.StyledTextField
import com.emarsys.sample.ui.component.toast.ErrorDialog
import com.emarsys.sample.ui.style.rowWithPointEightWidth

class MobileEngageScreen(
    override val context: Context
) : DetailScreen() {
    private val viewModel = MobileEngageViewModel()
    private val trackCustomEvent = {
        if (viewModel.isEventPresent()) {
            Emarsys.trackCustomEvent(
                viewModel.getCustomEvent(),
                viewModel.getPayloadMap()
            ) {
                if (it != null) {
                    Log.e("ERROR", it.toString())
                } else {
                    Toast.makeText(context, context.getString(R.string.event_track_success), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @ExperimentalCoilApi
    @ExperimentalComposeUiApi
    @Composable
    override fun Detail(paddingValues: PaddingValues) {
        val bottomPadding = remember { mutableStateOf(paddingValues.calculateBottomPadding()) }

        ColumnWithTapGesture(paddingValues = bottomPadding)
        {
            TitleText(titleText = stringResource(id = R.string.me_title))
            StyledTextField(
                fieldToEdit = viewModel.getCustomEventField(),
                label = stringResource(id = R.string.custom_event_label)
            )
            DividerWithBackgroundColor()
            EventPayloadTextArea(fieldToEdit = viewModel.getPayloadField(), stringResource(id = R.string.custom_event_payload))
            Row(
                modifier = Modifier
                    .rowWithPointEightWidth()
                    .padding(2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                StyledTextButton(buttonText = stringResource(id = R.string.track)) {
                    trackCustomEvent()
                }
            }
            GreyLine()
            if (viewModel.isErrorVisible()) {
                ErrorDialog(
                    message = viewModel.getErrorMessage(),
                    isVisible = viewModel.getErrorVisibleField()
                )
            }
        }
    }
}