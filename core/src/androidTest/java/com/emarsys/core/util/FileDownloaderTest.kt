package com.emarsys.core.util

import android.content.Context
import com.emarsys.testUtil.FileTestUtils
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.RetryUtils.retryRule
import com.emarsys.testUtil.TestUrls.LARGE_IMAGE
import com.emarsys.testUtil.TestUrls.customResponse
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

class FileDownloaderTest {

    private lateinit var context: Context
    private lateinit var fileDownloader: FileDownloader

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    var retry: TestRule = retryRule

    @Before
    fun setUp() {
        context = getTargetContext()
        fileDownloader = FileDownloader(context)
    }

    @Test
    fun testDownload_shouldNotReturnNull_whenUrlIsCorrect() {
        assertNotNull(fileDownloader.download(LARGE_IMAGE))
    }

    @Test
    fun testDownload_shouldReturnNull_whenSchemeIsNotHttps() {
        assertNull(fileDownloader.download("little://cat"))
    }

    @Test
    fun testDownload_shouldReturnNull_whenResourceDoesNotExist() {
        assertNull(fileDownloader.download(customResponse(404)))
    }

    @Test
    fun testDownload_returnedPathShouldExist() {
        val filePath = fileDownloader.download(LARGE_IMAGE)
        val file = File(filePath!!)
        assertTrue(file.exists())
    }

    @Test
    fun testDownload_downloadedAndRemoteFileShouldBeTheSame() {
        val path: String = LARGE_IMAGE
        val filePath = fileDownloader.download(path)
        val file = File(filePath)
        val fileInputStream: InputStream = FileInputStream(file)
        val remoteInputStream = fileDownloader.inputStreamFromUrl(path)
        assertTrue(Arrays.equals(convertToByteArray(fileInputStream), convertToByteArray(remoteInputStream!!)))
    }

    @Test
    fun testDelete_shouldDeleteTheFile() {
        val filePath = createTempFile()
        val file = File(filePath)
        assertTrue(file.exists())
        fileDownloader.delete(filePath)
        assertFalse(file.exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDelete_shouldThrowException_whenFileIsNotExist() {
        fileDownloader.delete("file:///invalidFile.file")
    }

    @Test
    fun testWriteReadFileIntoString() {
        val cacheFolder = context.cacheDir
        val fileName = UUID.randomUUID().toString()
        val fileUrl = File(cacheFolder, fileName).absolutePath
        val expected = "ContentOfTheFile\nNew line"
        FileTestUtils.writeToFile(expected, fileUrl)
        assertEquals(expected, fileDownloader.readFileIntoString(fileUrl))
    }

    @Test
    fun testReadURLIntoString() {
        fileDownloader.download(LARGE_IMAGE)?.let {
            val expected = fileDownloader.readFileIntoString(it)
            assertEquals(expected, fileDownloader.readURLIntoString(LARGE_IMAGE))
        }
    }

    private fun convertToByteArray(inputStream: InputStream): ByteArray? {
        val buffer = ByteArray(4096)
        val bao = ByteArrayOutputStream()
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            bao.write(buffer, 0, bytesRead)
        }
        return bao.toByteArray()
    }

    private fun createTempFile(): String {
        val cacheFolder = context.cacheDir
        val fileName = UUID.randomUUID().toString()
        val resultFile = File(cacheFolder, fileName)
        resultFile.createNewFile()
        return resultFile.toURI().toURL().path
    }
}