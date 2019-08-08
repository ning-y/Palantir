package io.ningyuan.palantir;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import io.ningyuan.palantir.fragments.SceneformFragment;
import io.ningyuan.palantir.utils.PdbRenderer;
import io.ningyuan.palantir.views.AboutView;
import io.ningyuan.palantir.views.SearchButton;
import io.ningyuan.palantir.views.SearchView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = String.format("PALANTIR::%s", MainActivity.class.getSimpleName());

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
    }

    public void doRender(String pdbId) {
        new PdbRenderer(this, pdbId).execute();
    }

    public void updateStatusString(String updateTo) {
        statusTextView.setText(updateTo);
    }

    public void updateModelRenderable(String name, File glbFile) {
        sceneformFragment.setModelRenderable(name, glbFile,
                () -> {
                    statusTextView.setText(name);
                });
    }

    public void showAbout() {
        aboutView.show();
    }

    public void hideAbout() {
        aboutView.hide();
    }
}