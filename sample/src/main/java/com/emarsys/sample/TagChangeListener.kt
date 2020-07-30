package com.emarsys.sample

interface TagChangeListener {

    fun addTagClicked(messageId: String)

    fun removeTagClicked(messageId: String)
}