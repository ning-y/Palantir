package io.ningyuan.palantir;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import io.ningyuan.palantir.fragments.SceneformFragment;
import io.ningyuan.palantir.utils.PdbSearcher;
import io.ningyuan.palantir.views.SearchButton;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private SceneformFragment sceneformFragment;
    private TextView statusTextView;
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
        statusTextView = findViewById(R.id.model_name);
        statusTextView.setText(getString(R.string.ux_model_renderable_not_yet_set));
        progressBar = findViewById(R.id.progress_bar);

        sceneformFragment = (SceneformFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        sceneformFragment.setParentActivity(this);
    }


    /**
     * Receive an intent. In this application, the only case in which this happens is from a
     * {@link SearchButton}'s {@link MainActivity#onSearchRequested()}.
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new PdbSearcher(this).execute(query);
        }
    }

    public void updateStatusString(String updateTo) {
        statusTextView.setText(updateTo);
    }

    public void updateModelRenderable(String name, File glbFile) {
        sceneformFragment.setModelRenderable(name, glbFile,
                () -> {
                    statusTextView.setText(name);
                    progressBar.setVisibility(View.INVISIBLE);
                });
    }
}