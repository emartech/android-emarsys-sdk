package com.emarsys.mobileengage.iam.model.specification;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.repository.specification.QueryAll;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedContract;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamContract;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FilterByCampaignIdTest {

    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.deleteMobileEngageDatabase();

        context = InstrumentationRegistry.getContext();
    }

    @Test
    public void testExecution_displayedIam_shouldDeleteIam() {
        DisplayedIamRepository repository = new DisplayedIamRepository(context);

        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L);
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L);
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L);

        repository.add(iam1);
        repository.add(iam2);
        repository.add(iam3);

        repository.remove(new FilterByCampaignId("campaign2"));

        List<DisplayedIam> result = repository.query(new QueryAll(DisplayedIamContract.TABLE_NAME));
        List<DisplayedIam> expected = Arrays.asList(iam1, iam3);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_displayedIam_shouldDelete_multipleIams() {
        DisplayedIamRepository repository = new DisplayedIamRepository(context);

        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L);
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L);
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L);
        DisplayedIam iam4 = new DisplayedIam("campaign4", 40L);

        repository.add(iam1);
        repository.add(iam2);
        repository.add(iam3);
        repository.add(iam4);

        repository.remove(new FilterByCampaignId("campaign1", "campaign2"));

        List<DisplayedIam> result = repository.query(new QueryAll(DisplayedIamContract.TABLE_NAME));
        List<DisplayedIam> expected = Arrays.asList(iam3, iam4);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_displayedIam_withEmptyIdArray() {
        DisplayedIamRepository repository = new DisplayedIamRepository(context);

        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L);
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L);
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L);
        DisplayedIam iam4 = new DisplayedIam("campaign4", 40L);

        repository.add(iam1);
        repository.add(iam2);
        repository.add(iam3);
        repository.add(iam4);

        repository.remove(new FilterByCampaignId());

        List<DisplayedIam> result = repository.query(new QueryAll(DisplayedIamContract.TABLE_NAME));
        List<DisplayedIam> expected = Arrays.asList(iam1, iam2, iam3, iam4);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_buttonClicked_shouldDeleteIam() {
        ButtonClickedRepository repository = new ButtonClickedRepository(context);

        ButtonClicked btn1 = new ButtonClicked("campaign1", "button1", 10L);
        ButtonClicked btn2 = new ButtonClicked("campaign1", "button3", 10L);
        ButtonClicked btn3 = new ButtonClicked("campaign2", "button10", 10L);

        repository.add(btn1);
        repository.add(btn2);
        repository.add(btn3);

        repository.remove(new FilterByCampaignId("campaign2"));

        List<ButtonClicked> result = repository.query(new QueryAll(ButtonClickedContract.TABLE_NAME));
        List<ButtonClicked> expected = Arrays.asList(btn1, btn2);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_buttonClicked_shouldDelete_multipleIams() {
        ButtonClickedRepository repository = new ButtonClickedRepository(context);

        ButtonClicked btn1 = new ButtonClicked("campaign1", "button1", 10L);
        ButtonClicked btn2 = new ButtonClicked("campaign1", "button3", 10L);
        ButtonClicked btn3 = new ButtonClicked("campaign2", "button10", 10L);
        ButtonClicked btn4 = new ButtonClicked("campaign3", "button10", 10L);

        repository.add(btn1);
        repository.add(btn2);
        repository.add(btn3);
        repository.add(btn4);

        repository.remove(new FilterByCampaignId("campaign1", "campaign2"));

        List<ButtonClicked> result = repository.query(new QueryAll(ButtonClickedContract.TABLE_NAME));
        List<ButtonClicked> expected = Collections.singletonList(btn4);

        assertEquals(expected, result);
    }

    @Test
    public void testExecution_buttonClicked_withEmptyIdArray() {
        ButtonClickedRepository repository = new ButtonClickedRepository(context);

        ButtonClicked btn1 = new ButtonClicked("campaign1", "button1", 10L);
        ButtonClicked btn2 = new ButtonClicked("campaign1", "button3", 10L);
        ButtonClicked btn3 = new ButtonClicked("campaign2", "button10", 10L);

        repository.add(btn1);
        repository.add(btn2);
        repository.add(btn3);

        repository.remove(new FilterByCampaignId());

        List<ButtonClicked> result = repository.query(new QueryAll(ButtonClickedContract.TABLE_NAME));
        List<ButtonClicked> expected = Arrays.asList(btn1, btn2, btn3);

        assertEquals(expected, result);
    }

}