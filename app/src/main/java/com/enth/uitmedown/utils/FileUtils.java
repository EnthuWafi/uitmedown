package com.enth.uitmedown.utils;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * Copies the image from the Uri to a temporary file in the cache directory.
     * We need Context to access the ContentResolver and CacheDir.
     */
    public static File getFileFromUri(Context context, Uri uri) {
        try {
            // Create a temporary file in the app's cache folder
            File file = new File(context.getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");

            // Open the input stream from the URI
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            // Create the output stream to the new file
            OutputStream outputStream = new FileOutputStream(file);

            // Copy bytes
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}