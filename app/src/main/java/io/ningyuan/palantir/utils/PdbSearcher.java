package io.ningyuan.palantir.utils;

import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.palantir.MainActivity;

public class PdbSearcher extends AsyncTask<String, Void, File> {
    private MainActivity mainActivity;

    public PdbSearcher(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.updateStatusString("Searching...");
    }

    @Override
    protected File doInBackground(String... queries) {
        try {
            String query = queries[0];
            Pdb pdb = new Pdb(query);
            File cacheFile = File.createTempFile(query, ".pdb", mainActivity.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            IOUtils.copy(pdb.getInputStream(), outputStream);
            outputStream.close();
            return cacheFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(File pdbFile) {
        if (pdbFile != null) {
            new PdbRenderer(mainActivity).execute(FileIo.javaUriToAndroidUri(pdbFile.toURI()));
        } else {
            mainActivity.updateStatusString("Something went wrong.");
        }
    }
}
