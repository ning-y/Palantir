package io.ningyuan.palantir;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.io.IOException;

import io.ningyuan.palantir.views.ImportButton;

import static io.ningyuan.palantir.utils.FileIo.cacheFileFromContentUri;
import static io.ningyuan.palantir.utils.FileIo.getFilenameFromContentUri;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_FILE_RESULT;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_MODE_GLB;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_MODE_OBJ;

public class SceneformActivity extends AppCompatActivity {
    private static final String TAG = SceneformActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final float SCALE_HACK_MAX = 0.1f;   // TODO: make this less hacky
    private static final float SCALE_HACK_MIN = 0.05f;  // TODO: make this less hacky

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private String modelName;
    private TextView modelNameTextView;
    private int importMode;

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

        modelNameTextView = findViewById(R.id.model_name);
        updateModelNameTextView();

        final ImportButton importGlbButton = findViewById(R.id.import_glb_button);
        importGlbButton.setImportModeToTrigger(IMPORT_MODE_GLB);
        final ImportButton importObjButton = findViewById(R.id.import_obj_button);
        importObjButton.setImportModeToTrigger(IMPORT_MODE_OBJ);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.setOnTapArPlaneListener(new ArFragmentTapListener());
    }

    public void setImportMode(int importMode) {
        this.importMode = importMode;
    }

    /**
     * Receive a result from the importGlbButton's importIntent i.e. Intent.ACTION_OPEN_DOCUMENT
     *
     * @param requestCode  the request code passed to startActivityForResult i.e. IMPORT_FILE_RESULT
     * @param resultCode   'exit code' by the external activity; RESULT_OK or RESULT_CANCELED
     * @param resultIntent an Intent carrying data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == IMPORT_FILE_RESULT && resultCode == RESULT_OK) {
            switch (importMode) {
                case IMPORT_MODE_GLB:
                    /* Sceneform's RenderableSource.builder cannot yet handle content URIs.
                       See: https://github.com/google-ar/sceneform-android-sdk/issues/477
                       So, extract an InputStream from the content URI, save it as a temp file,
                       and pass the URL (using the file:// scheme) of that temp file instead. */
                    try {
                        Uri contentUri = resultIntent.getData();
                        String filename = getFilenameFromContentUri(this, contentUri);
                        File cacheFile = cacheFileFromContentUri(this, contentUri, ".glb");
                        setModelRenderable(filename, cacheFile);
                    } catch (IOException e) {
                        Log.e(TAG, getString(R.string.log_import_failed_io) + e.toString());
                        showToast(R.string.error_import_failed_io, Toast.LENGTH_LONG);
                    }
                    break;
                case IMPORT_MODE_OBJ:
                    showToast("Wavefront OBJ imports not yet implemented.", Toast.LENGTH_LONG);
            }
        }
    }

    /**
     * Update modelNameTextView to show the current value of modelName.
     */
    private void updateModelNameTextView() {
        // Using String.valueOf accounts for when modelName == null
        modelNameTextView.setText(String.valueOf(modelName));
    }

    /**
     * Set a binary glTF file (*.glb) as the modelRenderable.
     */
    private void setModelRenderable(String filename, File cacheFile) {
        // Weird URI conversions coming up because java.net.URI != android.net.URI
        ModelRenderable.builder()
                .setSource(this, RenderableSource.builder().setSource(
                        this,
                        Uri.parse(cacheFile.toURI().toString()),
                        RenderableSource.SourceType.GLB)
                        .setScale(0.5f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                )
                .setRegistryId(cacheFile.getName())
                .build()
                .thenAccept(renderable -> {
                    modelRenderable = renderable;
                    modelName = filename;
                    updateModelNameTextView();
                })
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, null, throwable);
                            showToast(R.string.error_import_failed_bad_render, Toast.LENGTH_LONG);
                            return null;
                        });
    }

    /**
     * Show a toast with the desired message and duration.
     *
     * @param message  Message to show
     * @param duration Either Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     */
    private void showToast(final String message, final int duration) {
        Toast.makeText(this, message, duration).show();
    }

    /**
     * Show a toast with the desired message and duration.
     *
     * @param resID    Resource ID from string.xml for message
     * @param duration Either Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     */
    private void showToast(final int resID, final int duration) {
        showToast(getString(resID), duration);
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, getString(R.string.error_insufficient_opengl_version));
            showToast(R.string.error_insufficient_opengl_version, Toast.LENGTH_LONG);
            activity.finish();
            return false;
        }
        return true;
    }

    /**
     * OnTapArPlaneListener for arFragment.
     */
    private class ArFragmentTapListener implements BaseArFragment.OnTapArPlaneListener {
        @Override
        public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
            if (modelRenderable == null) {
                showToast(R.string.error_no_model_renderable, Toast.LENGTH_SHORT);
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
        }
    }
}
