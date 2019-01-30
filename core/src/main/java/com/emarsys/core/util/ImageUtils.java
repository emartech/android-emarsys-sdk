package com.emarsys.core.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.webkit.URLUtil;

import com.emarsys.core.device.DeviceInfo;

import java.io.File;

public class ImageUtils {

    public static Bitmap loadBitmap(Context context, String imageUrl) {
        Bitmap result = null;
        if (ImageUtils.isImageUrlValid(imageUrl)) {
            String fileUrl = downloadImage(context, imageUrl);
            result = loadBitmap(fileUrl, Integer.MAX_VALUE);
            if (fileUrl != null && isRemoteUrl(imageUrl)) {
                FileUtils.delete(fileUrl);
            }
        }
        return result;
    }

    public static Bitmap loadOptimizedBitmap(Context context, String imageUrl, DeviceInfo deviceInfo) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

        Bitmap result = null;
        if (ImageUtils.isImageUrlValid(imageUrl)) {
            String fileUrl = downloadImage(context, imageUrl);
            result = loadBitmap(fileUrl, deviceInfo.getDisplayMetrics().widthPixels);
            if (fileUrl != null && isRemoteUrl(imageUrl)) {
                FileUtils.delete(fileUrl);
            }
        }
        return result;
    }

    private static boolean isImageUrlValid(String imageUrl) {
        boolean result = true;
        if (imageUrl == null) {
            result = false;
        } else if (!URLUtil.isHttpsUrl(imageUrl)) {
            result = new File(imageUrl).exists();
        }
        return result;
    }

    private static String downloadImage(Context context, String imageUrl) {
        String fileUrl = imageUrl;
        if (isRemoteUrl(imageUrl)) {
            fileUrl = FileUtils.download(context, imageUrl);
        }
        return fileUrl;
    }

    private static boolean isRemoteUrl(String imageUrl) {
        return URLUtil.isHttpsUrl(imageUrl);
    }

    private static Bitmap loadBitmap(String imageFileUrl, int width) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFileUrl, options);

        options.inSampleSize = calculateInSampleSize(options, width);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imageFileUrl, options);
    }

    static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        final int width = options.outWidth;
        int inSampleSize = 1;
        while (reqWidth <= width / inSampleSize) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

}
