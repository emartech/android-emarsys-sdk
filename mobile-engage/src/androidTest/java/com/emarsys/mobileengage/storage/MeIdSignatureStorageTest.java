package com.emarsys.mobileengage.storage;

import android.content.Context;

import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class MeIdSignatureStorageTest {
    private MeIdSignatureStorage storage;
    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        storage = createMeIdSignatureStorage();
        storage.remove();
    }

    @Test
    public void testGet_shouldReturnNull_ifTheStorageIsEmpty() {
        assertNull(storage.get());
    }

    @Test
    public void testSet() {
        storage.set("12345");
        assertEquals("12345", storage.get());
    }

    @Test
    public void testRemove_shouldRemoveMeIdSignature() {
        storage.set("12345");
        storage.remove();

        assertNull(storage.get());
    }

    @Test
    public void testSet_shouldPreserveMeIdSignature() {
        storage.set("12345");
        storage = createMeIdSignatureStorage();

        assertEquals("12345", storage.get());
    }

    private MeIdSignatureStorage createMeIdSignatureStorage() {
        return new MeIdSignatureStorage(context.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE));
    }
}