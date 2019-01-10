package io.ningyuan.palantir.utils;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.GltfModels;
import de.javagl.jgltf.model.io.GltfAsset;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.obj.v2.ObjGltfAssetCreatorV2;

/**
 * Methods for Wavefront OBJ to binary glTF file conversions.
 */
public class ObjToGlb {
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