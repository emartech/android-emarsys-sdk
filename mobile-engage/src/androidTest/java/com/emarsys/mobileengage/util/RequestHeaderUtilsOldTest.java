package com.emarsys.mobileengage.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.storage.StringStorage;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.storage.MobileEngageStorageKey;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.SharedPrefsUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestHeaderUtilsOldTest {
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String APPLICATION_PASSWORD = "applicationPassword";
    public static final String EMARSYS_SHARED_PREFERENCES = "emarsys_shared_preferences";

    private RequestContext debugRequestContext;
    private RequestContext releaseRequestContext;
    private SharedPreferences sharedPreferences;
    private Storage<String> clientStateStorage;
    private Storage<String> contactTokenStorage;
    private Storage<String> refreshTokenStorage;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        SharedPrefsUtils.clearSharedPrefs(EMARSYS_SHARED_PREFERENCES);

        Context context = InstrumentationRegistry.getTargetContext();
        sharedPreferences = context.getSharedPreferences(EMARSYS_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        String meId = "meid";
        String meIdSignature = "meidsignature";
        MeIdStorage meIdStorage = new MeIdStorage(sharedPreferences);
        meIdStorage.set(meId);
        MeIdSignatureStorage meIdSignatureStorage = new MeIdSignatureStorage(sharedPreferences);
        meIdSignatureStorage.set(meIdSignature);

        clientStateStorage = new StringStorage(MobileEngageStorageKey.CLIENT_STATE, sharedPreferences);
        contactTokenStorage = new StringStorage(MobileEngageStorageKey.CONTACT_TOKEN, sharedPreferences);
        refreshTokenStorage = new StringStorage(MobileEngageStorageKey.REFRESH_TOKEN, sharedPreferences);

        UUIDProvider uuidProvider = mock(UUIDProvider.class);
        when(uuidProvider.provideId()).thenReturn("REQUEST_ID");

        TimestampProvider timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(100_000L);

        DeviceInfo debugDeviceInfo = mock(DeviceInfo.class);
        when(debugDeviceInfo.isDebugMode()).thenReturn(true);

        debugRequestContext = new RequestContext(
                APPLICATION_CODE,
                APPLICATION_PASSWORD,
                1,
                debugDeviceInfo,
                mock(AppLoginStorage.class),
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider,
                uuidProvider,
                clientStateStorage,
                contactTokenStorage,
                refreshTokenStorage);

        DeviceInfo releaseDeviceInfo = mock(DeviceInfo.class);
        when(releaseDeviceInfo.isDebugMode()).thenReturn(false);

        releaseRequestContext = new RequestContext(
                APPLICATION_CODE,
                APPLICATION_PASSWORD,
                1,
                releaseDeviceInfo,
                mock(AppLoginStorage.class),
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider,
                uuidProvider,
                clientStateStorage,
                contactTokenStorage,
                refreshTokenStorage);


    }

    @After
    public void tearDown() {
        SharedPrefsUtils.clearSharedPrefs(EMARSYS_SHARED_PREFERENCES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V2_configShouldNotBeNull() {
        RequestHeaderUtils_Old.createBaseHeaders_V2(null);
    }

    @Test
    public void testCreateBaseHeaders_V2_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Authorization", HeaderUtils.createBasicAuth(releaseRequestContext.getApplicationCode(), releaseRequestContext.getApplicationPassword()));

        Map<String, String> result = RequestHeaderUtils_Old.createBaseHeaders_V2(releaseRequestContext);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V3_requestContextShouldNotBeNull() {
        RequestHeaderUtils_Old.createBaseHeaders_V3(null);
    }

    @Test
    public void testCreateBaseHeaders_V3_shouldReturnCorrectMap() {
        String meId = "meid";
        String meIdSignature = "meidsignature";
        MeIdStorage meIdStorage = new MeIdStorage(sharedPreferences);
        meIdStorage.set(meId);
        MeIdSignatureStorage meIdSignatureStorage = new MeIdSignatureStorage(sharedPreferences);
        meIdSignatureStorage.set(meIdSignature);

        UUIDProvider uuidProvider = mock(UUIDProvider.class);
        when(uuidProvider.provideId()).thenReturn("REQUEST_ID");

        TimestampProvider timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(100_000L);

        RequestContext requestContext = new RequestContext(
                APPLICATION_CODE,
                APPLICATION_PASSWORD,
                1,
                mock(DeviceInfo.class),
                mock(AppLoginStorage.class),
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider,
                uuidProvider,
                clientStateStorage,
                contactTokenStorage,
                refreshTokenStorage);

        Map<String, String> expected = new HashMap<>();
        expected.put("X-ME-ID", meId);
        expected.put("X-ME-ID-SIGNATURE", meIdSignature);
        expected.put("X-ME-APPLICATIONCODE", APPLICATION_CODE);

        Map<String, String> result = RequestHeaderUtils_Old.createBaseHeaders_V3(requestContext);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefaultHeaders_configShouldNotBeNull() {
        RequestHeaderUtils_Old.createDefaultHeaders(null);
    }

    @Test
    public void testCreateDefaultHeaders_returnedValueShouldNotBeNull() {
        assertNotNull(RequestHeaderUtils_Old.createDefaultHeaders(debugRequestContext));
    }

    @Test
    public void testCreateDefaultHeaders_debug_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Content-Type", "application/json");
        expected.put("X-EMARSYS-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-EMARSYS-SDK-MODE", "debug");

        Map<String, String> result = RequestHeaderUtils_Old.createDefaultHeaders(debugRequestContext);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateDefaultHeaders_release_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Content-Type", "application/json");
        expected.put("X-EMARSYS-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-EMARSYS-SDK-MODE", "production");

        Map<String, String> result = RequestHeaderUtils_Old.createDefaultHeaders(releaseRequestContext);

        assertEquals(expected, result);
    }

}