package io.ningyuan.palantir.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import io.ningyuan.palantir.R;
import io.ningyuan.palantir.SceneformActivity;

import static io.ningyuan.palantir.utils.Toaster.showToast;

public class SceneformFragment extends ArFragment {
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final float SCALE_HACK_MAX = 0.1f;   // TODO: make this less hacky
    private static final float SCALE_HACK_MIN = 0.05f;  // TODO: make this less hacky

    private SceneformActivity parentActivity;
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
            Log.e(SceneformActivity.TAG, activity.getString(R.string.error_insufficient_opengl_version));
            showToast(activity, R.string.error_insufficient_opengl_version, Toast.LENGTH_LONG);
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

    public void setParentActivity(SceneformActivity activity) {
        this.parentActivity = activity;
    }

    /**
     * Set a binary glTF file (*.glb) as the modelRenderable.
     */
    public void setModelRenderable(String filename, File cacheFile) {
        // Weird URI conversions coming up because java.net.URI != android.net.URI
        ModelRenderable.builder()
                .setSource(parentActivity, RenderableSource.builder().setSource(
                        parentActivity,
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
                    parentActivity.updateModelNameTextView(filename);
                })
                .exceptionally(
                        throwable -> {
                            Log.e(SceneformActivity.TAG, null, throwable);
                            showToast(parentActivity, R.string.error_import_failed_bad_render, Toast.LENGTH_LONG);
                            return null;
                        });
    }

    private class ArFragmentTapListener implements BaseArFragment.OnTapArPlaneListener {
        @Override
        public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
            if (modelRenderable == null) {
                showToast(parentActivity, R.string.error_no_model_renderable, Toast.LENGTH_LONG);
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
