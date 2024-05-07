package com.emarsys.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.URLUtil
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.util.log.Logger
import com.emarsys.core.util.log.entry.CrashLog
import java.io.File

object ImageUtils {
    private const val RETRY_COUNT = 3

    fun loadOptimizedBitmap(fileDownloader: FileDownloader, imageUrl: String?, deviceInfo: DeviceInfo): Bitmap? {
        var result: Bitmap? = null
        if (isImageUrlValid(imageUrl)) {
            val fileUrl = downloadImage(fileDownloader, imageUrl)
            if (fileUrl != null) {
                result = loadBitmap(fileUrl, deviceInfo.displayMetrics.widthPixels)
                if (isRemoteUrl(imageUrl)) {
                    fileDownloader.delete(fileUrl)
                }
            }
        }
        return result
    }

    private fun isImageUrlValid(imageUrl: String?): Boolean {
        var result = true
        if (imageUrl == null) {
            result = false
        } else if (!URLUtil.isHttpsUrl(imageUrl)) {
            result = File(imageUrl).exists()
        }
        return result
    }

    private fun downloadImage(fileDownloader: FileDownloader, imageUrl: String?): String? {
        var fileUrl = imageUrl
        if (isRemoteUrl(imageUrl) && imageUrl != null) {
            fileUrl = fileDownloader.download(imageUrl, RETRY_COUNT)
        }
        return fileUrl
    }

    private fun isRemoteUrl(imageUrl: String?): Boolean {
        return URLUtil.isHttpsUrl(imageUrl)
    }

    private fun loadBitmap(imageFileUrl: String, width: Int): Bitmap? = try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imageFileUrl, options)
        options.inSampleSize = calculateInSampleSize(options, width)
        options.inJustDecodeBounds = false
        BitmapFactory.decodeFile(imageFileUrl, options)
    } catch (exception:Exception) {
        Logger.error(CrashLog(exception))
        null
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int): Int {
        val width = options.outWidth
        var inSampleSize = 1
        while (reqWidth <= width / inSampleSize) {
            inSampleSize *= 2
        }
        return inSampleSize
    }
}