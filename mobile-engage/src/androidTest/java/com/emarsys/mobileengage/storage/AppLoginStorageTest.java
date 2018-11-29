package com.emarsys.mobileengage.storage;

import android.content.Context;
import androidx.test.InstrumentationRegistry;

import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AppLoginStorageTest {
    private AppLoginStorage storage;
    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        storage = createAppLoginStorage();
        storage.remove();
    }

    @Test
    public void testGetLastAppLoginPayloadHashCode_shouldReturnNull_ifTheStorageIsEmpty() {
        assertNull(storage.get());
    }

    @Test
    public void testSet() {
        storage.set(42);
        assertEquals((Integer) 42, storage.get());
    }

    @Test
    public void testClear_shouldRemoveLastAppLoginPayloadHashCodeValue() {
        storage.set(42);
        storage.remove();

        assertNull(storage.get());
    }

    @Test
    public void testSetLastAppLoginPayloadHashCode_shouldPreserveValues() {
        storage.set(42);
        storage = createAppLoginStorage();

        assertEquals((Integer) 42, storage.get());
    }

    private AppLoginStorage createAppLoginStorage() {
        return new AppLoginStorage(context.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE));
    }
}