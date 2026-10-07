package com.emarsys.mobileengage.testUtil

import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.testUtil.RandomTestUtils.randomLong
import com.emarsys.testUtil.RandomTestUtils.randomNumberString

object RandomMETestUtils {
    fun randomDisplayedIam(): DisplayedIam = DisplayedIam(randomNumberString(), randomLong())
    fun randomButtonClick(): ButtonClicked = ButtonClicked(randomNumberString(), randomNumberString(), randomLong())
}
