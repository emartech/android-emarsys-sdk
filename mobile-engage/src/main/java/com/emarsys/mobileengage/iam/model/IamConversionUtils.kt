package com.emarsys.mobileengage.iam.model

import com.emarsys.core.util.TimestampUtils
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam

object IamConversionUtils {

    fun buttonClicksToArray(buttonClicks: List<ButtonClicked>): List<Map<String, Any>> =
        buttonClicks.map { buttonClickToJson(it) }

    fun buttonClickToJson(buttonClicked: ButtonClicked): Map<String, Any> = mapOf(
        "campaignId" to buttonClicked.campaignId,
        "buttonId" to buttonClicked.buttonId,
        "timestamp" to TimestampUtils.formatTimestampWithUTC(buttonClicked.timestamp)
    )

    fun displayedIamsToArray(displayedIams: List<DisplayedIam>): List<Map<String, Any>> =
        displayedIams.map { displayedIamToJson(it) }

    fun displayedIamToJson(displayedIam: DisplayedIam): Map<String, Any> = mapOf(
        "campaignId" to displayedIam.campaignId,
        "timestamp" to TimestampUtils.formatTimestampWithUTC(displayedIam.timestamp)
    )
}
