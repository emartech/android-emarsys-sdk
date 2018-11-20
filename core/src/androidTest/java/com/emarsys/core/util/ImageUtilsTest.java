package com.emarsys.core.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ImageUtilsTest {

    public static final String DENNA_IMAGE = "https://ems-denna.herokuapp.com/images/Emarsys.png";

    private Context context;
    private DeviceInfo deviceInfo;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        deviceInfo = new DeviceInfo(context, mock(HardwareIdProvider.class));
    }

    @Test
    public void testLoadBitmap_contextShouldNotBeNull() {
        assertNull(ImageUtils.loadBitmap(null, "url"));
    }

    @Test
    public void testLoadBitmap_imageUrlShouldNotBeNull() {

        assertNull(ImageUtils.loadBitmap(context, null));
    }

    @Test
    public void testLoadBitmap_imageUrlShouldNotBeInvalid() {
        assertNull(ImageUtils.loadBitmap(context, "invalidUrl"));
    }

    @Test
    public void testLoadBitmap_withRemoteUrl() {
        Bitmap bitmap = ImageUtils.loadBitmap(context, DENNA_IMAGE);
        assertNotNull(bitmap);
        assertEquals(bitmap.getWidth(), 2500);
        assertEquals(bitmap.getHeight(), 2505);
    }

    @Test
    public void testLoadBitmap_withRemoteUrl_CleansUpTempFile() {
        clearCache();
        assertEquals(0, context.getCacheDir().list().length);
        ImageUtils.loadBitmap(context, DENNA_IMAGE);
        assertEquals(0, context.getCacheDir().list().length);
    }

    @Test
    public void testLoadBitmap_withLocalFile_ShouldNotCleansUpLocalFile() {
        clearCache();
        String fileUrl = FileUtils.download(context, DENNA_IMAGE);
        File imageFile = new File(fileUrl);
        assertTrue(imageFile.exists());
        ImageUtils.loadBitmap(context, fileUrl);
        assertTrue(imageFile.exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadOptimizedBitmap_deviceInfo_mustNotBeNull() {
        ImageUtils.loadOptimizedBitmap(context, DENNA_IMAGE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadOptimizedBitmap_context_mustNotBeNull() {
        assertNull(ImageUtils.loadOptimizedBitmap(null, "url", deviceInfo));
    }

    @Test
    public void testLoadOptimizedBitmap_returnsNull_whenImageUrlIsNull() {
        assertNull(ImageUtils.loadOptimizedBitmap(context, null, deviceInfo));
    }

    @Test
    public void testLoadOptimizedBitmap_withRemoteUrl_CleansUpTempFile() {
        clearCache();
        assertEquals(0, context.getCacheDir().list().length);
        ImageUtils.loadOptimizedBitmap(context, DENNA_IMAGE, deviceInfo);
        assertEquals(0, context.getCacheDir().list().length);
    }

    @Test
    public void testLoadOptimizedBitmap_withLocalFile_ShouldNotCleanUpLocalFile() {
        clearCache();
        String fileUrl = FileUtils.download(context, DENNA_IMAGE);
        File imageFile = new File(fileUrl);
        assertTrue(imageFile.exists());
        ImageUtils.loadOptimizedBitmap(context, fileUrl, deviceInfo);
        assertTrue(imageFile.exists());
    }

    @Test
    public void testLoadOptimizedBitmap_imageUrlShouldBeInvalid() {
        assertNull(ImageUtils.loadOptimizedBitmap(context, "invalidUrl", deviceInfo));
    }

    @Test
    public void testLoadOptimizedBitmap_withRemoteUrl() {
        Bitmap bitmap = ImageUtils.loadOptimizedBitmap(context, DENNA_IMAGE, deviceInfo);
        assertNotNull(bitmap);
        assertTrue(bitmap.getWidth() < 2500);
        assertTrue(bitmap.getHeight() < 2505);
    }

    @Test
    public void testCalculateInSampleSize_returnedValueShouldBe4_whenRequestedWidthIs1080_widthIs2500() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = 2500;
        int inSampleSize = ImageUtils.calculateInSampleSize(options, 1080);
        assertEquals(4, inSampleSize);
    }

    private void clearCache() {
        clearDir(context.getCacheDir());
    }

    private void clearDir(File cacheDir) {
        for (File file : cacheDir.listFiles()) {
            if (file.isDirectory()) {
                clearDir(file);
            }
            file.delete();
        }
    }
}