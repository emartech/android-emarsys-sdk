package com.emarsys.sample.inbox

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.emarsys.mobileengage.api.inbox.Message

class InboxViewModel: ViewModel() {

    val fetchedMessages = mutableStateListOf<Message>()

    fun isFetchedMessagesEmpty(): Boolean {
        return this.fetchedMessages.isEmpty()
    }

    fun emptyFetchedMessages() {
        this.fetchedMessages.clear()
    }

    fun addMessageToFetched(message: Message) {
        this.fetchedMessages.add(message)
    }
}