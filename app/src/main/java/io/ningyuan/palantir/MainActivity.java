package io.ningyuan.palantir;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.ningyuan.palantir.fragments.SceneformFragment;
import io.ningyuan.palantir.utils.PdbSearcher;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private SceneformFragment sceneformFragment;
    private SearchView searchView;
    private TextView statusTextView;

    private PdbSearcher pdbSearcher = new PdbSearcher(this);

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
        // TODO remove this searchable trash
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextFocusChangeListener((View view, boolean hasFocus) -> {
            if (hasFocus) {
                showInputMethod(view.findFocus());
            }
        });

        final CursorAdapter suggestionAdaptor = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_2, null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2},
                new int[]{android.R.id.text1, android.R.id.text2}, 0);
        final List<String> suggestions = new ArrayList<>();
        searchView.setSuggestionsAdapter(suggestionAdaptor);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                String query = suggestions.get(i);
                deactivateSearchView();
                // TODO startRender(query);

                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                doSearch(suggestionAdaptor, s);
                return false;
            }
        });

        sceneformFragment = (SceneformFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        sceneformFragment.setParentActivity(this);
    }

    protected void doSearch(CursorAdapter cursorAdapter, String query) {
        if (query.length() < 3) {
            pdbSearcher.cancel(true);
            String[] columns = {
                    BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_TEXT_2
            };
            MatrixCursor cursor = new MatrixCursor(columns);
            cursorAdapter.swapCursor(cursor);
            return;
        }

        if (pdbSearcher.isRunning()) {
            pdbSearcher.cancel(true);
        }

        pdbSearcher = new PdbSearcher(this);
        pdbSearcher.execute(new Pair<>(cursorAdapter, query));
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