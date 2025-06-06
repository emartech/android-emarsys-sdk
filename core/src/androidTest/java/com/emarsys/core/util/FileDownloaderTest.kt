package com.emarsys.core.util

import android.content.Context
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.testUtil.FileTestUtils
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.RetryUtils
import com.emarsys.testUtil.TestUrls.LARGE_IMAGE
import com.emarsys.testUtil.TestUrls.customResponse
import com.emarsys.testUtil.rules.RetryRule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Arrays
import java.util.UUID
import java.util.concurrent.CountDownLatch

class FileDownloaderTest  {

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    private lateinit var context: Context
    private lateinit var fileDownloader: FileDownloader


    @Before
    fun setUp() {
        context = getTargetContext()
        fileDownloader = FileDownloader(context)
    }

    @Test

    fun testDownload_shouldNotReturnNull_whenUrlIsCorrect() {
        fileDownloader.download(LARGE_IMAGE) shouldNotBe null
    }

    @Test

    fun testDownload_shouldReturnNull_whenSchemeIsNotHttps() {
        fileDownloader.download("little://cat") shouldBe null
    }

    @Test

    fun testDownload_shouldReturnNull_whenResourceDoesNotExist() {
        val result = fileDownloader.download(customResponse(404))
        result shouldBe null
    }

    @Test

    fun testDownload_returnedPathShouldExist() {
        val filePath = fileDownloader.download(LARGE_IMAGE)
        val file = File(filePath!!)
        file.exists() shouldBe true
    }

    @Test

    fun testDownload_downloadedAndRemoteFileShouldBeTheSame() {
        val latch = CountDownLatch(1)
        val concurrentHandlerHolder: ConcurrentHandlerHolder =
            ConcurrentHandlerHolderFactory.create()

        concurrentHandlerHolder.coreHandler.post {
            val path: String = LARGE_IMAGE

            val filePath = fileDownloader.download(path, 3)!!
            val file = File(filePath)
            val fileInputStream: InputStream = FileInputStream(file)
            val remoteInputStream = fileDownloader.inputStreamFromUrl(path)
            try {
                Arrays.equals(
                    convertToByteArray(fileInputStream),
                    convertToByteArray(remoteInputStream!!)
                ) shouldBe true
            } finally {
                fileInputStream.close()
                remoteInputStream?.close()
                latch.countDown()
            }
        }
        latch.await()
    }

    @Test

    fun testDelete_shouldDeleteTheFile() {
        val filePath = createTempFile()
        val file = File(filePath)
        file.exists() shouldBe true
        fileDownloader.delete(filePath)
        file.exists() shouldBe false
    }

    @Test
    fun testDelete_shouldThrowException_whenFileIsNotExist() {
        shouldThrow<IllegalArgumentException> {
            fileDownloader.delete("file:///invalidFile.file")
        }
    }

    @Test

    fun testWriteReadFileIntoString() {
        val cacheFolder = context.cacheDir
        val fileName = UUID.randomUUID().toString()
        val fileUrl = File(cacheFolder, fileName).absolutePath
        val expected = "ContentOfTheFile\nNew line"
        FileTestUtils.writeToFile(expected, fileUrl)
        fileDownloader.readFileIntoString(fileUrl) shouldBe expected
    }

    @Test

    fun testReadURLIntoString() {
        fileDownloader.download(LARGE_IMAGE)?.let {
            val expected = fileDownloader.readFileIntoString(it)
            fileDownloader.readURLIntoString(LARGE_IMAGE) shouldBe expected
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