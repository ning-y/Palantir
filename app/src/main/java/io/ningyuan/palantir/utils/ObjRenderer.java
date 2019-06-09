package io.ningyuan.palantir.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.GltfModels;
import de.javagl.jgltf.model.io.GltfAsset;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.obj.v2.ObjGltfAssetCreatorV2;
import io.ningyuan.palantir.MainActivity;

/**
 * Methods for Wavefront OBJ to binary glTF file conversions.
 */
public class ObjRenderer extends AsyncTask<Uri, Void, File> {
    private MainActivity mainActivity;
    private String modelName;

    public ObjRenderer(MainActivity mainActivity, String modelName) {
        this.mainActivity = mainActivity;
        this.modelName = modelName;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.updateStatusString("Importing .obj file...");
    }

    @Override
    protected File doInBackground(Uri... contentUris) {
        try {
            /* Sceneform's does not support Wavefront OBJ files. Convert them to binary
               glTF (*.glb) first. */
            File objFile = FileIo.cacheFileFromContentUri(mainActivity, contentUris[0], ".obj");
            File glbFile = objFileToGlbFile(mainActivity, objFile);
            return glbFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(File glbFile) {
        new GlbRenderer(mainActivity, modelName, true)
                .execute(FileIo.javaUriToAndroidUri(glbFile.toURI()));
    }

    /**
     * From a Wavefront OBJ {@link File}, save a new binary glTF {@link File}.
     *
     * @param context
     * @param objFile
     * @return the binary glTF {@link File}
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws NoSuchElementException
     */
    public static File objFileToGlbFile(Context context, File objFile) throws IOException, IllegalArgumentException, NoSuchElementException {
        ObjGltfAssetCreatorV2 gltfAssetCreator = new ObjGltfAssetCreatorV2();
        GltfAsset gltfAsset = gltfAssetCreator.create(objFile.toURI());
        GltfModel gltfModel = GltfModels.create(gltfAsset);
        File glbFile = File.createTempFile(objFile.getName(), ".glb", context.getCacheDir());
        GltfModelWriter gltfModelWriter = new GltfModelWriter();
        gltfModelWriter.writeBinary(gltfModel, glbFile);
        return glbFile;
    }
}