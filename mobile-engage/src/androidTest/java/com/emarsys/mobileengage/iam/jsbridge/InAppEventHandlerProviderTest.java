package com.emarsys.mobileengage.iam.jsbridge;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.EventHandler;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Field;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@SdkSuppress(minSdkVersion = KITKAT)
public class InAppEventHandlerProviderTest {

    EventHandler inAppEventHandler;
    InAppMessageHandlerProvider provider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Application application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        inAppEventHandler = mock(EventHandler.class);

        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application(application)
                .credentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .disableDefaultChannel()
                .enableExperimentalFeatures(MobileEngageFeature.IN_APP_MESSAGING)
                .setDefaultInAppEventHandler(inAppEventHandler)
                .build();

        Field configField = MobileEngage.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(null, config);

        provider = new InAppMessageHandlerProvider();
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Field configField = MobileEngage.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(null, null);
    }


    @Test
    public void testProvideHandler() {
        assertEquals(inAppEventHandler, provider.provideHandler());
    }

}