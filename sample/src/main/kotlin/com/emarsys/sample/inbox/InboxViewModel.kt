package com.emarsys.sample.inbox

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.emarsys.Emarsys
import com.emarsys.inbox.InboxTag
import com.emarsys.mobileengage.api.inbox.Message

class InboxViewModel : ViewModel() {
    val refreshing = mutableStateOf(false)
    val fetchedMessages = mutableSetOf<Message>()

    fun isFetchedMessagesEmpty(): Boolean {
        return this.fetchedMessages.isEmpty()
    }

    private fun addMessageToFetched(message: Message) {
        this.fetchedMessages.add(message)
    }

    fun onSwipeFetchMessages() {
        refreshing.value = true
        Emarsys.messageInbox.fetchMessages {
            if (it.errorCause != null) {
                Log.e("INBOX", "Inbox Error" + it.errorCause)
            } else {
                it.result?.let { inboxResult ->
                    inboxResult.messages.forEach { message ->
                        Log.i("INBOX", message.title)
                        if (message.tags.isNullOrEmpty()) {
                            Emarsys.messageInbox.addTag(
                                InboxTag.SEEN,
                                messageId = message.id
                            )
                        }
                        addMessageToFetched(message)
                    }
                }
                refreshing.value = false
            }
        }
    }
}