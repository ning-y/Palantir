package io.ningyuan.palantir;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import io.ningyuan.palantir.fragments.SceneformFragment;
import io.ningyuan.palantir.utils.PdbRenderer;
import io.ningyuan.palantir.utils.Toaster;
import io.ningyuan.palantir.views.AboutView;
import io.ningyuan.palantir.views.SearchButton;
import io.ningyuan.palantir.views.SearchView;
import static io.ningyuan.palantir.utils.FileIo.cacheFileFromContentUri;
import static io.ningyuan.palantir.utils.FileIo.getFilenameFromContentUri;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = String.format("PALANTIR::%s", MainActivity.class.getSimpleName());
    private static final int IMPORT_GLB_FILE_RESULT = 1;

    private AboutView aboutView;
    private SceneformFragment sceneformFragment;
    private TextView statusTextView;

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
        statusTextView = findViewById(R.id.model_name);
        statusTextView.setText(getString(R.string.ux_model_renderable_not_yet_set));

        aboutView = findViewById(R.id.about);

        SearchView searchView = findViewById(R.id.search_view);
        ProgressBar searchProgressBar = findViewById(R.id.search_progress_bar);
        searchView.setProgressBar(searchProgressBar);
        SearchButton searchButton = findViewById(R.id.search_rcsb_button);
        searchButton.setSearchView(searchView);

        sceneformFragment = (SceneformFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        sceneformFragment.setParentActivity(this);

        setWarningDialog();
    }

    /**
     * Other views call this method to trigger an "import .glb" event. This starts the built-in file
     * browser. The file that is selected by the user is then received by {@link #onActivityResult}.
     */
    public void startImportGlb() {
        Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        importIntent.addCategory(Intent.CATEGORY_OPENABLE);
        importIntent.setType("*/*");

        if (importIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(importIntent, IMPORT_GLB_FILE_RESULT);
        } else {
            Toaster.showToastLong(this, R.string.TOAST_ERROR_NO_ACTIVITY_FOR_IMPORT_INTENT);
        }
    }

    public void doRender(String pdbId) {
        new PdbRenderer(this, pdbId).execute();
    }

    public void updateStatusString(String updateTo) {
        statusTextView.setText(updateTo);
    }

    public void updateModelRenderable(String name, File glbFile) {
        sceneformFragment.setModelRenderable(name, glbFile,
                () -> statusTextView.setText(name));
    }

    /**
     * Shows the full-screen view containing information about Palantir. Usually called from
     * {@see SearchView#setOnSuggestionListener}
     */
    public void showAbout() {
        aboutView.show();
    }

    /** Receives the result of an activity. Used for {@link #startImportGlb()}.
     *
     * @param requestCode reflects with which intent the returning activity was started with. If
     *                    {@link #startImportGlb()}, then {@link #IMPORT_GLB_FILE_RESULT}.
     * @param resultCode {@link #RESULT_OK} if successful.
     * @param resultIntent contains result data from the returning activity.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == IMPORT_GLB_FILE_RESULT && resultCode == RESULT_OK) {
            Uri contentUri = resultIntent.getData();
            try {
                File glbFile = cacheFileFromContentUri(this, contentUri, ".glb");
                updateModelRenderable(
                        String.format("Custom .glb file: %s", getFilenameFromContentUri(this, contentUri)), glbFile);
            } catch (IOException e) {
                Log.e(TAG, null, e);
                Toaster.showToastLong(this, R.string.TOAST_ERROR_IO_EXCEPTION_CACHING_IMPORT_INTENT_RESULT);
            }
        } else if (requestCode == IMPORT_GLB_FILE_RESULT) {
            Toaster.showToastLong(this, R.string.TOAST_ERROR_RESULT_CODE_NOT_OK_FOR_IMPORT_INTENT_RESULT);
        }
    }

    /**
     * Spawns a start-up warning dialog about using AR features safely. A requirement of the Google
     * Play Store.
     */
    private void setWarningDialog() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor preferenceEditor = preferences.edit();

        View warningDialogLayout = getLayoutInflater().inflate(R.layout.warning_dialog, null);
        CheckBox warningDialogCheckbox = warningDialogLayout.findViewById(R.id.warning_dialog_checkbox);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(warningDialogLayout)
                .setPositiveButton("OK.", (DialogInterface di, int which) -> {
                    preferenceEditor.putBoolean(
                            getString(R.string.should_show_warning),
                            !warningDialogCheckbox.isChecked()
                    );
                    preferenceEditor.commit();
                    di.dismiss();
                });

        boolean shouldShowWarning = preferences.getBoolean(getString(R.string.should_show_warning), true);
        if (shouldShowWarning) {
            builder.create().show();
        }
    }
}