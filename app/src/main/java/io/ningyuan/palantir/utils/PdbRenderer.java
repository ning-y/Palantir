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

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.palantir.R;
import io.ningyuan.palantir.MainActivity;
import io.ningyuan.palantir.models.Molecule;

import static io.ningyuan.palantir.utils.FileIo.cacheFileFromContentUri;

public class PdbRenderer extends AsyncTask<Molecule, Void, Molecule> {
    private static final String TAG = String.format("%s:%s", MainActivity.TAG, PdbRenderer.class.getSimpleName());

    /* Locations of various important files in the androids assets folder, which need to be copied
    onto the devices' internal storage (files directory) */
    private static final String ASSET_MOLFILE_DIR = "arm64-v8a/molfile_plugins";
    private static final String ASSET_STRIDE_BIN = "arm64-v8a/stride";
    private static final String ASSET_TCL_AUX_DIR = "tcl_aux";
    private static final String ASSET_VMD_AUX_DIR = "vmd_aux";
    private static final String ASSET_VMD_BIN = "arm64-v8a/vmd";
    private static final String INTERNAL_MOLFILE_DIR = "plugins/LINUXAMD64/molfile";
    private static final String INTERNAL_STRIDE_BIN = "vmd/stride";
    private static final String INTERNAL_TCL_AUX_DIR = "tcl_libraries";
    private static final String INTERNAL_VMD_AUX_DIR = "scripts/vmd";
    private static final String INTERNAL_VMD_BIN = "vmd/vmd";

    private MainActivity mainActivity;

    public PdbRenderer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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
        String strideBinDir = new File(context.getFilesDir(), INTERNAL_STRIDE_BIN).getCanonicalPath();
        processBuilder.environment().put("STRIDE_BIN", strideBinDir);
        File strideTmpIn = File.createTempFile("stride", null, context.getCacheDir());
        strideTmpIn.delete();
        processBuilder.environment().put("STRIDE_TMP_IN", strideTmpIn.getCanonicalPath());
        File strideTmpOut = File.createTempFile("stride", null, context.getCacheDir());
        strideTmpOut.delete();
        processBuilder.environment().put("STRIDE_TMP_OUT", strideTmpOut.getCanonicalPath());

        Process process = processBuilder.start();
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
    }

    /**
     * Copies the VMD binary from android assets into internal storage
     */
    private static void initVmd(Context context) throws IOException {
        String targetFilePath = new File(context.getFilesDir(), INTERNAL_VMD_BIN).getCanonicalPath();
        FileIo.copyAssetToInternalStorage(context, ASSET_VMD_BIN, targetFilePath, true);
    }

    private static void initStride(Context context) throws IOException {
        String targetFilePath = new File(context.getFilesDir(), INTERNAL_STRIDE_BIN).getCanonicalPath();
        FileIo.copyAssetToInternalStorage(context, ASSET_STRIDE_BIN, targetFilePath, true);
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

        AssetManager assetManager = context.getAssets();
        for (String assetName : assetManager.list(ASSET_TCL_AUX_DIR)) {
            String targetPath = new File(targetDir.getCanonicalPath(), assetName).getCanonicalPath();
            String assetPath = new File(assetTclDir, assetName).getPath();
            FileIo.copyAssetToInternalStorage(context, assetPath, targetPath);
        }
    }

    private static void initMolfilePlugins(Context context) throws IOException {
        File targetDir = new File(context.getFilesDir(), INTERNAL_MOLFILE_DIR);
        File assetMolfileDir = new File(ASSET_MOLFILE_DIR);

        AssetManager assetManager = context.getAssets();
        for (String assetName : assetManager.list(ASSET_MOLFILE_DIR)) {
            String targetPath = new File(targetDir.getCanonicalPath(), assetName).getCanonicalPath();
            String assetPath = new File(assetMolfileDir, assetName).getPath();
            FileIo.copyAssetToInternalStorage(context, assetPath, targetPath);
        }
    }

    @Override
    protected void onPreExecute() {
        mainActivity.updateStatusString("Importing .pdb file...");
    }

    @Override
    protected Molecule doInBackground(Molecule... molecules) {
        try { initVmd(mainActivity); } catch (IOException e) { e.printStackTrace(); }
        try { initStride(mainActivity); } catch (IOException e) { e.printStackTrace(); }
        try { initVmdAux(mainActivity); } catch (IOException e) { e.printStackTrace(); }
        try { initTclAux(mainActivity); } catch (IOException e) { e.printStackTrace(); }
        try { initMolfilePlugins(mainActivity); } catch (IOException e) { e.printStackTrace(); }

        try {
            Molecule molecule = molecules[0];
            Uri pdbFileUri = molecule.getPdbFileUri();
            File pdbFile = cacheFileFromContentUri(mainActivity, pdbFileUri, ".pdb");
            File objFile = pdbFileToObjFile(mainActivity, pdbFile);
            File glbFile = ObjRenderer.objFileToGlbFile(mainActivity, objFile);
            molecule.setGlbFile(glbFile);
            return molecule;
        } catch (IOException e) {
            Log.e(TAG, null, e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Molecule molecule) {
        mainActivity.updateModelRenderable(
            molecule.getPdb().toString(),
            molecule.getGlbFile()
        );
    }
}
