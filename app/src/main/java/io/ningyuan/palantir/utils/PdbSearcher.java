package io.ningyuan.palantir.utils;

import android.app.SearchManager;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Pair;
import android.widget.CursorAdapter;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.jPdbApi.Query;
import io.ningyuan.palantir.MainActivity;

public class PdbSearcher extends AsyncTask<Pair<CursorAdapter, String>, Void, Pair<CursorAdapter, MatrixCursor>> {
    private static final String TAG = String.format("PALANTIR::%s", PdbSearcher.class.getSimpleName());
    private MainActivity mainActivity;
    private boolean inProgress = false;

    public PdbSearcher(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public boolean isRunning() {
        return inProgress;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.updateStatusString("Searching...");
        inProgress = true;
    }

    @Override
    protected Pair<CursorAdapter, MatrixCursor> doInBackground(Pair<CursorAdapter, String>... cursorAdaptorQueryPair) {
        CursorAdapter cursorAdapter = cursorAdaptorQueryPair[0].first;
        String queryString = cursorAdaptorQueryPair[0].second;

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
                pdb.load();
                cursor.addRow(new Object[]{index++, pdb.getStructureId(), pdb.getTitle()});
            }

            return new Pair<>(cursorAdapter, cursor);
        } catch (IOException | ParserConfigurationException e) {
            Log.e(TAG, null, e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Pair<CursorAdapter, MatrixCursor> adapterCursorPair) {
        inProgress = false;
        if (adapterCursorPair != null) {
            CursorAdapter cursorAdapter = adapterCursorPair.first;
            MatrixCursor cursor = adapterCursorPair.second;
            cursorAdapter.swapCursor(cursor);
        }
    }

    @Override
    protected void onCancelled(Pair<CursorAdapter, MatrixCursor> result) {
        inProgress = false;
    }
}