package io.ningyuan.palantir.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Convenience methods for file I/O operations.
 */
public class FileIo {
    /**
     * From a content {@link Uri} (content://), obtain the real filename.
     *
     * @param context a {@link Context} with which to use {@link Context#getContentResolver()} and
     *                therefore {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)}.
     * @param uri     the content {@link Uri}
     * @return Real name of the file.
     */
    public static String getFilenameFromContentUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String filename = cursor.getString(nameIndex);
        cursor.close();
        return filename;
    }

    /**
     * From a {@link Uri}, save the contents as a cache file (as in, in {@link Context#getCacheDir()}.
     *
     * @param context
     * @param uri
     * @param suffix
     * @return the resulting cache {@link File}
     * @throws IOException
     */
    public static File cacheFileFromContentUri(Context context, Uri uri, String suffix) throws IOException {
        String cacheFileName = String.valueOf(System.currentTimeMillis());
        File cacheFile = File.createTempFile(cacheFileName, suffix, context.getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(cacheFile);
        IOUtils.copy(context.getContentResolver().openInputStream(uri), outputStream);
        outputStream.close();
        return cacheFile;
    }

    public static android.net.Uri javaUriToAndroidUri(java.net.URI javaUri) {
        return android.net.Uri.parse(javaUri.toString());
    }
}