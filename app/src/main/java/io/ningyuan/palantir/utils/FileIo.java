package io.ningyuan.palantir.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileIo {
    public static String getFilenameFromContentUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String filename = cursor.getString(nameIndex);
        cursor.close();
        return filename;
    }

    public static File cacheFileFromContentUri(Context context, Uri uri, String suffix) throws IOException {
        String cacheFileName = String.valueOf(System.currentTimeMillis());
        File cacheFile = File.createTempFile(cacheFileName, suffix, context.getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(cacheFile);
        IOUtils.copy(context.getContentResolver().openInputStream(uri), outputStream);
        outputStream.close();
        return cacheFile;
    }
}