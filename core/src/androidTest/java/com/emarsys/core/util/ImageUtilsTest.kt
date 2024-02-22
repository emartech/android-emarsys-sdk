package com.emarsys.core.util

import android.graphics.BitmapFactory
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.copyInputStreamToFile
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junitpioneer.jupiter.RetryingTest

import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

class ImageUtilsTest {

    private companion object {
        const val IMAGE_URL = "https://emarsys.com"
    }

    private lateinit var mockFileDownloader: FileDownloader
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var mockHardwareIdProvider: HardwareIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockVersionProvider: VersionProvider


    @BeforeEach
    fun setup() {
        mockFileDownloader = mock {
            on { download(any(), any()) } doAnswer {
                val fileContent = getTargetContext().resources.openRawResource(
                    getTargetContext().resources.getIdentifier(
                        "emarsys_test_image",
                        "raw", getTargetContext().packageName
                    )
                )
                val file = File(getTargetContext().cacheDir.toURI().toURL().path + "/testFile.tmp")
                file.copyInputStreamToFile(fileContent)
                file.toURI().toURL().path
            }
            on { delete(any()) } doAnswer {
                File(getTargetContext().cacheDir.toURI().toURL().path + "/testFile.tmp").delete()
                Unit
            }

        }
        mockHardwareIdProvider = mock {
            on { provideHardwareId() } doReturn "hardwareId"
        }
        mockLanguageProvider = mock {
            on { provideLanguage(any()) } doReturn "language"
        }
        mockVersionProvider = mock {
            on { provideSdkVersion() } doReturn "version"
        }

        deviceInfo = DeviceInfo(
            getTargetContext(),
            mockHardwareIdProvider,
            mockVersionProvider,
            mockLanguageProvider,
            Mockito.mock(NotificationManagerHelper::class.java),
            isAutomaticPushSendingEnabled = true,
            isGooglePlayAvailable = true
        )
    }


    @Test
    @RetryingTest(3)
    fun testLoadOptimizedBitmap_returnsNull_whenImageUrlIsNull() {
        ImageUtils.loadOptimizedBitmap(mockFileDownloader, null, deviceInfo) shouldBe null
    }

    @Test
    @RetryingTest(3)
    fun testLoadOptimizedBitmap_withRemoteUrl_CleansUpTempFile() {
        clearCache()
        getTargetContext().cacheDir.list()?.size shouldBe 0
        ImageUtils.loadOptimizedBitmap(mockFileDownloader, IMAGE_URL, deviceInfo)
        getTargetContext().cacheDir.list()?.size shouldBe 0
    }

    @Test
    @RetryingTest(3)
    fun testLoadOptimizedBitmap_withLocalFile_ShouldNotCleanUpLocalFile() {
        clearCache()
        val fileUrl = mockFileDownloader.download(IMAGE_URL)
        val imageFile = File(fileUrl!!)
        imageFile.exists() shouldBe true
        ImageUtils.loadOptimizedBitmap(mockFileDownloader, fileUrl, deviceInfo)
        imageFile.exists() shouldBe true
    }

    @Test
    fun testLoadOptimizedBitmap_imageUrlShouldBeInvalid() {
        ImageUtils.loadOptimizedBitmap(mockFileDownloader, "invalidUrl", deviceInfo) shouldBe null
    }

    @Test
    @RetryingTest(3)
    fun testLoadOptimizedBitmap_withRemoteUrl() {
        val bitmap = ImageUtils.loadOptimizedBitmap(mockFileDownloader, IMAGE_URL, deviceInfo)
        bitmap shouldNotBe null
        bitmap!!.width shouldBeLessThan 2500
        bitmap.height shouldBeLessThan 2505
    }

    @Test
    @RetryingTest(3)
    fun testCalculateInSampleSize_returnedValueShouldBe4_whenRequestedWidthIs1080_widthIs2500() {
        val options = BitmapFactory.Options().apply {
            outWidth = 2500
        }
        val inSampleSize = ImageUtils.calculateInSampleSize(options, 1080)
        inSampleSize shouldBe 4
    }

    private fun clearCache() {
        clearDir(getTargetContext().cacheDir)
    }

    private fun clearDir(cacheDir: File) {
        for (file in cacheDir.listFiles() ?: emptyArray()) {
            if (file.isDirectory) {
                clearDir(file)
            }
            file.delete()
        }
    }
}