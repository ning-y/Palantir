package io.ningyuan.palantir.utils;

import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.palantir.SceneformActivity;

public class PdbSearcher extends AsyncTask<String, Void, File> {
    private SceneformActivity sceneformActivity;

    public PdbSearcher(SceneformActivity sceneformActivity) {
        this.sceneformActivity = sceneformActivity;
    }

    @Override
    protected void onPreExecute() {
        sceneformActivity.updateModelNameTextView("Searching...");
    }

    @Override
    protected File doInBackground(String... pdbIds) {
        try {
            String pdbId = pdbIds[0];
            Pdb pdb = new Pdb(pdbId);
            File cacheFile = File.createTempFile(pdbId, ".pdb", sceneformActivity.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            IOUtils.copy(pdb.getInputStream(), outputStream);
            outputStream.close();
            return cacheFile;
        } catch (IOException e) {
            sceneformActivity.updateModelNameTextView("Something went wrong with PDB import.");
        }
        return null;
    }

    @Override
    protected void onPostExecute(File pdbFile) {
        new PdbRenderer(sceneformActivity).execute(FileIo.javaUriToAndroidUri(pdbFile.toURI()));
    }
}
