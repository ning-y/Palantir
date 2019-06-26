package io.ningyuan.palantir.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.jPdbApi.Query;
import io.ningyuan.palantir.MainActivity;
import io.ningyuan.palantir.models.Molecule;

public class PdbSearcher extends AsyncTask<String, Void, Molecule> {
    private static final String TAG = String.format("PALANTIR::%s", PdbSearcher.class.getSimpleName());
    private MainActivity mainActivity;

    public PdbSearcher(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.updateStatusString("Searching...");
    }

    @Override
    protected Molecule doInBackground(String... queries) {
        try {
            String keywords = queries[0];
            Query query = new Query(Query.KEYWORD_QUERY, keywords);
            List<String> results = query.execute();

            String pdbId = results.get(0);
            Pdb pdb = new Pdb(pdbId);
            File cacheFile = File.createTempFile(pdbId.toUpperCase(), ".pdb", mainActivity.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            IOUtils.copy(pdb.getInputStream(), outputStream);
            outputStream.close();

            pdb.load();
            Molecule result = new Molecule();
            result.setPdb(pdb);
            result.setPdbFileUri(cacheFile.toURI());

            return result;
        } catch (IOException | ParserConfigurationException e) {
            Log.e(TAG, null, e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Molecule result) {
        if (result != null) {
            new PdbRenderer(mainActivity).execute(result);
        } else {
            mainActivity.updateStatusString("Something went wrong.");
        }
    }
}
