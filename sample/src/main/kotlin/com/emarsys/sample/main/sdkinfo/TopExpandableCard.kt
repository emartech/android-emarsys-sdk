package com.emarsys.sample.main.sdkinfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import com.emarsys.sample.ui.style.cardWithFullWidth
import com.emarsys.sample.ui.style.columnWithMaxWidth
import com.emarsys.sample.ui.component.toast.customTextToast

class TopExpandableCard {

    @ExperimentalAnimationApi
    @Composable
    fun TopExpandableCard(context: Context) {
        Card(modifier = Modifier
            .clickable {
                TopCardViewModel.toggleCardExpansion()
            }
            .cardWithFullWidth(),
            elevation = 8.dp
        ) {
            AnimatedVisibility(visible = !TopCardViewModel.expanded.value) {
                LoginStatus(itemsToList = TopCardViewModel.setupInfo.value)
                MoreLessText(textToShow = TopCardViewModel.getMoreLessText())
            }
            AnimatedVisibility(visible = TopCardViewModel.expanded.value) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(start = 4.dp)
                ) {
                    SettingsStatus(context, TopCardViewModel.setupInfo.value)
                    Row(horizontalArrangement = Arrangement.End) {
                        MoreLessText(textToShow = TopCardViewModel.getMoreLessText())
                    }
                }
            }
        }
    }

    @Composable
    private fun LoginStatus(itemsToList: MutableMap<String, String>) {
        Column(
            modifier = Modifier.columnWithMaxWidth()
        ) {
            Row { Text(text = "Logged in: " + itemsToList["LoggedIn"].toString()) }
        }
    }

    @Composable
    private fun MoreLessText(textToShow: String) {
        Text(
            text = textToShow,
            modifier = Modifier
                .fillMaxWidth(1f),
            textAlign = TextAlign.End
        )
    }

    @Composable
    private fun SettingsStatus(context: Context, itemsToList: MutableMap<String, String>) {
        itemsToList.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "${item.key}: ")
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.caption,
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
}
