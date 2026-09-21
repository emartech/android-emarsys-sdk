package com.emarsys.core.util.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationUtils {

    public static byte[] serializableToBlob(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Exception while converting object to blob", e);
        }
    }

    public static Object blobToSerializable(byte[] blob) throws SerializationException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream ois = new ObjectInputStream(bais);
            bais.close();
            return ois.readObject();
        } catch (Exception e) {
            throw new SerializationException();
        }
    }

}
