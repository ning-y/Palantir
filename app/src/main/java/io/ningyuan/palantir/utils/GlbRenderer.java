package io.ningyuan.palantir.utils;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;

import io.ningyuan.palantir.SceneformActivity;

public class GlbRenderer extends AsyncTask<Uri, Void, File> {
    private SceneformActivity sceneformActivity;
    private String modelName;

    public GlbRenderer (SceneformActivity sceneformActivity, String modelName) {
        this.sceneformActivity = sceneformActivity;
        this.modelName = modelName;
    }

    @Override
    protected void onPreExecute() {
        sceneformActivity.updateModelNameTextView("Importing .glb file...");
    }

    @Override
    protected File doInBackground(Uri... contentUris) {
        try {
            File glbFile = FileIo.cacheFileFromContentUri(sceneformActivity, contentUris[0], ".glb");
            return glbFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(File glbFile) {
        if (glbFile == null) {
            Toaster.showToastShort(sceneformActivity, "Import failed.");
            sceneformActivity.updateModelNameTextView("Add a model file to begin!");
        } else {
            sceneformActivity.updateModelRenderable(glbFile.getName(), glbFile);
            sceneformActivity.updateModelNameTextView(modelName);
        }
    }
}
