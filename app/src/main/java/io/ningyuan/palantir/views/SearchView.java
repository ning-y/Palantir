package io.ningyuan.palantir.views;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import io.ningyuan.palantir.MainActivity;
import io.ningyuan.palantir.utils.PdbRenderer;
import io.ningyuan.palantir.utils.PdbSearcher;

public class SearchView extends android.widget.SearchView {
    private static final String TAG = String.format("PALANTIR::%s", SearchView.class.getSimpleName());

    CursorAdapter suggestionAdapter;
    MainActivity mainActivity;
    PdbSearcher pdbSearcher;

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.suggestionAdapter = new SimpleCursorAdapter(
                context, android.R.layout.simple_list_item_2, null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2},
                new int[]{android.R.id.text1, android.R.id.text2}, 0
        );
        this.mainActivity = (MainActivity) context;
        this.pdbSearcher = new PdbSearcher(this.suggestionAdapter);
        this.setSuggestionsAdapter(this.suggestionAdapter);

        this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setOnSuggestionListener();
        setOnQueryTextListener();
        Log.i(TAG, "the constructor was called");
    }

    public void activate() {
        this.setVisibility(VISIBLE);
        this.requestFocus();
        this.requestFocusFromTouch();
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(this.findFocus(), 0);
        }
    }

    private void setOnSuggestionListener() {
        setOnSuggestionListener(new android.widget.SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                suggestionAdapter.getCursor().moveToPosition(i);
                String query = suggestionAdapter.getCursor().getString(1);
                deactivate();
                mainActivity.doRender(query);
                return false;
            }
        });
    }

    private void setOnQueryTextListener() {
        setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                deactivate();
                new PdbRenderer(mainActivity, s).execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() == 0) {
                    deactivate();
                } else {
                    doSearch(s);
                }
                return false;
            }
        });
    }

    private void deactivate() {
        this.setVisibility(GONE);
        View decorView = mainActivity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    private void doSearch(String query) {
        if (query.length() < 3) {
            String[] columns = {
                    BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_TEXT_2
            };
            MatrixCursor cursor = new MatrixCursor(columns);
            suggestionAdapter.swapCursor(cursor);
        } else {
            pdbSearcher.cancel(true);
            pdbSearcher = new PdbSearcher(suggestionAdapter);
            Log.e(TAG, String.format("Starting search with %s", query));
            pdbSearcher.execute(query);
        }
    }
}