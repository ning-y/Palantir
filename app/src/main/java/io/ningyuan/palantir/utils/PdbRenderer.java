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
    /* Locations of various important files in the androids assets folder, which need to be copied
    onto the devices' internal storage (files directory) */
    private static final String VMD_BIN_FILE = "arm64-v8a/vmd";
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
        File vmd = new File(context.getFilesDir().getCanonicalPath(), "vmd/vmd");
        LinkedList<String> command = new LinkedList<>(Arrays.asList(args));
        command.addFirst(vmd.getCanonicalPath());
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("VMDDIR", context.getFilesDir().getCanonicalPath());
        processBuilder.environment().put("TCL_LIBRARY", context.getFilesDir().getCanonicalPath());
        Log.d(TAG, "call -1");
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
        String targetFilePath = new File(context.getFilesDir(), "vmd/vmd").getCanonicalPath();
        FileIo.copyAssetsFileToInternalStorage(context, VMD_BIN_FILE, targetFilePath, true);
    }

    private static void initDat(Context context) throws IOException {
        File auxDirFile = new File(context.getFilesDir(), VMD_AUX_DIR);

        for (String auxFilePath : VMD_AUX_FILES) {
            String targetFilePath = new File(auxDirFile.getCanonicalPath(), auxFilePath)
                    .getCanonicalPath();
            FileIo.copyAssetsFileToInternalStorage(context, auxFilePath, targetFilePath);
        }
    }

    private static void initTcl(Context context) throws IOException {
        String targetFilePath = new File(context.getFilesDir(), "init.tcl").getCanonicalPath();
        FileIo.copyAssetsFileToInternalStorage(context, TCL_AUX_FILE, targetFilePath);
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
}
