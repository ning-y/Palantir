package io.ningyuan.palantir;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import io.ningyuan.palantir.fragments.SceneformFragment;
import io.ningyuan.palantir.views.ImportButton;

import static io.ningyuan.palantir.utils.FileIo.cacheFileFromContentUri;
import static io.ningyuan.palantir.utils.FileIo.getFilenameFromContentUri;
import static io.ningyuan.palantir.utils.Toaster.showToast;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_FILE_RESULT;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_MODE_GLB;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_MODE_OBJ;

public class SceneformActivity extends AppCompatActivity {
    public static final String TAG = SceneformActivity.class.getSimpleName();

    private SceneformFragment sceneformFragment;
    private TextView modelNameTextView;
    private int importMode;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SceneformFragment.checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);

        modelNameTextView = findViewById(R.id.model_name);
        updateModelNameTextView(getString(R.string.ux_model_renderable_not_yet_set));

        final ImportButton importGlbButton = findViewById(R.id.import_glb_button);
        importGlbButton.setImportModeToTrigger(IMPORT_MODE_GLB);
        final ImportButton importObjButton = findViewById(R.id.import_obj_button);
        importObjButton.setImportModeToTrigger(IMPORT_MODE_OBJ);

        sceneformFragment = (SceneformFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        sceneformFragment.setParentActivity(this);
    }

    /**
     * Receive a result from a content provider. In this application, the only case in which this
     * happens is from an {@link ImportButton}'s {@link Intent#ACTION_OPEN_DOCUMENT}.
     *
     * @param requestCode  the request code used in {@link #startActivityForResult(Intent, int)}.
     *                     In this application, this can only be {@link ImportButton#IMPORT_FILE_RESULT}.
     * @param resultCode   'exit code' by the external activity; either {@link #RESULT_OK} or
     *                     {@link #RESULT_CANCELED}.
     * @param resultIntent an {@link Intent} carrying data from the returning activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode != IMPORT_FILE_RESULT || resultCode != RESULT_OK) {
            return;
        }

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
                    sceneformFragment.setModelRenderable(filename, cacheFile);
                } catch (IOException e) {
                    Log.e(TAG, getString(R.string.log_import_failed_io) + e.toString());
                    showToast(this, R.string.error_import_failed_io, Toast.LENGTH_LONG);
                }
                break;
            case IMPORT_MODE_OBJ:
                showToast(this, "Wavefront OBJ imports not yet implemented.", Toast.LENGTH_LONG);
        }
    }

    /**
     * Sets the {@link #importMode} of this activity. The {@link #importMode} determines the
     * behaviour of {@link #onActivityResult(int, int, Intent)} when a result {@link Intent} is
     * received by this activity.
     *
     * {@link ImportButton}s will call this method to 'prepare' this activity to handle their
     * respective import types. For example, the import button for glb files will ready this
     * activity to handle glb files; likewise, the import obj button prepares the activity to handle
     * obj files.
     *
     * @param importMode the import mode to set. Either {@link ImportButton#IMPORT_MODE_GLB} or
     *                   {@link ImportButton#IMPORT_MODE_OBJ}.
     */
    public void setImportMode(int importMode) {
        this.importMode = importMode;
    }

    /**
     * Updates the value of the {@link #modelNameTextView}. The {@link #modelNameTextView} is a UX
     * element which indicates what model will be spawned by the {@link SceneformFragment}.
     *
     * @param newModelName the new value for the {@link #modelNameTextView}.
     */
    public void updateModelNameTextView(String newModelName) {
        modelNameTextView.setText(newModelName);
    }
}