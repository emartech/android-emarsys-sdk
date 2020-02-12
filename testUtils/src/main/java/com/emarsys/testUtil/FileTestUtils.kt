package com.emarsys.testUtil

import java.io.*

object FileTestUtils {
    fun writeToFile(input: String, filePath: String): Boolean {
        var success: Boolean
        try {
            val resultFile = File(filePath)
            success = resultFile.createNewFile()
            if (success) {
                val fos = FileOutputStream(resultFile, false)
                val outputStreamWriter = OutputStreamWriter(fos)
                outputStreamWriter.write(input)
                outputStreamWriter.close()
            }
        } catch (ignored: IOException) {
            success = false
        }
        return success
    }
}

fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}