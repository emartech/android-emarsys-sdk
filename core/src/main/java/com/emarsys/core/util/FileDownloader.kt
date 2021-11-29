package com.emarsys.core.util

import android.content.Context
import android.webkit.URLUtil
import com.emarsys.core.Mockable
import kotlinx.coroutines.android.HandlerDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlinx.coroutines.flow.flow as flow


@Mockable
class FileDownloader(
    private val context: Context
) {
    private companion object {
        private const val BUFFER_SIZE = 4096
        private const val DELAY_TIME = 3000L
    }

    fun download(path: String, retryCount: Long = 0): String? {
        var result: String?
        if (URLUtil.isHttpsUrl(path)) {
            runBlocking {
                result = if (retryCount > 0) {
                    downloadToFlow(path).retry(retryCount) {
                        delay(DELAY_TIME)
                        it.printStackTrace()
                        it is Exception
                    }.single()
                } else {
                    downloadToFlow(path).singleOrNull()
                }
            }
        } else {
            result = null
        }
        return result
    }

    private fun downLoadFromInputStream(path: String): String? {
        val resultFile = createCacheFile()
        return inputStreamFromUrl(path).use { inputStream ->
            if (inputStream != null) {
                FileOutputStream(resultFile, false).use { outputStream ->
                    var bytesRead: Int
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
                resultFile.toURI().toURL().path
            } else null
        }
    }

    private suspend fun downloadToFlow(path: String): Flow<String?> {
        return flow {
            val downloadedFileUrl = downLoadFromInputStream(path)
            emit(downloadedFileUrl)
        }
    }

    private fun createCacheFile(): File {
        val cacheFolder = context.cacheDir
        val fileName = UUID.randomUUID().toString()
        val resultFile = File(cacheFolder, fileName)
        resultFile.createNewFile()
        return resultFile
    }

    fun delete(path: String) {
        val file = File(path)
        require(file.exists()) { "File $path does not exists." }
        file.delete()
    }

    fun readFileIntoString(fileUrl: String): String {
        BufferedReader(FileReader(fileUrl)).use {
            return it.readToString()
        }
    }

    fun readURLIntoString(url: String): String? {
        inputStreamFromUrl(url)?.bufferedReader().use {
            return it?.readToString()
        }
    }

    private fun BufferedReader.readToString(): String {
        val stringBuilder = StringBuilder()
        val lineSeparator = System.getProperty("line.separator")
        var line: String?

        while (this.readLine().also { line = it } != null) {
            stringBuilder.append(line)
            stringBuilder.append(lineSeparator)
        }
        stringBuilder.deleteCharAt(stringBuilder.length - 1)
        return stringBuilder.toString()
    }

    fun inputStreamFromUrl(path: String): InputStream? {
        return try {
            val url = URL(path)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            connection.inputStream
        } catch (ignored: IOException) {
            null
        }
    }
}