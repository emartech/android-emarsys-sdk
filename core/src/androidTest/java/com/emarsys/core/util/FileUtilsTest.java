package com.emarsys.core.util;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FileUtilsTest {

    public static final String DENNA_404 = "https://ems-denna.herokuapp.com/customResponseCode/404";
    public static final String DENNA_IMAGE = "https://ems-denna.herokuapp.com/images/Emarsys.png";

    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getContext();
    }

    @Test
    public void testDownload_contextShouldNotBeNull() {
        assertNull(FileUtils.download(null, "path"));
    }

    @Test
    public void testDownload_pathShouldNotBeNull() {
        assertNull(FileUtils.download(context, null));
    }

    @Test
    public void testDownload_shouldNotReturnNull_whenUrlIsCorrect() {
        assertNotNull(FileUtils.download(context, DENNA_IMAGE));
    }

    @Test
    public void testDownload_shouldReturnNull_whenSchemeIsNotHttps() {
        assertNull(FileUtils.download(context, "little://cat"));
    }

    @Test
    public void testDownload_shouldReturnNull_whenResourceDoesNotExist() {
        assertNull(FileUtils.download(context, DENNA_404));
    }

    @Test
    public void testDownload_returnedPathShouldExist() {
        String filePath = FileUtils.download(context, DENNA_IMAGE);
        File file = new File(filePath);
        assertTrue(file.exists());
    }

    @Test
    public void testDownload_downloadedAndRemoteFileShouldBeTheSame() throws Exception {
        String path = DENNA_IMAGE;
        String filePath = FileUtils.download(context, path);
        File file = new File(filePath);
        InputStream fileInputStream = new FileInputStream(file);
        InputStream remoteInputStream = FileUtils.inputStreamFromUrl(path);
        assertTrue(Arrays.equals(convertToByteArray(fileInputStream), convertToByteArray(remoteInputStream)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelete_shouldThrowException() {
        FileUtils.delete(null);
    }

    @Test
    public void testDelete_shouldDeleteTheFile() {
        String filePath = FileUtils.download(context, DENNA_IMAGE);
        File file = new File(filePath);
        assertTrue(file.exists());
        FileUtils.delete(filePath);
        assertFalse(file.exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelete_shouldThrowException_whenFileIsNotExist() {
        FileUtils.delete("file:///invalidFile.file");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteToFile_inputMustNotBeNull() {
        File cacheFolder = context.getCacheDir();
        String fileName = UUID.randomUUID().toString();

        String fileUrl = new File(cacheFolder, fileName).getAbsolutePath();
        FileUtils.writeToFile(null, fileUrl);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteToFile_filePathMustNotBeNull() {
        String expected = "ContentOfTheFile\nNew line";

        FileUtils.writeToFile(expected, null);
    }

    @Test
    public void testWriteReadFileIntoString() {
        File cacheFolder = context.getCacheDir();
        String fileName = UUID.randomUUID().toString();

        String fileUrl = new File(cacheFolder, fileName).getAbsolutePath();
        String expected = "ContentOfTheFile\nNew line";
        FileUtils.writeToFile(expected, fileUrl);

        assertEquals(expected, FileUtils.readFileIntoString(fileUrl));
    }

    @Test
    public void testReadURLIntoString() {
        String file = FileUtils.download(context, DENNA_IMAGE);
        String expected = FileUtils.readFileIntoString(file);

        assertEquals(expected, FileUtils.readURLIntoString(DENNA_IMAGE));
    }

    @Test
    public void testReadURLIntoString_shouldReturnNull_whenUrlIsNull() {
        assertEquals(null, FileUtils.readURLIntoString(null));
    }

    private byte[] convertToByteArray(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];

        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            bao.write(buffer, 0, bytesRead);
        }

        return bao.toByteArray();
    }
}