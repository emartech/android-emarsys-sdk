package com.emarsys.mobileengage.iam.model;

import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IamConversionUtils {

    public static List<Map<String, Object>> buttonClicksToArray(List<ButtonClicked> buttonClicks) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ButtonClicked buttonClick : buttonClicks) {
            result.add(buttonClickToJson(buttonClick));
        }
        return result;
    }

    public static Map<String, Object> buttonClickToJson(ButtonClicked buttonClicked) {
        Map<String, Object> result = new HashMap<>();
            result.put("message_id", buttonClicked.getCampaignId());
            result.put("button_id", buttonClicked.getButtonId());
            result.put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked.getTimestamp()));
        return result;
    }

    public static List<Map<String, Object>> displayedIamsToArray(List<DisplayedIam> displayedIams) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (DisplayedIam displayedIam : displayedIams) {
            result.add(displayedIamToJson(displayedIam));
        }
        return result;
    }

    public static Map<String, Object> displayedIamToJson(DisplayedIam displayedIam) {
        Map<String, Object> result = new HashMap<>();
            result.put("message_id", displayedIam.getCampaignId());
            result.put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam.getTimestamp()));
        return result;
    }

}
