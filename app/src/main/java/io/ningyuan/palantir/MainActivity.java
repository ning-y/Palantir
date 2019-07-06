package io.ningyuan.palantir;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.File;

import io.ningyuan.palantir.fragments.SceneformFragment;
import io.ningyuan.palantir.utils.PdbSearcher;
import io.ningyuan.palantir.views.SearchButton;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private SceneformFragment sceneformFragment;
    private SearchView searchView;
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
        progressBar = findViewById(R.id.progress_bar);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = findViewById(R.id.search_view);
        searchView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextFocusChangeListener((View view, boolean hasFocus) -> {
            if (hasFocus) {
                showInputMethod(view.findFocus());
            }
        });

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
            deactivateSearchView();
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

    public void activateSearchView() {
        searchView.setVisibility(View.VISIBLE);
        searchView.requestFocus();
        searchView.requestFocusFromTouch();
    }

    public void deactivateSearchView() {
        searchView.setVisibility(View.GONE);

        // Hide system UI elements: status and navigation bars
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }
}