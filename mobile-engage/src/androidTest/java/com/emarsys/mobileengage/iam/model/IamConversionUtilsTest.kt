package com.emarsys.mobileengage.iam.model

import com.emarsys.core.util.TimestampUtils
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.util.Arrays

class IamConversionUtilsTest : AnnotationSpec() {
    private var buttonClicked1: ButtonClicked? = null
    private var buttonClicked2: ButtonClicked? = null
    private var buttonClicked3: ButtonClicked? = null
    private var displayedIam1: DisplayedIam? = null
    private var displayedIam2: DisplayedIam? = null
    private var displayedIam3: DisplayedIam? = null

    @Before
    fun init() {
        buttonClicked1 = ButtonClicked("campaign1", "button1", 200)
        buttonClicked2 = ButtonClicked("campaign1", "button2", 400)
        buttonClicked3 = ButtonClicked("campaign2", "button1", 2000)
        displayedIam1 = DisplayedIam("campaign10", 500)
        displayedIam2 = DisplayedIam("campaign20", 1000)
        displayedIam3 = DisplayedIam("campaign30", 1500)
    }

    @Test
    fun testConvert_buttonClick() {
        val json = IamConversionUtils.buttonClickToJson(buttonClicked1)
        val expected: MutableMap<String, Any> = HashMap()
        expected["campaignId"] = buttonClicked1!!.campaignId
        expected["buttonId"] = buttonClicked1!!.buttonId
        expected["timestamp"] = TimestampUtils.formatTimestampWithUTC(buttonClicked1!!.timestamp)
        json shouldBe expected
    }

    @Test
    fun testConvert_buttonClickList() {
        val result = IamConversionUtils.buttonClicksToArray(
            Arrays.asList(
                buttonClicked1,
                buttonClicked2,
                buttonClicked3
            )
        )
        val click1: MutableMap<String, Any> = HashMap()
        click1["campaignId"] = buttonClicked1!!.campaignId
        click1["buttonId"] = buttonClicked1!!.buttonId
        click1["timestamp"] = TimestampUtils.formatTimestampWithUTC(buttonClicked1!!.timestamp)
        val click2: MutableMap<String, Any> = HashMap()
        click2["campaignId"] = buttonClicked2!!.campaignId
        click2["buttonId"] = buttonClicked2!!.buttonId
        click2["timestamp"] = TimestampUtils.formatTimestampWithUTC(buttonClicked2!!.timestamp)
        val click3: MutableMap<String, Any> = HashMap()
        click3["campaignId"] = buttonClicked3!!.campaignId
        click3["buttonId"] = buttonClicked3!!.buttonId
        click3["timestamp"] = TimestampUtils.formatTimestampWithUTC(buttonClicked3!!.timestamp)
        val expected = Arrays.asList<Map<String, Any>>(click1, click2, click3)
        result shouldBe expected
    }

    @Test
    fun testConvert_displayedIam() {
        val json = IamConversionUtils.displayedIamToJson(displayedIam1)
        val expected: MutableMap<String, Any> = HashMap()
        expected["campaignId"] = displayedIam1!!.campaignId
        expected["timestamp"] = TimestampUtils.formatTimestampWithUTC(displayedIam1!!.timestamp)
        json shouldBe expected
    }

    @Test
    fun testConvert_displayedIamList() {
        val result = IamConversionUtils.displayedIamsToArray(
            Arrays.asList(
                displayedIam1,
                displayedIam2,
                displayedIam3
            )
        )
        val iam1: MutableMap<String, Any> = HashMap()
        iam1["campaignId"] = displayedIam1!!.campaignId
        iam1["timestamp"] = TimestampUtils.formatTimestampWithUTC(displayedIam1!!.timestamp)
        val iam2: MutableMap<String, Any> = HashMap()
        iam2["campaignId"] = displayedIam2!!.campaignId
        iam2["timestamp"] = TimestampUtils.formatTimestampWithUTC(displayedIam2!!.timestamp)
        val iam3: MutableMap<String, Any> = HashMap()
        iam3["campaignId"] = displayedIam3!!.campaignId
        iam3["timestamp"] = TimestampUtils.formatTimestampWithUTC(displayedIam3!!.timestamp)
        val expected = Arrays.asList<Map<String, Any>>(iam1, iam2, iam3)

        result shouldBe expected
    }
}