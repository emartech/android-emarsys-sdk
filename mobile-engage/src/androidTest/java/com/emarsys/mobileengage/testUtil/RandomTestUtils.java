package com.emarsys.mobileengage.testUtil;

import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RandomTestUtils {

    public static boolean randomBool() {
        return new Random().nextBoolean();
    }

    public static int randomInt() {
        return new Random().nextInt();
    }

    public static long randomLong() {
        return new Random().nextLong();
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static String randomNumberString() {
        return Long.toString(randomLong());
    }

    public static Object randomMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(randomString(), randomInt());
        map.put(randomString(), randomBool());
        map.put(randomString(), randomString());
        return map;
    }

    public static DisplayedIam randomDisplayedIam() {
        return new DisplayedIam(randomNumberString(), randomLong());
    }

    public static ButtonClicked randomButtonClick() {
        return new ButtonClicked(randomNumberString(), randomNumberString(), randomLong());
    }
}
