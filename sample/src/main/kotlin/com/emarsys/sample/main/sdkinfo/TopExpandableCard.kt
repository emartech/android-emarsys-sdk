package com.emarsys.sample.main.sdkinfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.emarsys.sample.dashboard.DashboardViewModel
import com.emarsys.sample.ui.cardWithFullWidth
import com.emarsys.sample.ui.component.toast.customTextToast
import com.emarsys.sample.ui.style.columnWithMaxWidth

@ExperimentalAnimationApi
@Composable
fun TopExpandableCard(context: Context, dashboardViewModel: DashboardViewModel) {
    Card(modifier = Modifier
        .clickable {
            TopCardViewModel.toggleCardExpansion()
        }
        .cardWithFullWidth(),
        elevation = 8.dp
    ) {
        AnimatedVisibility(visible = !TopCardViewModel.expanded.value) {
            LoginStatus(dashboardViewModel.hasLogin())
            MoreLessText(textToShow = TopCardViewModel.getMoreLessText())
        }
        AnimatedVisibility(visible = TopCardViewModel.expanded.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(start = 4.dp)
            ) {
                SettingsStatus(context, dashboardViewModel.getInfoMap())
                Row(horizontalArrangement = Arrangement.End) {
                    MoreLessText(textToShow = TopCardViewModel.getMoreLessText())
                }
            }
        }
    }
}

@Composable
private fun LoginStatus(isLoggedIn: Boolean) {
    Column(
        modifier = Modifier
            .columnWithMaxWidth()
            .padding(4.dp)
    ) {
        Row {
            Text(
                text = "Logged in: $isLoggedIn",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
private fun MoreLessText(textToShow: String) {
    Text(
        text = textToShow,
        style = MaterialTheme.typography.body1,
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(4.dp),
        textAlign = TextAlign.End
    )
}

@Composable
private fun SettingsStatus(context: Context, itemsToList: Map<String, String>) {
    itemsToList.forEach { item ->
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "${item.key}: ", style = MaterialTheme.typography.body1)
            Text(
                text = item.value,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        copyToClipboard(context, item.value)
                    })
                }
            )
        }
    }
}

private fun copyToClipboard(context: Context, copy: String) {
    val myClipboard =
        ContextCompat.getSystemService(
            context,
            ClipboardManager::class.java
        ) as ClipboardManager
    val clip = ClipData.newPlainText("copied text", copy)
    myClipboard.setPrimaryClip(clip)
    customTextToast(context, "Copied to clipboard, value: $copy")
}
