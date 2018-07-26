package com.emarsys.mobileengage.inbox.model;

import android.support.test.runner.AndroidJUnit4;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class NotificationCacheTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private NotificationCache notificationCache;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;
    private Notification notification4;
    private Notification notification5;

    @Before
    public void init() {
        notificationCache = new NotificationCache();
        NotificationCache.internalCache.clear();

        notification1 = new Notification("id1", "sid1", "title1", null, new HashMap<String, String>(), new JSONObject(), 100, 10000000);
        notification2 = new Notification("id2", "sid2", "title2", null, new HashMap<String, String>(), new JSONObject(), 200, 20000000);
        notification3 = new Notification("id3", "sid3", "title3", null, new HashMap<String, String>(), new JSONObject(), 300, 30000000);
        notification4 = new Notification("id4", "sid4", "title4", null, new HashMap<String, String>(), new JSONObject(), 400, 40000000);
        notification5 = new Notification("id5", "sid5", "title5", null, new HashMap<String, String>(), new JSONObject(), 500, 50000000);
    }

    @Test
    public void testCache() {
        Notification notification = mock(Notification.class);
        notificationCache.cache(notification);

        Assert.assertFalse(NotificationCache.internalCache.isEmpty());
        Assert.assertEquals(1, NotificationCache.internalCache.size());
        Assert.assertEquals(notification, NotificationCache.internalCache.get(0));
    }

    @Test
    public void testCache_ignoresNull() {
        notificationCache.cache(notification1);
        notificationCache.cache(null);
        notificationCache.cache(notification2);

        Assert.assertEquals(2, NotificationCache.internalCache.size());
        Assert.assertEquals(notification2, NotificationCache.internalCache.get(0));
        Assert.assertEquals(notification1, NotificationCache.internalCache.get(1));
    }

    @Test
    public void testMerge_withEmptyLists() {
        List<Notification> result = notificationCache.merge(new ArrayList<Notification>());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testMerge_withEmptyCache() {
        List<Notification> fetched = new ArrayList<>(Arrays.asList(notification1, notification2, notification3));
        List<Notification> result = notificationCache.merge(fetched);

        List<Notification> expected = new ArrayList<>(Arrays.asList(notification1, notification2, notification3));

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testMerge_withEmptyFetched() {
        notificationCache.cache(notification3);
        notificationCache.cache(notification2);
        notificationCache.cache(notification1);

        List<Notification> fetched = new ArrayList<>();
        List<Notification> result = notificationCache.merge(fetched);

        List<Notification> expected = new ArrayList<>(Arrays.asList(notification1, notification2, notification3));

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testMerge_withNonEmptyLists() {
        notificationCache.cache(notification3);
        notificationCache.cache(notification2);
        notificationCache.cache(notification1);

        List<Notification> fetched = new ArrayList<>();
        fetched.add(notification4);
        fetched.add(notification5);

        List<Notification> result = notificationCache.merge(fetched);

        List<Notification> expected = new ArrayList<>(
                Arrays.asList(
                        notification1,
                        notification2,
                        notification3,
                        notification4,
                        notification5));

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testMerge_shouldInvalidateAutomatically() {
        List<Notification> expected = new ArrayList<>();
        expected.add(notification1);
        expected.add(notification2);

        notificationCache.cache(notification3);
        notificationCache.cache(notification2);
        notificationCache.cache(notification1);

        List<Notification> fetched = new ArrayList<>();
        fetched.add(notification3);
        fetched.add(notification4);
        fetched.add(notification5);

        notificationCache.merge(fetched);

        Assert.assertEquals(expected, NotificationCache.internalCache);
    }

    @Test
    public void testInvalidate_withEmptyList() {
        notificationCache.cache(notification3);
        notificationCache.cache(notification2);
        notificationCache.cache(notification1);

        List<Notification> fetched = new ArrayList<>();

        notificationCache.invalidate(fetched);

        List<Notification> expected = new ArrayList<>(Arrays.asList(notification1, notification2, notification3));

        Assert.assertEquals(expected, NotificationCache.internalCache);
    }

    @Test
    public void testInvalidate_withNonEmptyList() {
        notificationCache.cache(notification3);
        notificationCache.cache(notification2);
        notificationCache.cache(notification1);

        List<Notification> fetched = new ArrayList<>(Arrays.asList(
                new Notification("id1", "sid1", "title1", null, null, null, Integer.MAX_VALUE, 20),
                new Notification("id3", "sid3", "title3", null, null, null, Integer.MAX_VALUE, 50)
        ));

        notificationCache.invalidate(fetched);

        Notification expected = notification2;

        Assert.assertEquals(1, NotificationCache.internalCache.size());
        Assert.assertEquals(expected, NotificationCache.internalCache.get(0));
    }

    @Test
    public void testInvalidate_withoutCommonElements() {
        notificationCache.cache(notification3);
        notificationCache.cache(notification2);
        notificationCache.cache(notification1);

        List<Notification> fetched = new ArrayList<>(Arrays.asList(notification4, notification5));

        notificationCache.invalidate(fetched);

        List<Notification> expected = new ArrayList<>(Arrays.asList(notification1, notification2, notification3));

        Assert.assertEquals(expected, NotificationCache.internalCache);
    }
}