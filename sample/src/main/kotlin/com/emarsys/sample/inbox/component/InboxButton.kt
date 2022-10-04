package com.emarsys.sample.inbox.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emarsys.mobileengage.api.action.ActionModel
import com.emarsys.mobileengage.api.action.AppEventActionModel
import com.emarsys.mobileengage.api.action.OpenExternalUrlActionModel
import com.emarsys.sample.inbox.event.InboxAppEventHandler
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.toast.customTextToast
import com.emarsys.sample.ui.style.rowWithMaxWidth
import org.json.JSONObject

@Composable
fun InboxButton(
    context: Context,
    actionModels: List<ActionModel>,
    inboxAppEventHandler: InboxAppEventHandler
) {
    Row(
        modifier = Modifier
            .rowWithMaxWidth()
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        actionModels.forEach { actionModel ->
            when (actionModel) {
                is AppEventActionModel -> {
                    AppEventButton(context, actionModel, inboxAppEventHandler)
                }
                is OpenExternalUrlActionModel -> {
                    OpenUrlButton(context = context, actionModel = actionModel)
                }
                else -> {
                    DefaultButton(context = context, actionModel = actionModel)
                }
            }
        }
    }
}

@Composable
private fun AppEventButton(
    context: Context,
    actionModel: AppEventActionModel,
    inboxAppEventHandler: InboxAppEventHandler
) {
    val json = actionModel.payload?.let { JSONObject(it) }
    StyledTextButton(onClick = {
        inboxAppEventHandler.handleEvent(context, actionModel.name, json)
    }, buttonText = actionModel.title)
}

@Composable
private fun OpenUrlButton(context: Context, actionModel: OpenExternalUrlActionModel) {
    StyledTextButton(
        onClick = { onOpenExternalUrlTriggered(context, actionModel) },
        buttonText = actionModel.title
    )
}

@Composable
private fun DefaultButton(context: Context, actionModel: ActionModel) {
    StyledTextButton(
        onClick = { customTextToast(context, actionModel.id) },
        buttonText = actionModel.title
    )
}

fun onOpenExternalUrlTriggered(context: Context, actionModel: OpenExternalUrlActionModel) {
    val link = Uri.parse(actionModel.url.toString())
    val externalUrlIntent = Intent(Intent.ACTION_VIEW, link)
    externalUrlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(externalUrlIntent)
}