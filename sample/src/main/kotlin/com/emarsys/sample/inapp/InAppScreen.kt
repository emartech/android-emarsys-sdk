package com.emarsys.sample.inapp

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.annotation.ExperimentalCoilApi
import com.emarsys.Emarsys
import com.emarsys.inapp.ui.InlineInAppView
import com.emarsys.sample.R
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.divider.DividerWithBackgroundColor
import com.emarsys.sample.ui.component.divider.GreyLine
import com.emarsys.sample.ui.component.row.RowWithCenteredContent
import com.emarsys.sample.ui.component.screen.DetailScreen
import com.emarsys.sample.ui.component.text.TitleText
import com.emarsys.sample.ui.component.textfield.StyledTextField
import com.emarsys.sample.ui.component.toast.ErrorDialog
import com.emarsys.sample.ui.component.toast.customTextToast
import com.emarsys.sample.ui.style.columnWithMaxWidth
import com.emarsys.sample.ui.style.rowWithMaxWidth
import com.emarsys.sample.ui.style.rowWithPointEightWidth
import java.util.*

class InAppScreen(
    override val context: Context,
    override val application: Application
) : DetailScreen() {
    private val trackCustomEvent = {
        if (viewModel.isEventPresent()) {
            Emarsys.trackCustomEvent(
                viewModel.getCustomEvent(),
                null
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

    private val viewModel = InAppViewModel()

    @OptIn(ExperimentalAnimationApi::class)
    @ExperimentalCoilApi
    @ExperimentalComposeUiApi
    @Composable
    override fun Detail(paddingValues: PaddingValues) {
        viewModel.removeInAppFromMap()
        val bottomPadding = remember { mutableStateOf(paddingValues.calculateBottomPadding()) }
        val focusManager = LocalFocusManager.current

        if (viewModel.isErrorVisible()) {
            ErrorDialog(
                message = viewModel.getErrorMessage(),
                isVisible = viewModel.getErrorVisibleField()
            )
        }
        LazyColumn(
            modifier = Modifier
                .columnWithMaxWidth()
                .padding(bottom = bottomPadding.value)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            item { TitleText(titleText = stringResource(id = R.string.inapp_title)) }
            item {
                StyledTextField(
                    fieldToEdit = viewModel.getCustomEventField(),
                    label = stringResource(id = R.string.custom_event_label)
                )
            }
            item {
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
            }
            item {
                Row(
                    modifier = Modifier.rowWithMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.dnd_switch_label))
                    Switch(
                        checked = viewModel.isDnd(),
                        onCheckedChange = {
                            if (viewModel.isDnd()) {
                                Emarsys.inApp.resume()
                                customTextToast(context, context.getString(R.string.inapp_enabled))
                            } else {
                                Emarsys.inApp.pause()
                                customTextToast(context, context.getString(R.string.inapp_paused))
                            }
                            viewModel.switchDnd()
                        }
                    )
                }
            }
            item { GreyLine() }
            item { TitleText(titleText = stringResource(id = R.string.inline_inapp_title)) }
            item {
                StyledTextField(
                    fieldToEdit = viewModel.viewIdField,
                    label = stringResource(id = R.string.inline_inapp_label)
                )
            }
            item { DividerWithBackgroundColor() }
            item {
                StyledTextButton(buttonText = stringResource(id = R.string.inline_inapp_button_label)) {
                    if (viewModel.isViewIdPresent()) {
                        viewModel.addViewIdToMap()
                    }
                }
            }
            item { DividerWithBackgroundColor() }
            items(viewModel.viewIdsWithUuid.keys.toList()) { mapEntryKey ->
                CreateInAppView(
                    context = context,
                    inApp = viewModel.viewIdsWithUuid[mapEntryKey]!!,
                    uuid = mapEntryKey
                )
            }
        }
    }


    @Composable
    private fun CreateInAppView(
        context: Context,
        inApp: String,
        uuid: UUID
    ) {
        val isVisible = remember { mutableStateOf(true) }
        if (isVisible.value) {
            RowWithCenteredContent {
                Card(elevation = 5.dp) {
                    AndroidView(factory = {
                        InlineInAppView(context).apply {
                            loadInApp(inApp)
                            this.onCloseListener = {
                                isVisible.value = false
                                viewModel.closedInApps.add(uuid)
                                this.visibility = View.GONE
                            }
                            this.onAppEventListener = { property, json ->
                                customTextToast(context, "AppEvent - $property, $json")
                                isVisible.value = false
                                viewModel.closedInApps.add(uuid)
                            }
                        }
                    })
                }
            }
        }
    }
}
