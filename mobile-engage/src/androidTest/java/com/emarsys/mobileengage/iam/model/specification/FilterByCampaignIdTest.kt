package com.emarsys.mobileengage.iam.model.specification

import com.emarsys.core.database.helper.CoreDbHelper
import com.emarsys.core.database.repository.specification.Everything
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.*

class FilterByCampaignIdTest {

    @Rule
    @JvmField
    var timeout: TestRule = TimeoutUtils.timeoutRule
    
    private lateinit var displayedIamRepository: DisplayedIamRepository
    private lateinit var buttonClickedRepository: ButtonClickedRepository

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()

        val context = InstrumentationRegistry.getTargetContext()
        val dbHelper = CoreDbHelper(context, HashMap())

        displayedIamRepository = DisplayedIamRepository(dbHelper)
        buttonClickedRepository = ButtonClickedRepository(dbHelper)
    }

    @Test
    fun testQuery_displayedIam() {
        val iam1 = DisplayedIam("campaign1", 10L)
        val iam2 = DisplayedIam("campaign2", 20L)
        val iam3 = DisplayedIam("campaign3", 30L)

        displayedIamRepository.add(iam1)
        displayedIamRepository.add(iam2)
        displayedIamRepository.add(iam3)

        val result = displayedIamRepository.query(FilterByCampaignId("campaign2", "campaign3"))
        val expected = listOf(iam2, iam3)

        result shouldBe expected
    }

    @Test
    fun testDelete_displayedIam_shouldDeleteIam() {
        val iam1 = DisplayedIam("campaign1", 10L)
        val iam2 = DisplayedIam("campaign2", 20L)
        val iam3 = DisplayedIam("campaign3", 30L)

        displayedIamRepository.add(iam1)
        displayedIamRepository.add(iam2)
        displayedIamRepository.add(iam3)

        displayedIamRepository.remove(FilterByCampaignId("campaign2"))

        val result = displayedIamRepository.query(Everything())
        val expected = listOf(iam1, iam3)

        result shouldBe expected
    }

    @Test
    fun testDelete_displayedIam_shouldDelete_multipleIams() {
        val iam1 = DisplayedIam("campaign1", 10L)
        val iam2 = DisplayedIam("campaign2", 20L)
        val iam3 = DisplayedIam("campaign3", 30L)
        val iam4 = DisplayedIam("campaign4", 40L)

        displayedIamRepository.add(iam1)
        displayedIamRepository.add(iam2)
        displayedIamRepository.add(iam3)
        displayedIamRepository.add(iam4)

        displayedIamRepository.remove(FilterByCampaignId("campaign1", "campaign2"))

        val result = displayedIamRepository.query(Everything())
        val expected = listOf(iam3, iam4)

        result shouldBe expected
    }

    @Test
    fun testDelete_displayedIam_withEmptyIdArray() {
        val iam1 = DisplayedIam("campaign1", 10L)
        val iam2 = DisplayedIam("campaign2", 20L)
        val iam3 = DisplayedIam("campaign3", 30L)
        val iam4 = DisplayedIam("campaign4", 40L)

        displayedIamRepository.add(iam1)
        displayedIamRepository.add(iam2)
        displayedIamRepository.add(iam3)
        displayedIamRepository.add(iam4)

        displayedIamRepository.remove(FilterByCampaignId())

        val result = displayedIamRepository.query(Everything())
        val expected = listOf(iam1, iam2, iam3, iam4)

        result shouldBe expected
    }

    @Test
    fun testQuery_buttonClicked() {
        val btn1 = ButtonClicked("campaign1", "button1", 10L)
        val btn2 = ButtonClicked("campaign1", "button3", 10L)
        val btn3 = ButtonClicked("campaign2", "button10", 10L)

        buttonClickedRepository.add(btn1)
        buttonClickedRepository.add(btn2)
        buttonClickedRepository.add(btn3)

        val result = buttonClickedRepository.query(FilterByCampaignId("campaign1"))
        val expected = listOf(btn1, btn2)

        result shouldBe expected
    }

    @Test
    fun testDelete_buttonClicked_shouldDeleteIam() {
        val btn1 = ButtonClicked("campaign1", "button1", 10L)
        val btn2 = ButtonClicked("campaign1", "button3", 10L)
        val btn3 = ButtonClicked("campaign2", "button10", 10L)

        buttonClickedRepository.add(btn1)
        buttonClickedRepository.add(btn2)
        buttonClickedRepository.add(btn3)

        buttonClickedRepository.remove(FilterByCampaignId("campaign2"))

        val result = buttonClickedRepository.query(Everything())
        val expected = listOf(btn1, btn2)

        result shouldBe expected
    }

    @Test
    fun testDelete_buttonClicked_shouldDelete_multipleIams() {
        val btn1 = ButtonClicked("campaign1", "button1", 10L)
        val btn2 = ButtonClicked("campaign1", "button3", 10L)
        val btn3 = ButtonClicked("campaign2", "button10", 10L)
        val btn4 = ButtonClicked("campaign3", "button10", 10L)

        buttonClickedRepository.add(btn1)
        buttonClickedRepository.add(btn2)
        buttonClickedRepository.add(btn3)
        buttonClickedRepository.add(btn4)

        buttonClickedRepository.remove(FilterByCampaignId("campaign1", "campaign2"))

        val result = buttonClickedRepository.query(Everything())
        val expected = listOf(btn4)

        result shouldBe expected
    }

    @Test
    fun testDelete_buttonClicked_withEmptyIdArray() {
        val btn1 = ButtonClicked("campaign1", "button1", 10L)
        val btn2 = ButtonClicked("campaign1", "button3", 10L)
        val btn3 = ButtonClicked("campaign2", "button10", 10L)

        buttonClickedRepository.add(btn1)
        buttonClickedRepository.add(btn2)
        buttonClickedRepository.add(btn3)

        buttonClickedRepository.remove(FilterByCampaignId())

        val result = buttonClickedRepository.query(Everything())
        val expected = listOf(btn1, btn2, btn3)

        result shouldBe expected
    }

}