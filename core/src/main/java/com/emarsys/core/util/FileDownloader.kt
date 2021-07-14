package com.emarsys.core.util

import android.content.Context
import android.webkit.URLUtil
import com.emarsys.core.Mockable
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Mockable
class FileDownloader(private val context: Context) {
    private companion object {
        private const val BUFFER_SIZE = 4096
    }

    fun download(path: String): String? {
        return if (URLUtil.isHttpsUrl(path)) {
            val resultFile = createCacheFile()
            inputStreamFromUrl(path)?.use { inputStream ->
                FileOutputStream(resultFile, false).use { outputStream ->
                    var bytesRead: Int
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
                resultFile.toURI().toURL().path
            }
        } else {
            null
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