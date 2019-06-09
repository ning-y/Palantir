package io.ningyuan.palantir.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.ningyuan.palantir.MainActivity;

/**
 * Convenience methods for file I/O operations.
 */
public class FileIo {
    private static final String TAG = String.format("%s:%s", MainActivity.TAG, FileIo.class.getSimpleName());

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

    public static File copyAssetToInternalStorage(Context context, String assetPath, String targetPath, boolean shouldSetExecutable) throws IOException {
        return isAssetPathDir(context, assetPath)
                ? copyAssetDirToInteralStorage(context, assetPath, targetPath)
                : copyAssetFileToInternalStorage(context, assetPath, targetPath, shouldSetExecutable);
    }

    public static File copyAssetToInternalStorage(Context context, String assetPath, String targetPath) throws IOException {
        return copyAssetToInternalStorage(context, assetPath, targetPath, false);
    }


    private static File copyAssetDirToInteralStorage(Context context, String assetPath, String targetPath) throws IOException {
        for (String relAssetPath : context.getAssets().list(assetPath)) {
            // relPath is a path to the file in relation to the asset root.
            // each relPath differs only in the file name (or directory name)
            // and their target copy-to's is within this given targetPath (but with targetPath as a directory)
            String newTargetPath = new File(new File(targetPath), relAssetPath).getCanonicalPath();
            String newAssetPath = new File(new File(assetPath), relAssetPath).getPath();
            copyAssetToInternalStorage(context, newAssetPath, newTargetPath);
        }

        return new File(targetPath);
    }

    private static File copyAssetFileToInternalStorage(Context context, String assetPath, String targetPath, boolean shouldSetExecutable) throws IOException {
        File targetFile = new File(targetPath);

        // file already exists---nothing to do
        if (targetFile.exists()) return targetFile;

        // file does not exist; create it's containing parent dirs, then copy it over
        targetFile.getParentFile().mkdirs();
        InputStream inputStream = context.getAssets().open(assetPath);
        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
        IOUtils.copy(inputStream, fileOutputStream);

        inputStream.close();
        fileOutputStream.close();

        if (shouldSetExecutable) targetFile.setExecutable(true);

        return targetFile;
    }

    /**
     * @param context
     * @param assetPath
     * @return if the {@param assetPath} is pointing to a directory (instead of a file)
     */
    private static boolean isAssetPathDir(Context context, String assetPath) throws IOException {
        return context.getAssets().list(assetPath).length != 0;
    }

    public static android.net.Uri javaUriToAndroidUri(java.net.URI javaUri) {
        return android.net.Uri.parse(javaUri.toString());
    }
}