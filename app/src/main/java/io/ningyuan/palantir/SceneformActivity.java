package io.ningyuan.palantir;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SceneformActivity extends AppCompatActivity {
    private static final String TAG = SceneformActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final float SCALE_HACK_MAX = 0.1f;   // TODO: make this less hacky
    private static final float SCALE_HACK_MIN = 0.05f;  // TODO: make this less hacky
    private static final int IMPORT_GLB_FILE_RESULT = 1;

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (modelRenderable == null) {
                        Toast.makeText(
                                this,
                                "You must first import a *.glb file!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable node and add it to the anchor.
                    TransformableNode transformableNode =
                            new TransformableNode(arFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(modelRenderable);
                    transformableNode.select();

                    // The model normally spawns way too large. As a work-around (i.e. hack),
                    // bound it's scale values to the ones where it looks reasonably large.
                    // TODO: make it less hacky, of course
                    transformableNode.getScaleController().setMaxScale(SCALE_HACK_MAX);
                    transformableNode.getScaleController().setMinScale(SCALE_HACK_MIN);
                });

        final FloatingActionButton importButton = findViewById(R.id.import_button);
        importButton.setOnClickListener(view -> {
            Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            importIntent.addCategory(Intent.CATEGORY_OPENABLE);
            // MIME type for glTF not yet supported; https://issuetracker.google.com/issues/121223582
            importIntent.setType("*/*");

            // Only startActivity if there is a resolvable activity; if not checked, will crash
            if (importIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(importIntent, IMPORT_GLB_FILE_RESULT);
            } else {
                Toast.makeText(
                        this,
                        "Something went wrong: no resolvable activity for importIntent.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    /**
     * Receive a result from the importButton's importIntent i.e. Intent.ACTION_OPEN_DOCUMENT
     *
     * @param requestCode  the request code passed to startActivityForResult i.e. IMPORT_GLB_FILE_RESULT
     * @param resultCode   'exit code' by the external activity; RESULT_OK or RESULT_CANCELED
     * @param resultIntent an Intent carrying data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == IMPORT_GLB_FILE_RESULT && resultCode == RESULT_OK) {
            Uri uri = resultIntent.getData();
            setModelRenderable(uri);
        }
    }

    /**
     * @param uri content URI returned from ACTION_OPEN_DOCUMENT's resulting intent
     *            <p>
     *            So, the URI that is passed here is a content URI from the ACTION_OPEN_DOCUMENT intent.
     *            However, RenderableSource.builder().setSource really only accepts URLs, not URIs (this
     *            restriction is not documented). So, take the content URI, open an inputStream, save it
     *            in this app's cache directory, then pass that new file in the cache to
     *            ReadnerableSource.builder().setSource.
     */
    private void setModelRenderable(Uri uri) {
        try {
            Log.e("setModelRenderable", "Start writing file");
            String cacheFileName = String.valueOf(System.currentTimeMillis());
            File cacheFile = File.createTempFile(cacheFileName, ".glb", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            IOUtils.copy(getContentResolver().openInputStream(uri), outputStream);
            outputStream.close();
            Log.e("setModelRenderable", "Done writing file " + cacheFile.getCanonicalPath());

            ModelRenderable.builder()
                    .setSource(this, RenderableSource.builder().setSource(
                            this,
                            Uri.parse(cacheFile.toURI().toString()),
                            RenderableSource.SourceType.GLB)
                            .setScale(0.5f)
                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                            .build()
                    )
                    .setRegistryId(cacheFileName)
                    .build()
                    .thenAccept(renderable -> modelRenderable = renderable)
                    .exceptionally(
                            throwable -> {
                                Log.e("setModelRenderable", null, throwable);
                                Toast.makeText(
                                        this,
                                        "Unable to load renderable",
                                        Toast.LENGTH_LONG
                                ).show();
                                return null;
                            });
        } catch (IOException e) {
            Log.e("setModelRenderable", "Failed with IOException: " + e.toString());
            Toast.makeText(this, "Unable to import file.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
