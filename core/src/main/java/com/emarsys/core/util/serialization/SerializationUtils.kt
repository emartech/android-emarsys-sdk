package com.emarsys.core.util.serialization

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object SerializationUtils {

    @JvmStatic
    fun serializableToBlob(`object`: Any?): ByteArray {
        return try {
            ByteArrayOutputStream().use { baos ->
                ObjectOutputStream(baos).use { oos ->
                    oos.writeObject(`object`)
                }
                baos.toByteArray()
            }
        } catch (e: Exception) {
            throw RuntimeException("Exception while converting object to blob", e)
        }
    }

    @JvmStatic
    @Throws(SerializationException::class)
    fun blobToSerializable(blob: ByteArray): Any? {
        return try {
            ByteArrayInputStream(blob).use { bais ->
                ObjectInputStream(bais).use { ois ->
                    ois.readObject()
                }
            }
        } catch (e: Exception) {
            throw SerializationException()
        }
    }
}
