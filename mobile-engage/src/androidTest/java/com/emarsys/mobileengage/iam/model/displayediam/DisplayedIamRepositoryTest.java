package com.emarsys.mobileengage.iam.model.displayediam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.helper.DbHelper;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.test.filters.SdkSuppress;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.emarsys.core.database.DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID;
import static com.emarsys.core.database.DatabaseContract.DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SdkSuppress(minSdkVersion = KITKAT)
public class DisplayedIamRepositoryTest {

    static {
        mock(Cursor.class);
    }

    private DisplayedIamRepository iamRepository;
    private DisplayedIam displayedIam1;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();

        Context context = InstrumentationRegistry.getTargetContext();
        DbHelper dbHelper = new CoreDbHelper(context, new HashMap<TriggerKey, List<Runnable>>());
        iamRepository = new DisplayedIamRepository(dbHelper);
        displayedIam1 = new DisplayedIam("campaign1", new Date().getTime());
    }

    @Test
    public void testContentValuesFromItem() {
        ContentValues expected = new ContentValues();
        expected.put(DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID, displayedIam1.getCampaignId());
        expected.put(DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP, displayedIam1.getTimestamp());

        ContentValues result = iamRepository.contentValuesFromItem(displayedIam1);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testItemFromCursor() {
        Cursor cursor = mock(Cursor.class);

        when(cursor.getColumnIndex(DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID)).thenReturn(0);
        when(cursor.getString(0)).thenReturn(displayedIam1.getCampaignId());
        when(cursor.getColumnIndex(DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP)).thenReturn(1);
        when(cursor.getLong(1)).thenReturn(displayedIam1.getTimestamp());

        DisplayedIam result = iamRepository.itemFromCursor(cursor);
        DisplayedIam expected = displayedIam1;

        Assert.assertEquals(expected, result);
    }

}