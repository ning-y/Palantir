package io.ningyuan.palantir.utils;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.ningyuan.palantir.R;
import io.ningyuan.palantir.SceneformActivity;

public class PdbToObj {
    private static final String TAG = String.format("%s:%s", SceneformActivity.TAG, PdbToObj.class.getSimpleName());
    private static final String DAT_DIR_REL_PATH = "scripts/vmd/";
    private static final String[] DAT_FILES = new String[]{
            "atomselmacros.dat", "colordefs.dat", "materials.dat", "restypes.dat"
    };

    /**
     * Copies the VMD binary from android assets into internal storage
     */
    public static void initVmd(Context context) {
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
                Log.i(TAG, String.format("vmd copied from assets to internal storage at %s.", vmd.getCanonicalPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Found vmd in internal storage.");
        }
    }

    public static void initDat(Context context) {
        Log.i(TAG, String.format("Looking for %s in internal storage...", DAT_DIR_REL_PATH));
        // TODO: fix java.lang.IllegalArgumentException: File files/scripts/vmd/ contains a path separator
        File datDir = new File(context.getFilesDir(), DAT_DIR_REL_PATH);

        if (!datDir.exists()) {
            Log.i(TAG, String.format("%s not found in internal storage. Running mkdirs...", DAT_DIR_REL_PATH));
            datDir.mkdirs();
        }

        for (String datFileName : DAT_FILES) {
            try {
                Log.i(TAG, String.format("Looking for %s in internal storage...", datFileName));
                File datFile = new File(datDir.getCanonicalPath(), datFileName);
                if (datFile.exists()) { continue; }

                Log.i(TAG, String.format("%s not found in internal storage. Copying from assets to %s...", datFileName, datFile.getCanonicalPath()));
                InputStream streamAssets = context.getAssets().open(datFileName);
                FileOutputStream streamInternal = new FileOutputStream(datFile);
                IOUtils.copy(streamAssets, streamInternal);
                streamAssets.close();
                streamInternal.close();
                Log.i(TAG, String.format("%s copied from assets to internal storage.", datFileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File pdbFileToObjFile(Context context, File pdbFile) throws IOException{
        File[] tclScriptFiles = makeTclScript(context, pdbFile);
        String tclScriptPath = tclScriptFiles[0].getCanonicalPath();
        File objFile = tclScriptFiles[1];

        Log.d(TAG, String.format(
                "tclFile at %s:\n%s",
                tclScriptPath,
                IOUtils.toString(new FileInputStream(tclScriptFiles[0]), StandardCharsets.UTF_8)));

        runVmd(context, new String[]{"-dispdev", "none", "-e", tclScriptPath});

        Log.d(TAG, String.format(
                "pdbFileToObjFile concludes with objFile.exists(): %b",
                objFile.exists()
        ));

        return objFile;
    }

    private static File[] makeTclScript(Context context, File pdbFile) throws IOException {
        String template = context.getResources().getString(R.string.tcl_script_template);
        File objFile = File.createTempFile(pdbFile.getName(), ".obj", context.getCacheDir());
        String tclScript = String.format(template, pdbFile.getCanonicalPath(), objFile.getCanonicalPath());

        File tclFile = File.createTempFile(pdbFile.getName(), ".tcl", context.getCacheDir());
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(tclFile));
        outputStreamWriter.write(tclScript);
        outputStreamWriter.close();

        return new File[]{tclFile, objFile};
    }

    private static void runVmd(Context context, String[] args) throws IOException {
        File vmd = context.getFileStreamPath("vmd");
        String[] envVars = new String[]{String.format("VMDDIR=%s", context.getFilesDir().getCanonicalPath())};
        String[] command = new String[args.length+1];
        command[0] = vmd.getAbsolutePath();
        System.arraycopy(args, 0, command, 1, args.length);

        Log.d(TAG, String.format("runVmd with: %s and %s", Arrays.toString(command), Arrays.toString(envVars)));
        Process process = Runtime.getRuntime().exec(command, envVars);

        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();

        String line;
        BufferedReader outReader = new BufferedReader(new InputStreamReader(stdout));
        while ((line = outReader.readLine()) != null) {
            Log.d(TAG, line);
        }

        Log.d(TAG, "call 1");

        BufferedReader errReader = new BufferedReader(new InputStreamReader(stderr));

        Log.d(TAG, "call 2");

        while ((line = errReader.readLine()) != null) {
            Log.d(TAG, line);
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
