package com.emarsys.mobileengage.util

import com.emarsys.core.util.AndroidVersionUtils
import com.emarsys.core.util.TimestampUtils
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.iam.model.IamConversionUtils
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import java.util.Locale

object RequestPayloadUtils {

    @JvmStatic
    fun createSetPushTokenPayload(pushToken: String): Map<String, Any> {
        return mutableMapOf<String, Any>("pushToken" to pushToken)
    }

    @JvmStatic
    fun createTrackDeviceInfoPayload(requestContext: MobileEngageRequestContext): Map<String, Any?> {
        val deviceInfo = requestContext.deviceInfo
        val payload: MutableMap<String, Any?> = mutableMapOf(
                "platform" to deviceInfo.platform,
                "applicationVersion" to deviceInfo.applicationVersion,
                "deviceModel" to deviceInfo.model,
                "osVersion" to deviceInfo.osVersion,
                "sdkVersion" to deviceInfo.sdkVersion,
                "language" to deviceInfo.language,
                "timezone" to deviceInfo.timezone)
        val notificationSettings = deviceInfo.notificationSettings
        val notificationSettingsMap: MutableMap<String, Any> = mutableMapOf(
                "areNotificationsEnabled" to notificationSettings.areNotificationsEnabled,
                "importance" to notificationSettings.importance
        )
        val channelSettings: MutableList<Map<String, Any>> = mutableListOf()
        if (AndroidVersionUtils.isOreoOrAbove) {
            for ((channelId, importance, isCanBypassDnd, isCanShowBadge, isShouldVibrate, isShouldShowLights) in notificationSettings.channelSettings) {
                val channelSettingMap: Map<String, Any> = mapOf(
                    "channelId" to channelId,
                    "importance" to importance,
                    "canShowBadge" to isCanShowBadge,
                    "canBypassDnd" to isCanBypassDnd,
                    "shouldVibrate" to isShouldVibrate,
                    "shouldShowLights" to isShouldShowLights
                )
                channelSettings.add(channelSettingMap)
            }
            notificationSettingsMap["channelSettings"] = channelSettings
        }
        payload["pushSettings"] = notificationSettingsMap
        return payload
    }

    @JvmStatic
    fun createCustomEventPayload(eventName: String, eventAttributes: Map<String, String>?, requestContext: MobileEngageRequestContext): Map<String, Any> {
        return createEventPayload(EventType.CUSTOM, eventName, eventAttributes, requestContext)
    }

    @JvmStatic
    fun createInternalCustomEventPayload(eventName: String, eventAttributes: Map<String, String>?, requestContext: MobileEngageRequestContext): Map<String, Any> {
        return createEventPayload(EventType.INTERNAL, eventName, eventAttributes, requestContext)
    }

    private fun createEventPayload(eventType: EventType, eventName: String, eventAttributes: Map<String, String>?, requestContext: MobileEngageRequestContext): Map<String, Any> {
        val event = createEvent(eventType, eventName, eventAttributes, requestContext)
        return mapOf(
                "clicks" to emptyList(),
                "viewedMessages" to emptyList(),
                "events" to listOf(event)
        )
    }

    private fun createEvent(eventType: EventType, eventName: String, eventAttributes: Map<String, String>?, requestContext: MobileEngageRequestContext): Map<String, Any> {
        val event: MutableMap<String, Any> = mutableMapOf(
                "type" to eventType.eventType(),
                "name" to eventName,
                "timestamp" to TimestampUtils.formatTimestampWithUTC(requestContext.timestampProvider.provideTimestamp())
        )
        if (!eventAttributes.isNullOrEmpty()) {
            event["attributes"] = eventAttributes
        }
        requestContext.sessionIdHolder.sessionId?.let {
            event["sessionId"] = it
        }
        return event
    }

    @JvmStatic
    fun createCompositeRequestModelPayload(
            events: List<Any>,
            displayedIams: List<DisplayedIam>,
            buttonClicks: List<ButtonClicked>,
            doNotDisturb: Boolean): Map<String, Any> {
        val compositePayload: MutableMap<String, Any> = mutableMapOf(
                "viewedMessages" to IamConversionUtils.displayedIamsToArray(displayedIams),
                "clicks" to IamConversionUtils.buttonClicksToArray(buttonClicks)
        )

        if (doNotDisturb) {
            compositePayload["dnd"] = true
        }
        compositePayload["events"] = events
        return compositePayload
    }

    @JvmStatic
    fun createRefreshContactTokenPayload(requestContext: MobileEngageRequestContext): Map<String, Any?> {
        return mutableMapOf<String, Any?>(
                "refreshToken" to requestContext.refreshTokenStorage.get()
        )
    }

    @JvmStatic
    fun createInlineInAppPayload(viewId: String, clicks: List<ButtonClicked>): Map<String, Any?> {
        return mapOf("viewIds" to listOf(viewId), "clicks" to IamConversionUtils.buttonClicksToArray(clicks))
    }
}

internal enum class EventType {
    CUSTOM, INTERNAL
}

internal fun EventType.eventType(): String {
    return this.name.lowercase(Locale.forLanguageTag("en_US"))
}