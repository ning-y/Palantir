package io.ningyuan.palantir.views;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import io.ningyuan.palantir.MainActivity;
import io.ningyuan.palantir.utils.PdbRenderer;
import io.ningyuan.palantir.utils.PdbSearcher;

public class SearchView extends android.widget.SearchView {
    private static final String TAG = String.format("PALANTIR::%s", SearchView.class.getSimpleName());
    private static final Object[] ABOUT_ROW = new Object[]{Integer.MAX_VALUE, "About", "About this program"};
    private static final Object[] IMPORT_GLB_ROW = new Object[]{Integer.MAX_VALUE, "Import .glb", "Import a binary glTF file"};

    CursorAdapter suggestionAdapter;
    MainActivity mainActivity;
    PdbSearcher pdbSearcher;
    ProgressBar progressBar;

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.suggestionAdapter = new SimpleCursorAdapter(
                context, android.R.layout.simple_list_item_2, getEmptyCursor(),
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2},
                new int[]{android.R.id.text1, android.R.id.text2}, 0
        );
        this.mainActivity = (MainActivity) context;
        this.pdbSearcher = new PdbSearcher(this.suggestionAdapter, null);
        this.setSuggestionsAdapter(this.suggestionAdapter);

        setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setQueryHint("Enter a PDB search query");
        setAutocompleteThreshold(0);
        setOnCloseListener();
        setOnSuggestionListener();
        setOnQueryTextListener();
        Log.i(TAG, "the constructor was called");
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.pdbSearcher = new PdbSearcher(suggestionAdapter, progressBar);
        progressBar.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE);  // TODO: does nothing; fix
    }

    public static MatrixCursor getEmptyCursor() {
        return getEmptyCursor(true, true);
    }

    public static MatrixCursor getEmptyCursor(boolean shouldAddAbout, boolean shouldAddImportGlb) {
        String[] columns = {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2
        };
        MatrixCursor cursor = new MatrixCursor(columns);

        // I explicitly want the import row to show up after the about row.
        if (shouldAddAbout) cursor.addRow(ABOUT_ROW);
        if (shouldAddImportGlb) cursor.addRow(IMPORT_GLB_ROW);

        return cursor;
    }

    public void activate() {
        setVisibility(VISIBLE);
        requestFocus();
        requestFocusFromTouch();
        suggestionAdapter.changeCursor(getEmptyCursor());
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(this.findFocus(), 0);
        }
    }

    private void setAutocompleteThreshold(int threshold) {
        AutoCompleteTextView search_text = this.findViewById(this.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        search_text.setThreshold(threshold);
    }

    private void setOnCloseListener() {
        // FIXME: The soft keyboard does not close if user presses the 'X' to close the search view.
        //        Any input on the soft keyboard will then show its appropriate search suggestions---but
        //        with the search view being "GONE" throughout.
        setOnCloseListener(() -> {
            deactivate();
            return false;
        });
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
                if (query.equals(ABOUT_ROW[1])) {
                    mainActivity.showAbout();
                } else if (query.equals(IMPORT_GLB_ROW[1])) {
                    mainActivity.startImportGlb();
                } else {
                    mainActivity.doRender(query);
                }
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
                Log.d(TAG, String.format("onQueryTextChange found \"%s\"", s));
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
        // Clear the soft keyboard
        clearFocus();
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }

        pdbSearcher.makeInvalid();
        setVisibility(GONE);
        progressBar.setVisibility(GONE);
        suggestionAdapter.changeCursor(getEmptyCursor(false, false));
        View decorView = mainActivity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    private void doSearch(String query) {
        Log.d(TAG, String.format("doSearch found \"%s\"", query));
        if (query.length() < 3) {
            MatrixCursor cursor = getEmptyCursor();
            suggestionAdapter.changeCursor(cursor);
        } else {
            pdbSearcher.cancel(true);
            pdbSearcher.makeInvalid();  // pdbSearcher.onCancelled not called if doInBackground already complete
            pdbSearcher = new PdbSearcher(suggestionAdapter, progressBar);
            Log.i(TAG, String.format("Starting search with %s", query));
            pdbSearcher.execute(query);
        }
    }
}