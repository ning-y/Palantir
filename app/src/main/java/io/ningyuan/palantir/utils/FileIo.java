package io.ningyuan.palantir.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.ningyuan.palantir.SceneformActivity;

/**
 * Convenience methods for file I/O operations.
 */
public class FileIo {
    private static final String TAG = String.format("%s:%s", SceneformActivity.TAG, FileIo.class.getSimpleName());

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

    /**
     * Copies a file from the android assets folder onto the internal (files) storage of the device.
     * Does nothing, if the file at {@param targetPath} already exists.
     *
     * @param assetPath  relative path from the assets folder of the source asset file
     *                   i.e. app/src/main/assets/${assetPath}
     * @param targetPath relative path from the android file dir of the target location
     *                   i.e. /data/data/io.ningyuan.palantir/files/${internalPath}
     * @return the resulting target {@link File}
     */
    public static File copyAssetsFileToInternalStorage(Context context, String assetPath, String targetPath) throws IOException {
        return copyAssetsFileToInternalStorage(context, assetPath, targetPath, false);
    }

    /**
     * Copies a file from the android assets folder onto the internal (files) storage of the device.
     * Does nothing, if the file at {@param targetPath} already exists or if the file at
     * {@param assetPath} is a directory
     *
     * @param assetPath           relative path from the assets folder of the source asset file
     *                            i.e. app/src/main/assets/${assetPath}
     * @param targetPath          relative path from the android file dir of the target location
     *                            i.e. /data/data/io.ningyuan.palantir/files/${internalPath}
     * @param shouldSetExecutable if the target file should be {@link File#setExecutable(boolean)}
     * @return the resulting target {@link File}
     */
    public static File copyAssetsFileToInternalStorage(Context context, String assetPath, String targetPath, boolean shouldSetExecutable) throws IOException {
        File targetFile = new File(targetPath);

        if (context.getAssets().list(assetPath).length != 0) {
            log("%s is a directory. Recursively checking its contents.", assetPath);
            return null;
        }

        if (targetFile.exists()) {
            log("%s found.", targetFile.getCanonicalPath());
            return null;
        }

        log("%s not found. Preparing to create.", targetFile.getCanonicalPath());
        targetFile.getParentFile().mkdirs();
        InputStream assetPathStream = context.getAssets().open(assetPath);
        FileOutputStream targetFileStream = new FileOutputStream(targetFile);
        IOUtils.copy(assetPathStream, targetFileStream);

        assetPathStream.close();
        targetFileStream.close();

        if (shouldSetExecutable) targetFile.setExecutable(true);

        log("Copied %s from assets to %s.", assetPath, targetFile.getCanonicalPath());
        return targetFile;
    }

    public static android.net.Uri javaUriToAndroidUri(java.net.URI javaUri) {
        return android.net.Uri.parse(javaUri.toString());
    }

    private static void log(String template, String... args) {
        Log.d(TAG, String.format(template, args));
    }
}