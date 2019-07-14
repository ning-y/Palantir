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
            String[] columns = {
                    BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_TEXT_2
            };
            MatrixCursor cursor = new MatrixCursor(columns);
            int index = 1;
            Log.i(TAG, "Start for in doInBackground");
            for (String pdbId : results) {
                Log.i(TAG, pdbId);
                Pdb pdb = new Pdb(pdbId);
                try {
                    pdb.load();
                    cursor.addRow(new Object[]{index++, pdb.getStructureId(), pdb.getTitle()});
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Encountered an exception.", e);
                }
            }

            return cursor;
        } catch (IOException | ParserConfigurationException e) {
            Log.e(TAG, null, e);
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
        inProgress = false;
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}