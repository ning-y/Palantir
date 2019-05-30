package io.ningyuan.palantir;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import io.ningyuan.palantir.fragments.SceneformFragment;
import io.ningyuan.palantir.utils.FileIo;
import io.ningyuan.palantir.utils.GlbRenderer;
import io.ningyuan.palantir.utils.ObjRenderer;
import io.ningyuan.palantir.utils.PdbRenderer;
import io.ningyuan.palantir.utils.Toaster;
import io.ningyuan.palantir.views.ImportButton;
import io.ningyuan.palantir.views.SearchButton;

import static io.ningyuan.palantir.views.ImportButton.IMPORT_FILE_RESULT;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_MODE_GLB;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_MODE_OBJ;
import static io.ningyuan.palantir.views.ImportButton.IMPORT_MODE_PDB;

public class SceneformActivity extends AppCompatActivity {
    public static final String TAG = SceneformActivity.class.getSimpleName();

    private ProgressBar progressBar;
    public SceneformFragment sceneformFragment;  // TODO: make private
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
        modelNameTextView.setText(getString(R.string.ux_model_renderable_not_yet_set));
        progressBar = findViewById(R.id.progress_bar);

        final ImportButton importGlbButton = findViewById(R.id.import_glb_button);
        importGlbButton.setImportModeToTrigger(IMPORT_MODE_GLB);
        final ImportButton importObjButton = findViewById(R.id.import_obj_button);
        importObjButton.setImportModeToTrigger(IMPORT_MODE_OBJ);
        final ImportButton importPdbButton = findViewById(R.id.import_pdb_button);
        importPdbButton.setImportModeToTrigger(IMPORT_MODE_PDB);
        final SearchButton searchRcsbButton = findViewById(R.id.import_rcsb_button);

        sceneformFragment = (SceneformFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        sceneformFragment.setParentActivity(this);
    }

    /**
     * Receive a result from a content provider. In this application, the only case in which this
     * happens is from an {@link ImportButton}'s {@link #startActivityForResult(Intent, int)} using
     * {@link Intent#ACTION_OPEN_DOCUMENT}.
     *
     * @param requestCode  the request code used in {@link #startActivityForResult(Intent, int)}.
     *                     In this application, this can only be {@link ImportButton#IMPORT_FILE_RESULT}.
     * @param resultCode   'exit code' by the external activity; either {@link #RESULT_OK} or
     *                     {@link #RESULT_CANCELED}.
     * @param resultIntent an {@link Intent} carrying data from the returning activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        Log.d(TAG, String.format(
                "onActivityResult called with requestCode %d; resultCode %d; importMode %d",
                requestCode, resultCode, importMode
        ));

        if (requestCode != IMPORT_FILE_RESULT || resultCode != RESULT_OK) {
            return;
        }

        String lastModelName = modelNameTextView.getText().toString();
        modelNameTextView.setText("Loading...");
        progressBar.setVisibility(View.VISIBLE);

        Uri contentUri = resultIntent.getData();
        String filename = FileIo.getFilenameFromContentUri(this, contentUri);
        File glbFile = null;

        try {
            switch (importMode) {
                case IMPORT_MODE_GLB:
                    /* Sceneform's RenderableSource.builder cannot yet handle content URIs.
                       See: https://github.com/google-ar/sceneform-android-sdk/issues/477
                       So, extract an InputStream from the content URI, save it as a temp file,
                       and pass the URL (using the file:// scheme) of that temp file instead.
                       All these are abstracted in FileIo.cacheFileFromContentUri and GlbRenderer.*/
                    new GlbRenderer(this, filename).execute(contentUri);
                    return;
                case IMPORT_MODE_OBJ:
                    new ObjRenderer(this, filename).execute(contentUri);
                    return;
                case IMPORT_MODE_PDB:
                    new PdbRenderer(this).execute(contentUri);
                    // unlike other two modes, PdbRenderer takes care of setting modelRenderable
                    return;
            }

            if (glbFile != null) {
                sceneformFragment.setModelRenderable(filename, glbFile,
                        () -> {
                            modelNameTextView.setText(filename);
                            progressBar.setVisibility(View.INVISIBLE);
                        });
            } else {
                // ImportButton must use either IMPORT_MODE_GLB or IMPORT_MODE_OBJ, so glbFile
                // should always be non-null
                throw new IllegalStateException();
            }

        } catch (IllegalStateException e) {
            Log.e(TAG, getString(R.string.log_import_failed_io) + e.toString());
            Toaster.showToastLong(this, R.string.error_import_failed_io);
            modelNameTextView.setText(lastModelName);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Sets the {@link #importMode} of this activity. The {@link #importMode} determines the
     * behaviour of {@link #onActivityResult(int, int, Intent)} when a result {@link Intent} is
     * received by this activity.
     * <p>
     * {@link ImportButton}s will call this method to 'prepare' this activity to handle their
     * respective import types. For example, the import button for glb files will ready this
     * activity to handle glb files; likewise, the import obj button prepares the activity to handle
     * obj files.
     *
     * @param importMode the import mode to set. Either {@link ImportButton#IMPORT_MODE_GLB} or
     *                   {@link ImportButton#IMPORT_MODE_OBJ}.
     * @see ImportButton#setImportModeToTrigger(int)
     */
    public void setImportMode(int importMode) {
        this.importMode = importMode;
    }

    public void updateModelNameTextView(String updateTo) {
        modelNameTextView.setText(updateTo);
    }

    public void updateModelRenderable(String name, File glbFile) {
        sceneformFragment.setModelRenderable(name, glbFile,
                () -> {
                    modelNameTextView.setText(name);
                    progressBar.setVisibility(View.INVISIBLE);
                });
    }
}