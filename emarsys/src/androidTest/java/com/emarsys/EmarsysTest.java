package com.emarsys;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.config.EmarsysConfig;
import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.trigger.TriggerEvent;
import com.emarsys.core.database.trigger.TriggerType;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.di.EmarsysDependencyContainer;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.predict.PredictInternal;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.shard.PredictShardTrigger;
import com.emarsys.testUtil.RandomTestUtils;
import com.emarsys.testUtil.ReflectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class EmarsysTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private MobileEngageInternal mockMobileEngageInternal;
    private PredictInternal mockPredictInternal;
    private Application application;

    @Before
    public void init() {
        mockMobileEngageInternal = mock(MobileEngageInternal.class);
        mockPredictInternal = mock(PredictInternal.class);
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

        ReflectionTestUtils.setStaticField(Emarsys.class, "mobileEngageInternal", mockMobileEngageInternal);
        ReflectionTestUtils.setStaticField(Emarsys.class, "predictInternal", mockPredictInternal);
    }

    @After
    public void tearDown() {
        ReflectionTestUtils.setStaticField(Emarsys.class, "mobileEngageInternal", null);
        ReflectionTestUtils.setStaticField(Emarsys.class, "predictInternal", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetup_config_mustNotBeNull() {
        Emarsys.setup(null);
    }

    @Test
    public void testSetup_registersPredictTigger() {
        EmarsysDependencyContainer container = mock(EmarsysDependencyContainer.class);
        CoreSQLiteDatabase coreDB = mock(CoreSQLiteDatabase.class);
        Runnable trigger = mock(PredictShardTrigger.class);

        when(container.getCoreSQLiteDatabase()).thenReturn(coreDB);
        when(container.getPredictShardTrigger()).thenReturn(trigger);

        DependencyInjection.setup(container);

        EmarsysConfig config = new EmarsysConfig.Builder()
                .mobileEngageCredentials("", "")
                .contactFieldId(1)
                .predictMerchantId("")
                .disableDefaultChannel()
                .application(application).build();

        Emarsys.setup(config);

        verify(coreDB).registerTrigger("shard", TriggerType.AFTER, TriggerEvent.INSERT, trigger);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSetCustomer_customerId_mustNotBeNull() {
        Emarsys.setCustomer(null);
    }

    @Test
    public void testSetCustomer_delegatesTo_mobileEngageInternalAppLogin() {
        String customerId = "customerId";

        Emarsys.setCustomer(customerId);

        verify(mockMobileEngageInternal).appLogin(customerId);
    }

    @Test
    public void testSetCustomer_delegatesTo_predictInternalSetCustomer() {
        String customerId = "customerId";

        Emarsys.setCustomer(customerId);

        verify(mockPredictInternal).setCustomer(customerId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEvent_eventName_mustNotBeNull() {
        Emarsys.trackCustomEvent(null, new HashMap<String, String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackCart_items_mustNotBeNull() {
        Emarsys.Predict.trackCart(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackCart_itemElements_mustNotBeNull() {
        Emarsys.Predict.trackCart(Arrays.<CartItem>asList(null, null));
    }

    @Test
    public void testTrackCart_delegatesTo_predictInternal() {
        List<CartItem> itemList = Arrays.asList(
                createItem("itemId0", 200.0, 100.0),
                createItem("itemId1", 201.0, 101.0),
                createItem("itemId2", 202.0, 102.0));

        Emarsys.Predict.trackCart(itemList);

        verify(mockPredictInternal).trackCart(itemList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackItemView_itemViewId_mustNotBeNull() {
        Emarsys.Predict.trackItemView(null);
    }

    @Test
    public void testTrackItemView_delegatesTo_predictInternal() {
        String itemId = RandomTestUtils.randomString();

        Emarsys.Predict.trackItemView(itemId);

        verify(mockPredictInternal).trackItemView(itemId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredict_testTrackCategoryView_categoryPath_mustNotBeNull() {
        Emarsys.Predict.trackCategoryView(null);
    }

    @Test
    public void testTrackCategoryView_delegatesTo_predictInternal() {
        String categoryPath = RandomTestUtils.randomString();

        Emarsys.Predict.trackCategoryView(categoryPath);

        verify(mockPredictInternal).trackCategoryView(categoryPath);
    }

    private CartItem createItem(final String id, final double price, final double quantity) {
        return new CartItem() {
            @Override
            public String getItemId() {
                return id;
            }

            @Override
            public double getPrice() {
                return price;
            }

            @Override
            public double getQuantity() {
                return quantity;
            }
        };
    }
}
