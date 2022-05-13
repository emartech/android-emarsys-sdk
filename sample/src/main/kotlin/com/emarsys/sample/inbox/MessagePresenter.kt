package com.emarsys.sample.inbox

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.emarsys.Emarsys
import com.emarsys.mobileengage.api.inbox.Message
import com.emarsys.sample.R
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.divider.DividerWithBackgroundColor
import com.emarsys.sample.ui.component.row.RowWithCenteredContent
import com.emarsys.sample.ui.component.row.RowWithEvenlySpacedContent
import com.emarsys.sample.ui.component.text.TextWithFullWidthLine
import com.emarsys.sample.ui.component.text.TitleText
import com.emarsys.sample.ui.component.textfield.StyledTextField
import com.emarsys.sample.ui.component.toast.customTextToast
import com.emarsys.sample.ui.style.cardWithFullWidth
import com.emarsys.sample.ui.style.columnWithMaxWidth
import com.emarsys.sample.ui.style.rowWithMaxWidth

class MessagePresenter(private val context: Context) {

    private companion object {
        const val DEFAULT_ELEVATION = 10
        const val ON_CLICK_ELEVATION = 50
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
    @ExperimentalCoilApi
    @Composable
    fun MessageCard(message: Message) {
        val expanded = remember { mutableStateOf(false) }
        val elevation = remember { mutableStateOf(DEFAULT_ELEVATION) }
        val tagToRemoveOrAdd = remember { mutableStateOf("") }
        var messageTags = remember { mutableStateListOf<String>() }
        if (messageTags.isEmpty()) {
            messageTags = getMessageTags(message, messageTags)
        }

        Card(
            elevation = elevation.value.dp,
            modifier = Modifier
                .cardWithFullWidth()
                .clickable {
                    elevation.value =
                        if (elevation.value == DEFAULT_ELEVATION) ON_CLICK_ELEVATION else DEFAULT_ELEVATION
                    expanded.value = !expanded.value
                }
        ) {
            AnimatedVisibility(visible = !expanded.value) {
                UnexpandedMessageContent(message = message, messageTags)
            }
            AnimatedVisibility(visible = expanded.value) {
                ExpandedMessageContent(
                    message = message,
                    tagToRemoveOrAdd = tagToRemoveOrAdd,
                    messageTags = messageTags
                )
            }
        }
    }

    @Composable
    private fun UnexpandedMessageContent(message: Message, messageTags: SnapshotStateList<String>) {
        Row(modifier = Modifier.rowWithMaxWidth()) {
            Column(
                modifier = Modifier
                    .columnWithMaxWidth()
                    .weight(0.5f)
            ) {
                message.imageUrl?.let { ShowImage(imageUrl = it) }
            }
            Column(
                modifier = Modifier
                    .columnWithMaxWidth()
                    .weight(1f)
            ) {
                TextWithFullWidthLine(text = "${stringResource(id = R.string.title)}: ${message.title}")
                CommonMessageDetails(message = message)
                if (!messageTags.isNullOrEmpty()) {
                    TextFromTags(tags = messageTags)
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun ExpandedMessageContent(
        message: Message,
        tagToRemoveOrAdd: MutableState<String>,
        messageTags: SnapshotStateList<String>,
    ) {
        val somethingWentWrong = stringResource(id = R.string.something_went_wrong)
        val tagAdded = stringResource(id = R.string.tag_added)
        val tagRemoved = stringResource(id = R.string.tag_removed)
        Column(Modifier.columnWithMaxWidth()) {
            Row(
                modifier = Modifier.rowWithMaxWidth()
            ) {
                message.imageUrl?.let { ShowImage(imageUrl = it) }
            }
            RowWithCenteredContent { TitleText(titleText = "${stringResource(id = R.string.title)}: ${message.title}") }
            CommonMessageDetails(message)
            if (!messageTags.isNullOrEmpty()) {
                TextFromTags(tags = messageTags)
            }
            RowWithCenteredContent(
                content = {
                    StyledTextField(
                        fieldToEdit = tagToRemoveOrAdd,
                        label = stringResource(id = R.string.tag)
                    )
                }
            )
            DividerWithBackgroundColor()
            RowWithEvenlySpacedContent(
                content = {
                    StyledTextButton(buttonText = stringResource(id = R.string.add_tag)) {
                        if (tagToRemoveOrAdd.value.isNotEmpty()) {
                            onAddTagClicked(
                                tagToRemoveOrAdd.value,
                                message.id,
                                messageTags,
                                somethingWentWrong,
                                tagAdded
                            )
                        }
                    }
                    StyledTextButton(buttonText = stringResource(id = R.string.remove_tag)) {
                        if (tagToRemoveOrAdd.value.isNotEmpty()) {
                            onRemoveTagClicked(
                                tagToRemoveOrAdd.value,
                                message.id,
                                messageTags,
                                somethingWentWrong,
                                tagRemoved
                            )
                        }
                    }
                }
            )
        }
    }

    @Composable
    private fun CommonMessageDetails(message: Message) {
        TextWithFullWidthLine(text = "ID: ${message.id}")
        TextWithFullWidthLine(text = "${stringResource(id = R.string.campaign_id)}: ${message.campaignId}")
        message.actions?.let { TextFromActionList(actionModels = it) }
    }

    private fun onAddTagClicked(
        tag: String,
        messageId: String,
        messageTags: SnapshotStateList<String>,
        somethingWentWrong: String,
        tagAdded: String
    ) {
        Emarsys.messageInbox.addTag(tag, messageId) {
            if (it == null) {
                addTagToMessageTags(tag, messageTags)
                customTextToast(context, tagAdded)
            } else {
                customTextToast(context, somethingWentWrong)
            }
        }
    }

    private fun onRemoveTagClicked(
        tag: String,
        messageId: String,
        messageTags: SnapshotStateList<String>,
        somethingWentWrong: String,
        tagRemoved: String
    ) {
        Emarsys.messageInbox.removeTag(tag, messageId) {
            if (it == null) {
                messageTags.remove(tag)
                customTextToast(context, tagRemoved)
            } else {
                customTextToast(context, somethingWentWrong)
            }
        }
    }

    private fun getMessageTags(
        message: Message,
        messageTags: SnapshotStateList<String>
    ): SnapshotStateList<String> {
        if (!message.tags.isNullOrEmpty()) {
            message.tags!!.forEach { tag ->
                if (!messageTags.contains(tag)) {
                    messageTags.add(tag)
                }
            }
        }
        return messageTags
    }

    private fun addTagToMessageTags(tag: String, messageTags: SnapshotStateList<String>) {
        if (!messageTags.contains(tag)) {
            messageTags.add(tag)
        }
    }
}