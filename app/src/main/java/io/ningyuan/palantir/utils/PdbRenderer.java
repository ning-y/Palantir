package io.ningyuan.palantir.utils;

import android.content.Context;
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
    // TCL needs an external file, init.tcl to work
    private static final String TCL_AUX_FILE = "init.tcl";
    // VMD needs a number of auxiliary files to work
    private static final String VMD_AUX_DIR = "scripts/vmd/";
    private static final String[] VMD_AUX_FILES = {
            "atomselect.tcl", "atomselmacros.dat", "biocore.tcl", "colordefs.dat",
            "graphlabels.tcl", "hotkeys.tcl", "loadplugins.tcl", "logfile.tcl",
            "materials.dat", "restypes.dat", "vectors.tcl", "vmdinit.tcl"};

    private SceneformActivity sceneformActivity;

    public PdbRenderer(SceneformActivity sceneformActivity) {
        this.sceneformActivity = sceneformActivity;
    }

    @Override
    protected void onPreExecute() {
        sceneformActivity.updateModelNameTextView("Importing .pdb file...");
    }

    @Override
    protected File doInBackground(Uri... uri) {
        try { initVmd(sceneformActivity); } catch (IOException e) { e.printStackTrace(); }
        try { initDat(sceneformActivity); } catch (IOException e) { e.printStackTrace(); }
        try { initTcl(sceneformActivity); } catch (IOException e) { e.printStackTrace(); }

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

    private static File pdbFileToObjFile(Context context, File pdbFile) throws IOException{
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
        File vmd = new File(context.getFilesDir().getCanonicalPath(), "vmd/vmd");
        LinkedList<String> command = new LinkedList<>(Arrays.asList(args));
        command.addFirst( vmd.getCanonicalPath());
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("VMDDIR", context.getFilesDir().getCanonicalPath());
        processBuilder.environment().put("TCL_LIBRARY", context.getFilesDir().getCanonicalPath());
        Process process = processBuilder.start();
        // Log.d(TAG, String.format("runVmd with: %s and %s", Arrays.toString(command), Arrays.toString(envVars)));

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
        Log.i(TAG, "Looking for vmd in internal storage...");
        File vmdDir = new File(context.getFilesDir(), "vmd");

        if (!vmdDir.exists()) {
            Log.i(TAG, String.format("%s not found in internal storage. Running mkdirs...", vmdDir.getCanonicalPath()));
            vmdDir.mkdirs();
        }

        File vmd = new File(context.getFilesDir().getCanonicalPath(), "vmd/vmd");

        if (!vmd.exists()) {
            Log.w(TAG, "vmd not found in internal storage. Copying from assets...");
            InputStream assets = context.getAssets().open("arm64-v8a/vmd");
            FileOutputStream internal = new FileOutputStream(vmd);
            IOUtils.copy(assets, internal);
            assets.close();
            internal.close();
            vmd.setExecutable(true);
            Log.i(TAG, String.format("vmd copied from assets to internal storage at %s.", vmd.getCanonicalPath()));
        } else {
            Log.i(TAG, "Found vmd in internal storage.");
        }
    }

    private static void initDat(Context context) throws IOException {
        Log.i(TAG, String.format("Looking for %s in internal storage...", VMD_AUX_DIR));
        // TODO: fix java.lang.IllegalArgumentException: File files/scripts/vmd/ contains a path separator
        File datDir = new File(context.getFilesDir(), VMD_AUX_DIR);

        if (!datDir.exists()) {
            Log.i(TAG, String.format("%s not found in internal storage. Running mkdirs...", VMD_AUX_DIR));
            datDir.mkdirs();
        }

        for (String datFileName : VMD_AUX_FILES) {
            File datFile = new File(datDir.getCanonicalPath(), datFileName);
            Log.i(TAG, String.format("Looking for %s in internal storage...", datFile.getCanonicalPath()));
            if (datFile.exists()) { continue; }

            Log.i(TAG, String.format("%s not found in internal storage. Copying from assets to %s...", datFileName, datFile.getCanonicalPath()));
            InputStream streamAssets = context.getAssets().open(datFileName);
            FileOutputStream streamInternal = new FileOutputStream(datFile);
            IOUtils.copy(streamAssets, streamInternal);
            streamAssets.close();
            streamInternal.close();
            Log.i(TAG, String.format("%s copied from assets to internal storage.", datFileName));
        }
    }

    private static void initTcl(Context context) throws IOException {
        File internalTclInit = new File(context.getFilesDir().getCanonicalPath(), "init.tcl");  // TODO temporarily replacing above only
        Log.i(TAG, String.format("Looking for %s in internal storage...", internalTclInit.getCanonicalPath()));
        if (internalTclInit.exists()) {
            Log.i(TAG, String.format("%s found.", internalTclInit.getCanonicalPath()));
            return;
        }

        Log.i(TAG, String.format("%s not found. Creating...", internalTclInit.getCanonicalPath()));
        InputStream streamAssets = context.getAssets().open(TCL_AUX_FILE);
        FileOutputStream streamInternal = new FileOutputStream(internalTclInit);
        IOUtils.copy(streamAssets, streamInternal);
        streamAssets.close();
        streamInternal.close();
        Log.i(TAG, String.format("Created %s", internalTclInit.getCanonicalPath()));
    }
}
