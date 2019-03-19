package io.ningyuan.palantir.utils;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.ningyuan.palantir.SceneformActivity;

public class PdbToObj {
    private static final String TAG = String.format("%s:%s", SceneformActivity.TAG, PdbToObj.class.getSimpleName());

    /**
     * Copies the VMD binary from android assets into internal storage
     */
    public static void init(Context context) {
        Log.i(TAG, "Looking for vmd in internal storage...");
        File vmd = context.getFileStreamPath("vmd");
        if (!vmd.exists()) {
            try {
                Log.w(TAG, "vmd not found in internal storage. Copying from assets...");
                InputStream assets = context.getAssets().open("arm64-v8a/vmd");
                FileOutputStream internal = new FileOutputStream(vmd);
                IOUtils.copy(assets, internal);
                assets.close();
                internal.close();
                vmd.setExecutable(true);
                Log.i(TAG, "vmd copied from assets to internal storage.");
            } catch (IOException e) { }
        } else {
            Log.i(TAG, "Found vmd in internal storage.");
        }
    }

    public static void logVmdHelp(Context context) {
        Log.d(TAG, "Starting logVmdHelp");
        File vmd = context.getFileStreamPath("vmd");
        Log.d(TAG, String.format("vmd.exists(): %b", vmd.exists()));

        try {
            Process process = Runtime.getRuntime().exec(new String[]{vmd.getAbsolutePath(), "--help"});
            InputStream stdout = process.getInputStream();
            InputStream stderr = process.getErrorStream();

            String line;
            BufferedReader outReader = new BufferedReader(new InputStreamReader(stdout));
            while ((line = outReader.readLine()) != null) {
                Log.d(TAG, line);
            }

            BufferedReader errReader = new BufferedReader(new InputStreamReader(stderr));
            while ((line = errReader.readLine()) != null) {
                Log.d(TAG, line);
            }

            Log.d(TAG, "Done with logVmdHelp");
        } catch (IOException e) {
            Log.e(TAG, "Something went wrong with VMD!");
        }
    }
}
