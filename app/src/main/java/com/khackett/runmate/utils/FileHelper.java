package com.khackett.runmate.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class to manage image files.
 * Courtesy of: https://github.com/treehouse/treehouse_android_utilities
 */
public class FileHelper {

    public static final String TAG = FileHelper.class.getSimpleName();

    public static final int SHORT_SIDE_TARGET = 1280;

    /**
     * Method to convert a file from a URI to a byte array. It accepts regular URIs as well as content URIs.
     * @param context
     * @param uri
     * @return
     */
    public static byte[] getByteArrayFromFile(Context context, Uri uri) {
        byte[] fileBytes = null;
        InputStream inStream = null;
        ByteArrayOutputStream outStream = null;
        Log.d(TAG, "in the getByteArrayFromFile() method");
        Log.d(TAG, "in the getByteArrayFromFile() method - uri = " + uri);
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try {
                Log.d(TAG, "entering try to set inputstream");
                inStream = context.getContentResolver().openInputStream(uri);
                outStream = new ByteArrayOutputStream();

                byte[] bytesFromFile = new byte[1024 * 1024]; // buffer size (1 MB)
                int bytesRead = inStream.read(bytesFromFile);
                while (bytesRead != -1) {
                    outStream.write(bytesFromFile, 0, bytesRead);
                    bytesRead = inStream.read(bytesFromFile);
                }

                fileBytes = outStream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    inStream.close();
                    outStream.close();
                } catch (IOException e) { /*( Intentionally blank */ }
            }
        } else {
            try {
                File file = new File(uri.getPath());
                FileInputStream fileInput = new FileInputStream(file);
                fileBytes = IOUtils.toByteArray(fileInput);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return fileBytes;
    }

    /**
     * Method to reduce a byte array for an image file to a smaller PNG.
     * @param imageData
     * @return
     */
    public static byte[] reduceImageForUpload(byte[] imageData) {
        Bitmap bitmap = ImageResizer.resizeImageMaintainAspectRatio(imageData, SHORT_SIDE_TARGET);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] reducedData = outputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
            // Intentionally blank
        }
        return reducedData;
    }

    /**
     * Method to create a file name using file type or the MIME type for both regular URIs and content URIs.
     * @param context
     * @param uri
     * @param fileType
     * @return
     */
    public static String getFileName(Context context, Uri uri, String fileType) {
        String fileName = "uploaded_file.";

        if (fileType.equals(ParseConstants.TYPE_IMAGE)) {
            fileName += "png";
        } else {
            // For video, we want to get the actual file extension
            if (uri.getScheme().equals("content")) {
                // do it using the mime type
                String mimeType = context.getContentResolver().getType(uri);
                int slashIndex = mimeType.indexOf("/");
                String fileExtension = mimeType.substring(slashIndex + 1);
                fileName += fileExtension;
            } else {
                fileName = uri.getLastPathSegment();
            }
        }

        return fileName;
    }
}