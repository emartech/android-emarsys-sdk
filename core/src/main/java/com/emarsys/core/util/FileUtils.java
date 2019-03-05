package com.emarsys.core.util;

import android.content.Context;
import android.webkit.URLUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class FileUtils {

    private static final int BUFFER_SIZE = 4096;

    public static String download(Context context, String path) {
        String result = null;
        if (path != null && context != null && URLUtil.isHttpsUrl(path)) {
            try {
                File cacheFolder = context.getCacheDir();
                String fileName = UUID.randomUUID().toString();
                File resultFile = new File(cacheFolder, fileName);
                resultFile.createNewFile();
                InputStream inputStream = inputStreamFromUrl(path);
                FileOutputStream fos = new FileOutputStream(resultFile, false);
                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                result = resultFile.toURI().toURL().getPath();
                fos.close();
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
        return result;
    }

    public static void delete(String path) {
        Assert.notNull(path, "Path must not be null!");
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("File %s does not exists.", path));
        }
        file.delete();
    }

    public static String readFileIntoString(String fileUrl) {
        String result = null;
        if (fileUrl != null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileUrl));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                String ls = System.getProperty("line.separator");
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                reader.close();
                result = stringBuilder.toString();
            } catch (IOException ignored) {
            }
        }
        return result;
    }

    public static String readURLIntoString(String url) {
        String result = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String lineSeparator = System.getProperty("line.separator");

            String line = reader.readLine();

            if (line != null) {
                stringBuilder.append(line);
            }
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(lineSeparator);
                stringBuilder.append(line);
            }
            reader.close();
            result = stringBuilder.toString();
        } catch (IOException ignored) {
        }
        return result;
    }

    public static boolean writeToFile(String input, String filePath) {
        Assert.notNull(input, "Input must not be null!");
        Assert.notNull(filePath, "FilePath must not be null!");

        boolean success;
        try {
            File resultFile = new File(filePath);
            success = resultFile.createNewFile();
            if (success) {
                FileOutputStream fos = new FileOutputStream(resultFile, false);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
                outputStreamWriter.write(input);
                outputStreamWriter.close();
            }
        } catch (IOException ignored) {
            success = false;
        }

        return success;
    }

    static InputStream inputStreamFromUrl(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        return connection.getInputStream();
    }
}
