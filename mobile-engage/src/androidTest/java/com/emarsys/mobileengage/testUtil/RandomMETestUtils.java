package com.emarsys.mobileengage.testUtil;

import static com.emarsys.testUtil.RandomTestUtils.randomLong;
import static com.emarsys.testUtil.RandomTestUtils.randomNumberString;

import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;

public class RandomMETestUtils {

    public static DisplayedIam randomDisplayedIam() {
        return new DisplayedIam(randomNumberString(), randomLong());
    }

    public static ButtonClicked randomButtonClick() {
        return new ButtonClicked(randomNumberString(), randomNumberString(), randomLong());
    }
}
