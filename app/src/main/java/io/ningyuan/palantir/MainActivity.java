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
import io.ningyuan.palantir.utils.FileIo;
import io.ningyuan.palantir.utils.PdbRenderer;
import io.ningyuan.palantir.utils.Toaster;
import io.ningyuan.palantir.views.AboutView;
import io.ningyuan.palantir.views.SearchButton;
import io.ningyuan.palantir.views.SearchView;

import static io.ningyuan.palantir.utils.FileIo.cacheFileFromContentUri;

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
     * Received by onActivityResult.
     */
    public void startImportGlb() {
        Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        importIntent.addCategory(Intent.CATEGORY_OPENABLE);
        importIntent.setType("*/*");

        if (importIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(importIntent, IMPORT_GLB_FILE_RESULT);
        } else {
            Toaster.showToastLong(this, "Error: unable to open the file explorer.");
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

    public void showAbout() {
        aboutView.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == IMPORT_GLB_FILE_RESULT && resultCode == RESULT_OK) {
            Uri contentUri = resultIntent.getData();
            try {
                File glbFile = cacheFileFromContentUri(this, contentUri, ".glb");
                updateModelRenderable("Custom .glb file", glbFile);
            } catch (IOException e) {
                Log.e(TAG, null, e);
                Toaster.showToastLong(this, "Error: unable to open the selected file");
            }
        } else if (requestCode == IMPORT_GLB_FILE_RESULT) {
            Toaster.showToastLong(this, "Error: import failed");
        }
    }

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