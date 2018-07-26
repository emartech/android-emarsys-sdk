package com.emarsys.mobileengage.deeplink;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.MobileEngageUtils;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.util.MobileEngageIdlingResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DeepLinkInternalIdlingResourceTest {

    static {
        mock(Activity.class);
    }

    private MobileEngageIdlingResource idlingResource;
    private Activity activity;
    private DeepLinkInternal deepLink;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() throws Exception {
        DatabaseTestUtils.deleteMobileEngageDatabase();
        DatabaseTestUtils.deleteCoreDatabase();

        Application application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application(application)
                .credentials("user", "pass")
                .enableIdlingResource(true)
                .disableDefaultChannel()
                .build();

        RequestIdProvider requestIdProvider = mock(RequestIdProvider.class);
        when(requestIdProvider.provideId()).thenReturn("REQUEST_ID");

        RequestContext requestContext = new RequestContext(
                mock(MobileEngageConfig.class),
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                requestIdProvider
        );

        deepLink = new DeepLinkInternal(mock(RequestManager.class), requestContext);

        activity = mock(Activity.class, Mockito.RETURNS_DEEP_STUBS);

        MobileEngageUtils.setup(config);
        idlingResource = mock(MobileEngageIdlingResource.class);
        Field idlingResourceField = MobileEngageUtils.class.getDeclaredField("idlingResource");
        idlingResourceField.setAccessible(true);
        idlingResourceField.set(null, idlingResource);
    }

    @Test
    public void testTrackDeepLink_correctIntent_callsIdlingResource() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5"));

        deepLink.trackDeepLinkOpen(activity, intent);

        verify(idlingResource, times(1)).increment();
    }

    @Test
    public void testTrackDeepLink_emptyIntent_doesNotCallIdlingResource() {
        deepLink.trackDeepLinkOpen(activity, new Intent());

        verifyZeroInteractions(idlingResource);
    }

    @Test
    public void testTrackDeepLink_invalidIntent_doesNotCallIdlingResource() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&other=1_2_3_4_5"));

        deepLink.trackDeepLinkOpen(activity, intent);

        verifyZeroInteractions(idlingResource);
    }
}
