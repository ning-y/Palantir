package io.ningyuan.palantir.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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

import io.ningyuan.palantir.R;
import io.ningyuan.palantir.MainActivity;

import static io.ningyuan.palantir.utils.Toaster.showToastLong;

/**
 * {@link ArFragment} extended to automatically register an
 * {@link com.google.ar.sceneform.ux.BaseArFragment.OnTapArPlaneListener}. Also handles the logic
 * for changing the model to be rendered (using {@link #setModelRenderable(String, File, Runnable)}.
 */
public class SceneformFragment extends ArFragment {
    private static final String LOG_TAG = SceneformFragment.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final float SCALE_HACK_MAX = 0.5f;   // TODO: make this less hacky
    private static final float SCALE_HACK_MIN = 0.1f;  // TODO: make this less hacky

    private MainActivity parentActivity;
    private ModelRenderable modelRenderable;

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(LOG_TAG, activity.getString(R.string.error_insufficient_opengl_version));
            showToastLong(activity, R.string.error_insufficient_opengl_version);
            activity.finish();
            return false;
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = super.onCreateView(inflater, container, savedInstanceState);
        this.setOnTapArPlaneListener(new ArFragmentTapListener());
        return inflatedView;
    }

    /**
     * Setter to obtain a reference to the parent activity of this {@link android.app.Fragment},
     * usually the {@link MainActivity}. {@link #parentActivity} is set here in this setter
     * method rather than automatically in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)},
     * because using {@link View#getParent()} in the latter will return null, since the parent is
     * not yet assigned while the {@link android.app.Fragment} is still being 'constructed'.
     *
     * @param activity the activity to set as the {@link #parentActivity}, usually a
     *                 {@link MainActivity}.
     */
    public void setParentActivity(MainActivity activity) {
        this.parentActivity = activity;
    }

    /**
     * Set a binary glTF file (*.glb) as the modelRenderable.
     *
     * @param filename the name of the file which the user has selected. For example, if the
     *                 user navigates and selects through the external content provider a file
     *                 named "5CRO.glb", then the filename here would be "5CRO.glb".
     * @param glbFile  a file accessible to this application (in terms of permissions), which is
     *                 what is to be rendered.
     * @param thenDo   lambda to be called on success or failure
     * @see MainActivity#onActivityResult(int, int, Intent)
     */
    public void setModelRenderable(String filename, File glbFile, Runnable thenDo) {
        // Weird URI conversions coming up because java.net.URI != android.net.URI
        ModelRenderable.builder()
                .setSource(parentActivity, RenderableSource.builder().setSource(
                        parentActivity,
                        Uri.parse(glbFile.toURI().toString()),
                        RenderableSource.SourceType.GLB)
                        .setScale(0.5f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                )
                .setRegistryId(glbFile.getName())
                .build()
                .thenAccept(renderable -> {
                    modelRenderable = renderable;
                    Log.i(LOG_TAG, "modelRenderable set as " + filename);
                    thenDo.run();

                })
                .exceptionally(
                        throwable -> {
                            Log.e(LOG_TAG, null, throwable);
                            showToastLong(parentActivity, R.string.error_import_failed_bad_render);
                            thenDo.run();
                            return null;
                        });
    }

    /**
     * The {@link com.google.ar.sceneform.ux.BaseArFragment.OnTapArPlaneListener} which will
     * automatically be set in the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} method.
     */
    private class ArFragmentTapListener implements BaseArFragment.OnTapArPlaneListener {
        @Override
        public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
            if (modelRenderable == null) {
                showToastLong(parentActivity, R.string.error_no_model_renderable);
            }

            // Create the Anchor.
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(getArSceneView().getScene());

            // Create the transformable node and add it to the anchor.
            TransformableNode transformableNode = new TransformableNode(getTransformationSystem());
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
