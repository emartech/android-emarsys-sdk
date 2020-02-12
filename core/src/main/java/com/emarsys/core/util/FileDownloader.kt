package com.emarsys.core.util

import android.content.Context
import android.webkit.URLUtil
import com.emarsys.core.Mockable
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Mockable
class FileDownloader(private val context: Context?) {
    private companion object {
        private const val BUFFER_SIZE = 4096
    }

    fun download(path: String?): String? {
        var result: String? = null
        if (path != null && context != null && URLUtil.isHttpsUrl(path)) {
            try {
                val cacheFolder = context.cacheDir
                val fileName = UUID.randomUUID().toString()
                val resultFile = File(cacheFolder, fileName)
                resultFile.createNewFile()
                val inputStream = inputStreamFromUrl(path)
                val fos = FileOutputStream(resultFile, false)
                var bytesRead: Int
                val buffer = ByteArray(BUFFER_SIZE)
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    fos.write(buffer, 0, bytesRead)
                }
                result = resultFile.toURI().toURL().path
                fos.close()
                inputStream.close()
            } catch (ignored: IOException) {
            }
        }
        return result
    }

    fun delete(path: String) {
        val file = File(path)
        require(file.exists()) { "File $path does not exists." }
        file.delete()
    }

    fun readFileIntoString(fileUrl: String?): String? {
        var result: String? = null
        if (fileUrl != null) {
            try {
                val reader = BufferedReader(FileReader(fileUrl))
                val stringBuilder = StringBuilder()
                var line: String?
                val ls = System.getProperty("line.separator")
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                    stringBuilder.append(ls)
                }
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                reader.close()
                result = stringBuilder.toString()
            } catch (ignored: IOException) {
            }
        }
        return result
    }

    fun readURLIntoString(url: String?): String? {
        var result: String? = null
        try {
            val reader = BufferedReader(InputStreamReader(URL(url).openStream()))
            val stringBuilder = StringBuilder()
            val lineSeparator = System.getProperty("line.separator")
            var line = reader.readLine()
            if (line != null) {
                stringBuilder.append(line)
            }
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(lineSeparator)
                stringBuilder.append(line)
            }
            reader.close()
            result = stringBuilder.toString()
        } catch (ignored: IOException) {
        }
        return result
    }

    fun inputStreamFromUrl(path: String?): InputStream {
        val url = URL(path)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        return connection.inputStream
    }
}