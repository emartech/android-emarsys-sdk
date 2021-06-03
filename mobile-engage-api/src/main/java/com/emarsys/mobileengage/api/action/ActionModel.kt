package com.emarsys.mobileengage.api.action

import java.net.URL

sealed class ActionModel {
    abstract val id: String
    abstract val title: String
    abstract val type: String
}

data class AppEventActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, Any>?
): ActionModel()

data class CustomEventActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, Any>?
): ActionModel()

data class DismissActionModel(
    override val id: String,
    override val title: String,
    override val type: String
): ActionModel()

data class OpenExternalUrlActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val url: URL
): ActionModel()