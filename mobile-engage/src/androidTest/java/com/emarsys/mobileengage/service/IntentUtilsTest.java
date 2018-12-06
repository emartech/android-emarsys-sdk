package com.emarsys.mobileengage.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntentUtilsTest {

    static {
        mock(Activity.class);
        mock(PackageManager.class);
    }

    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateLaunchIntent_remoteIntentMustNotBeNull() {
        IntentUtils.createLaunchIntent(null, context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateLaunchIntent_contextMustNotBeNull() {
        Intent intent = new Intent();
        intent.putExtra("key", "value");
        IntentUtils.createLaunchIntent(intent, null);
    }

    @Test
    public void testCreateLaunchIntent() {
        Intent launchIntentForPackage = new Intent();
        PackageManager pm = mock(PackageManager.class);
        when(pm.getLaunchIntentForPackage(anyString())).thenReturn(launchIntentForPackage);
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getPackageManager()).thenReturn(pm);
        when(mockActivity.getPackageName()).thenReturn("packageName");


        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("key", "value");
        intent.putExtras(bundle);
        Intent result = IntentUtils.createLaunchIntent(intent, mockActivity);

        Assert.assertTrue(launchIntentForPackage == result);

        Bundle launcherBundle = launchIntentForPackage.getExtras();

        Assert.assertEquals(bundle.keySet(), launcherBundle.keySet());
        for (String key : bundle.keySet()) {
            Assert.assertEquals(bundle.get(key), launcherBundle.get(key));
        }
    }

    @Test
    public void testCreateLaunchIntent_withNoBundleInIntent() {
        Intent launchIntentForPackage = new Intent();
        PackageManager pm = mock(PackageManager.class);
        when(pm.getLaunchIntentForPackage(anyString())).thenReturn(launchIntentForPackage);
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getPackageManager()).thenReturn(pm);
        when(mockActivity.getPackageName()).thenReturn("packageName");

        Intent result = IntentUtils.createLaunchIntent(new Intent(), mockActivity);

        Assert.assertTrue(launchIntentForPackage == result);
        Assert.assertEquals(null, launchIntentForPackage.getExtras());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNotificationHandlerServiceIntent_remoteMessageDataMustNotBeNull() {
        IntentUtils.createNotificationHandlerServiceIntent(context, null, 0, "action");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNotificationHandlerServiceIntent_contextMustNotBeNull() {
        IntentUtils.createNotificationHandlerServiceIntent(null, new HashMap<String, String>(), 0, "action");
    }

    @Test
    public void createNotificationHandlerServiceIntent() {
        int notificationId = 987;

        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("key1", "value1");
        remoteMessageData.put("key2", "value2");

        Intent resultIntent = IntentUtils.createNotificationHandlerServiceIntent(context, remoteMessageData, notificationId, "action");
        assertEquals("action", resultIntent.getAction());
        Bundle payload = resultIntent.getBundleExtra("payload");
        assertEquals("value1", payload.getString("key1"));
        assertEquals("value2", payload.getString("key2"));
        assertEquals(notificationId, payload.getInt("notification_id"));
    }

    @Test
    public void createNotificationHandlerServiceIntent_withoutAction() {
        Intent resultIntent = IntentUtils.createNotificationHandlerServiceIntent(context, new HashMap<String, String>(), 0, null);
        assertEquals(null, resultIntent.getAction());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNotificationHandlerServicePendingIntent_remoteMessageDataMustNotBeNull() {
        IntentUtils.createNotificationHandlerServicePendingIntent(context, null, 0, "action");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNotificationHandlerServicePendingIntent_contextMustNotBeNull() {
        IntentUtils.createNotificationHandlerServicePendingIntent(null, new HashMap<String, String>(), 0, "action");
    }

}