package com.emarsys.core.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;

import com.emarsys.test.util.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ImageUtilsTest {

    public static final String DENNA_IMAGE = "https://ems-denna.herokuapp.com/images/Emarsys.png";

    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
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

    @Test
    public void testLoadOptimizedBitmap_withRemoteUrl_CleansUpTempFile() {
        clearCache();
        assertEquals(0, context.getCacheDir().list().length);
        ImageUtils.loadOptimizedBitmap(context, DENNA_IMAGE);
        assertEquals(0, context.getCacheDir().list().length);
    }

    @Test
    public void testLoadOptimizedBitmap_withLocalFile_ShouldNotCleansUpLocalFile() {
        clearCache();
        String fileUrl = FileUtils.download(context, DENNA_IMAGE);
        File imageFile = new File(fileUrl);
        assertTrue(imageFile.exists());
        ImageUtils.loadOptimizedBitmap(context, fileUrl);
        assertTrue(imageFile.exists());
    }

    @Test
    public void testLoadOptimizedBitmap_imageUrlShouldNotBeNull() {
        assertNull(ImageUtils.loadOptimizedBitmap(context, null));
    }

    @Test
    public void testLoadOptimizedBitmap_contextShouldNotBeNull() {
        assertNull(ImageUtils.loadOptimizedBitmap(null, "url"));
    }

    @Test
    public void testLoadOptimizedBitmap_imageUrlShouldNotBeInvalid() {
        assertNull(ImageUtils.loadOptimizedBitmap(context, "invalidUrl"));
    }

    @Test
    public void testLoadOptimizedBitmap_withRemoteUrl() {
        Bitmap bitmap = ImageUtils.loadOptimizedBitmap(context, DENNA_IMAGE);
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