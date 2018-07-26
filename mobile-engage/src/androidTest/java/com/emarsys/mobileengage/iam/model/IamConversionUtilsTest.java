package com.emarsys.mobileengage.iam.model;

import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class IamConversionUtilsTest {

    private ButtonClicked buttonClicked1;
    private ButtonClicked buttonClicked2;
    private ButtonClicked buttonClicked3;

    private DisplayedIam displayedIam1;
    private DisplayedIam displayedIam2;
    private DisplayedIam displayedIam3;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        buttonClicked1 = new ButtonClicked("campaign1", "button1", 200);
        buttonClicked2 = new ButtonClicked("campaign1", "button2", 400);
        buttonClicked3 = new ButtonClicked("campaign2", "button1", 2000);

        displayedIam1 = new DisplayedIam("campaign10", 500);
        displayedIam2 = new DisplayedIam("campaign20", 1000);
        displayedIam3 = new DisplayedIam("campaign30", 1500);
    }

    @Test
    public void testConvert_buttonClick() throws Exception {
        Map<String, Object> json = IamConversionUtils.buttonClickToJson(buttonClicked1);

        Map<String, Object> expected = new HashMap<>();
        expected.put("message_id", buttonClicked1.getCampaignId());
        expected.put("button_id", buttonClicked1.getButtonId());
        expected.put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked1.getTimestamp()));

        assertEquals(expected, json);
    }

    @Test
    public void testConvert_buttonClickList() throws Exception {
        List<Map<String, Object>> result = IamConversionUtils.buttonClicksToArray(Arrays.asList(
                buttonClicked1,
                buttonClicked2,
                buttonClicked3
        ));

        Map<String, Object> click1 = new HashMap<>();
        click1.put("message_id", buttonClicked1.getCampaignId());
        click1.put("button_id", buttonClicked1.getButtonId());
        click1.put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked1.getTimestamp()));

        Map<String, Object> click2 = new HashMap<>();
        click2.put("message_id", buttonClicked2.getCampaignId());
        click2.put("button_id", buttonClicked2.getButtonId());
        click2.put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked2.getTimestamp()));

        Map<String, Object> click3 = new HashMap<>();
        click3.put("message_id", buttonClicked3.getCampaignId());
        click3.put("button_id", buttonClicked3.getButtonId());
        click3.put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked3.getTimestamp()));

        List<Map<String, Object>> expected = Arrays.asList(click1, click2, click3);
        assertEquals(expected, result);
    }

    @Test
    public void testConvert_displayedIam() throws Exception {
        Map<String, Object> json = IamConversionUtils.displayedIamToJson(displayedIam1);

        Map<String, Object> expected = new HashMap<>();
        expected.put("message_id", displayedIam1.getCampaignId());
        expected.put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam1.getTimestamp()));

        assertEquals(expected, json);
    }

    @Test
    public void testConvert_displayedIamList() throws Exception {
        List<Map<String, Object>> result = IamConversionUtils.displayedIamsToArray(Arrays.asList(
                displayedIam1,
                displayedIam2,
                displayedIam3
        ));

        Map<String, Object> iam1 = new HashMap<>();
        iam1.put("message_id", displayedIam1.getCampaignId());
        iam1.put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam1.getTimestamp()));

        Map<String, Object> iam2 = new HashMap<>();
        iam2.put("message_id", displayedIam2.getCampaignId());
        iam2.put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam2.getTimestamp()));

        Map<String, Object> iam3 = new HashMap<>();
        iam3.put("message_id", displayedIam3.getCampaignId());
        iam3.put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam3.getTimestamp()));

        List<Map<String, Object>> expected = Arrays.asList(iam1, iam2, iam3);

        assertEquals(expected, result);
    }
}