package io.ningyuan.palantir.utils;

import android.app.SearchManager;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.jPdbApi.Query;
import io.ningyuan.palantir.views.SearchView;

public class PdbSearcher extends AsyncTask<String, Void, MatrixCursor> {
    private static final String TAG = String.format("PALANTIR::%s", PdbSearcher.class.getSimpleName());
    private boolean inProgress = false;

    private CursorAdapter adapter;
    private ProgressBar progressBar;

    public PdbSearcher(CursorAdapter adapter, ProgressBar progressBar) {
        this.adapter = adapter;
        this.progressBar = progressBar;
    }

    public boolean isRunning() {
        return inProgress;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute called.");
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        inProgress = true;
    }

    @Override
    protected MatrixCursor doInBackground(String... queryStrings) {
        String queryString = queryStrings[0];

        try {
            Query query = new Query(Query.KEYWORD_QUERY, queryString);
            List<String> results = query.execute();
            MatrixCursor cursor = SearchView.getEmptyCursor(false);
            int index = 1;
            Log.i(TAG, String.format("Start doInBackground for %s", queryString));
            for (String pdbId : results) {
                Log.d(TAG, String.format("Fetching %s for %s", pdbId, queryString));
                Pdb pdb = new Pdb(pdbId);
                try {
                    pdb.load();
                    cursor.addRow(new Object[]{index++, pdb.getStructureId(), pdb.getTitle()});
                } catch (FileNotFoundException e) {
                    Log.e(TAG, String.format("Encountered an exception for %s.", queryString), e);
                }
            }

            return cursor;
        } catch (IOException | ParserConfigurationException e) {
            Log.e(TAG, String.format("Encountered an exception for %s.", queryString), e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(MatrixCursor cursor) {
        inProgress = false;
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }

        if (cursor == null) {
            String[] columns = {
                    BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_TEXT_2
            };
            cursor = new MatrixCursor(columns);
        }

        adapter.changeCursor(cursor);
    }

    @Override
    protected void onCancelled(MatrixCursor cursor) {
        // On cancelled, do nothing. I make this explicit because method used to set the progressBar
        // visibility to View.INVISIBLE. That was not a good idea: when a search was cancelled and
        // another started in its place immediately, a race condition was created. One would expect
        // onCancelled to be called first, setting the progressBar to invisible; then, onPreExecute
        // of the new pdbSearch would set progressBar to View.VISIBLE. In reality, onPreExecute is
        // often called before onCancelled, so the progressBar would not show. Instead, I set the
        // progressBar visibility to View.INVISIBLE explicitly in the SearchView methods.
        Log.d(TAG, "onCancelled called.");
        inProgress = false;
    }
}