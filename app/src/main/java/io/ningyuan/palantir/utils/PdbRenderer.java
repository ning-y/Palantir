package io.ningyuan.palantir.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.LinkedList;

import io.ningyuan.palantir.R;
import io.ningyuan.palantir.SceneformActivity;

import static io.ningyuan.palantir.utils.FileIo.cacheFileFromContentUri;

public class PdbRenderer extends AsyncTask<Uri, Void, File> {
    private static final String TAG = String.format("%s:%s", SceneformActivity.TAG, PdbRenderer.class.getSimpleName());

    /* Locations of various important files in the androids assets folder, which need to be copied
    onto the devices' internal storage (files directory) */
    private static final String ASSET_TCL_AUX_DIR = "tcl_aux";
    private static final String ASSET_VMD_AUX_DIR = "vmd_aux";
    private static final String ASSET_VMD_BIN = "arm64-v8a/vmd";
    private static final String INTERNAL_TCL_AUX_DIR = "tcl_libraries";
    private static final String INTERNAL_VMD_AUX_DIR = "scripts/vmd/";
    private static final String INTERNAL_VMD_BIN = "vmd/vmd";

    private SceneformActivity sceneformActivity;

    public PdbRenderer(SceneformActivity sceneformActivity) {
        this.sceneformActivity = sceneformActivity;
    }

    private static File pdbFileToObjFile(Context context, File pdbFile) throws IOException {
        File[] tclScriptFiles = makeTclScript(context, pdbFile);
        String tclScriptPath = tclScriptFiles[0].getCanonicalPath();
        File objFile = tclScriptFiles[1];

        Log.d(TAG, String.format(
                "tclFile at %s:\n%s",
                tclScriptPath,
                IOUtils.toString(new FileInputStream(tclScriptFiles[0]), StandardCharsets.UTF_8)));

        runVmd(context, "-dispdev", "none", "-e", tclScriptPath);

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

    private static void runVmd(Context context, String... args) throws IOException {
        // Prepare the VMD command and args
        File vmd = new File(context.getFilesDir().getCanonicalPath(), INTERNAL_VMD_BIN);
        LinkedList<String> command = new LinkedList<>(Arrays.asList(args));
        command.addFirst(vmd.getCanonicalPath());
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // Prepare the environment variables
        /** {@link #INTERNAL_VMD_AUX_DIR} is set such that VMDDIR is the {@link Context#getFilesDir()} */
        processBuilder.environment().put("VMDDIR", context.getFilesDir().getCanonicalPath());
        String tclAuxDir = new File(context.getFilesDir(), INTERNAL_TCL_AUX_DIR).getCanonicalPath();
        processBuilder.environment().put("TCL_LIBRARY", tclAuxDir);

        Process process = processBuilder.start();
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();

        Log.d(TAG, "call 0");

        String line;
        BufferedReader outReader = new BufferedReader(new InputStreamReader(stdout));
        while ((line = outReader.readLine()) != null) {
            Log.d(TAG, line);
        }

        Log.d(TAG, "call 1");
        BufferedReader errReader = new BufferedReader(new InputStreamReader(stderr));
        while ((line = errReader.readLine()) != null) {
            Log.d(TAG, line);
        }
    }

    /**
     * Copies the VMD binary from android assets into internal storage
     */
    private static void initVmd(Context context) throws IOException {
        String targetFilePath = new File(context.getFilesDir(), INTERNAL_VMD_BIN).getCanonicalPath();
        FileIo.copyAssetToInternalStorage(context, ASSET_VMD_BIN, targetFilePath, true);
    }

    private static void initVmdAux(Context context) throws IOException {
        File targetDir = new File(context.getFilesDir(), INTERNAL_VMD_AUX_DIR);
        File assetVmdDir = new File(ASSET_VMD_AUX_DIR);

        AssetManager assetManager = context.getAssets();
        for (String assetName : assetManager.list(ASSET_VMD_AUX_DIR)) {
            String targetPath = new File(targetDir.getCanonicalPath(), assetName).getCanonicalPath();
            String assetPath = new File(assetVmdDir, assetName).getPath();
            FileIo.copyAssetToInternalStorage(context, assetPath, targetPath);
        }
    }

    private static void initTclAux(Context context) throws IOException {
        File targetDir = new File(context.getFilesDir(), INTERNAL_TCL_AUX_DIR);
        File assetTclDir = new File(ASSET_TCL_AUX_DIR);

        // TODO this is a bigger problem than I thought. need to copy recursively.
        AssetManager assetManager = context.getAssets();
        for (String assetName : assetManager.list(ASSET_TCL_AUX_DIR)) {
            String targetPath = new File(targetDir.getCanonicalPath(), assetName).getCanonicalPath();
            String assetPath = new File(assetTclDir, assetName).getPath();
            FileIo.copyAssetToInternalStorage(context, assetPath, targetPath);
        }
    }

    @Override
    protected void onPreExecute() {
        sceneformActivity.updateModelNameTextView("Importing .pdb file...");
    }

    @Override
    protected File doInBackground(Uri... uri) {
        try { initVmd(sceneformActivity); } catch (IOException e) { e.printStackTrace(); }
        try { initVmdAux(sceneformActivity); } catch (IOException e) { e.printStackTrace(); }
        try { initTclAux(sceneformActivity); } catch (IOException e) { e.printStackTrace(); }

        try {
            File pdbFile = cacheFileFromContentUri(sceneformActivity, uri[0], ".pdb");
            File objFile = pdbFileToObjFile(sceneformActivity, pdbFile);
            File glbFile = ObjRenderer.objFileToGlbFile(sceneformActivity, objFile);
            return glbFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(File glbFile) {
        sceneformActivity.updateModelRenderable(glbFile.getName(), glbFile);
    }
}
