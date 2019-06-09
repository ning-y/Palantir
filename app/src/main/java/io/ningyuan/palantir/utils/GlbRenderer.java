package io.ningyuan.palantir.utils;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;

import io.ningyuan.palantir.MainActivity;

public class GlbRenderer extends AsyncTask<Uri, Void, File> {
    private boolean skipOnPreExecute;
    private MainActivity mainActivity;
    private String modelName;

    public GlbRenderer(MainActivity mainActivity, String modelName) {
        this.mainActivity = mainActivity;
        this.modelName = modelName;
    }

    public GlbRenderer(MainActivity mainActivity, String modelName, Boolean skipOnPreExecute) {
        this(mainActivity, modelName);
        this.skipOnPreExecute = skipOnPreExecute;
    }

    @Override
    protected void onPreExecute() {
        if (!skipOnPreExecute) {
            mainActivity.updateStatusString("Importing .glb file...");
        }
    }

    @Override
    protected File doInBackground(Uri... contentUris) {
        try {
            File glbFile = FileIo.cacheFileFromContentUri(mainActivity, contentUris[0], ".glb");
            return glbFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(File glbFile) {
        if (glbFile == null) {
            Toaster.showToastShort(mainActivity, "Import failed.");
            mainActivity.updateStatusString("Add a model file to begin!");
        } else {
            mainActivity.updateModelRenderable(glbFile.getName(), glbFile);
            mainActivity.updateStatusString(modelName);
        }
    }
}
