package io.ningyuan.palantir.utils;

import android.app.SearchManager;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.CursorAdapter;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.jPdbApi.Query;

public class PdbSearcher extends AsyncTask<String, Void, MatrixCursor> {
    private static final String TAG = String.format("PALANTIR::%s", PdbSearcher.class.getSimpleName());
    private boolean inProgress = false;
    private CursorAdapter adapter;

    public PdbSearcher(CursorAdapter adapter) {
        this.adapter = adapter;
    }

    public boolean isRunning() {
        return inProgress;
    }

    @Override
    protected void onPreExecute() {
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
            for (String pdbId : results) {
                Pdb pdb = new Pdb(pdbId);
                try {
                    pdb.load();
                    cursor.addRow(new Object[]{index++, pdb.getStructureId(), pdb.getTitle()});
                } catch (IOException e) {
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

        if (cursor == null) {
            String[] columns = {
                    BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_TEXT_2
            };
            cursor = new MatrixCursor(columns);
        }

        adapter.swapCursor(cursor);
    }

    @Override
    protected void onCancelled(MatrixCursor cursor) {
        inProgress = false;
    }
}