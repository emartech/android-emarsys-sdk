package com.emarsys.mobileengage.iam.model.specification;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.helper.DbHelper;
import com.emarsys.core.database.repository.specification.QueryAll;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FilterByCampaignIdTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();
    private DisplayedIamRepository displayedIamRepository;
    private ButtonClickedRepository buttonClickedRepository;

    @Before
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();

        Context context = InstrumentationRegistry.getContext();
        DbHelper dbHelper = new CoreDbHelper(context, new HashMap<TriggerKey, List<Runnable>>());

        displayedIamRepository = new DisplayedIamRepository(dbHelper);
        buttonClickedRepository = new ButtonClickedRepository(dbHelper);
    }

    @Test
    public void testExecution_displayedIam_shouldDeleteIam() {
        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L);
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L);
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L);

        displayedIamRepository.add(iam1);
        displayedIamRepository.add(iam2);
        displayedIamRepository.add(iam3);

        displayedIamRepository.remove(new FilterByCampaignId("campaign2"));

        List<DisplayedIam> result = displayedIamRepository.query(new QueryAll(DatabaseContract.DISPLAYED_IAM_TABLE_NAME));
        List<DisplayedIam> expected = Arrays.asList(iam1, iam3);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_displayedIam_shouldDelete_multipleIams() {
        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L);
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L);
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L);
        DisplayedIam iam4 = new DisplayedIam("campaign4", 40L);

        displayedIamRepository.add(iam1);
        displayedIamRepository.add(iam2);
        displayedIamRepository.add(iam3);
        displayedIamRepository.add(iam4);

        displayedIamRepository.remove(new FilterByCampaignId("campaign1", "campaign2"));

        List<DisplayedIam> result = displayedIamRepository.query(new QueryAll(DatabaseContract.DISPLAYED_IAM_TABLE_NAME));
        List<DisplayedIam> expected = Arrays.asList(iam3, iam4);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_displayedIam_withEmptyIdArray() {
        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L);
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L);
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L);
        DisplayedIam iam4 = new DisplayedIam("campaign4", 40L);

        displayedIamRepository.add(iam1);
        displayedIamRepository.add(iam2);
        displayedIamRepository.add(iam3);
        displayedIamRepository.add(iam4);

        displayedIamRepository.remove(new FilterByCampaignId());

        List<DisplayedIam> result = displayedIamRepository.query(new QueryAll(DatabaseContract.DISPLAYED_IAM_TABLE_NAME));
        List<DisplayedIam> expected = Arrays.asList(iam1, iam2, iam3, iam4);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_buttonClicked_shouldDeleteIam() {
        ButtonClicked btn1 = new ButtonClicked("campaign1", "button1", 10L);
        ButtonClicked btn2 = new ButtonClicked("campaign1", "button3", 10L);
        ButtonClicked btn3 = new ButtonClicked("campaign2", "button10", 10L);

        buttonClickedRepository.add(btn1);
        buttonClickedRepository.add(btn2);
        buttonClickedRepository.add(btn3);

        buttonClickedRepository.remove(new FilterByCampaignId("campaign2"));

        List<ButtonClicked> result = buttonClickedRepository.query(new QueryAll(DatabaseContract.BUTTON_CLICKED_TABLE_NAME));
        List<ButtonClicked> expected = Arrays.asList(btn1, btn2);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_buttonClicked_shouldDelete_multipleIams() {
        ButtonClicked btn1 = new ButtonClicked("campaign1", "button1", 10L);
        ButtonClicked btn2 = new ButtonClicked("campaign1", "button3", 10L);
        ButtonClicked btn3 = new ButtonClicked("campaign2", "button10", 10L);
        ButtonClicked btn4 = new ButtonClicked("campaign3", "button10", 10L);

        buttonClickedRepository.add(btn1);
        buttonClickedRepository.add(btn2);
        buttonClickedRepository.add(btn3);
        buttonClickedRepository.add(btn4);

        buttonClickedRepository.remove(new FilterByCampaignId("campaign1", "campaign2"));

        List<ButtonClicked> result = buttonClickedRepository.query(new QueryAll(DatabaseContract.BUTTON_CLICKED_TABLE_NAME));
        List<ButtonClicked> expected = Collections.singletonList(btn4);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_buttonClicked_withEmptyIdArray() {
        ButtonClicked btn1 = new ButtonClicked("campaign1", "button1", 10L);
        ButtonClicked btn2 = new ButtonClicked("campaign1", "button3", 10L);
        ButtonClicked btn3 = new ButtonClicked("campaign2", "button10", 10L);

        buttonClickedRepository.add(btn1);
        buttonClickedRepository.add(btn2);
        buttonClickedRepository.add(btn3);

        buttonClickedRepository.remove(new FilterByCampaignId());

        List<ButtonClicked> result = buttonClickedRepository.query(new QueryAll(DatabaseContract.BUTTON_CLICKED_TABLE_NAME));
        List<ButtonClicked> expected = Arrays.asList(btn1, btn2, btn3);

        assertEquals(expected, result);
    }

}