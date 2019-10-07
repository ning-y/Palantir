package io.ningyuan.palantir.utils;

import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.jPdbApi.Query;
import io.ningyuan.palantir.views.SearchView;

/**
 * Represents a search instance. For example, at the instance when 'insul' is just keyed into the
 * searchView's EditText, a PdbSearcher instance is formed for 'insul'. However, when 'insuli' is
 * keyed in, the 'insul' PdbSearcher instance is invalidated in favour of the new 'insuli' instance.
 */
public class PdbSearcher extends AsyncTask<String, Void, LinkedList<Pdb>> {
    private static final String TAG = String.format("PALANTIR::%s", PdbSearcher.class.getSimpleName());

    private boolean valid = true;
    private CursorAdapter adapter;
    private ProgressBar progressBar;

    public PdbSearcher(CursorAdapter adapter, ProgressBar progressBar) {
        this.adapter = adapter;
        this.progressBar = progressBar;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * The parent SearchView has been deactivated, or a newer PdbSearcher was spawned.
     */
    public void makeInvalid() {
        valid = false;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute called.");
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected LinkedList<Pdb> doInBackground(String... queryStrings) {
        String queryString = queryStrings[0];

        try {
            Query query = new Query(Query.KEYWORD_QUERY, queryString);
            List<String> results = query.execute();
            LinkedList<Pdb> pdbResults = new LinkedList<>();
            Log.i(TAG, String.format("Start doInBackground for %s", queryString));
            for (String pdbId : results) {
                ((LinkedList<Pdb>) pdbResults).addLast(new Pdb(pdbId));
                // TODO: do this later, after just the pdbIds are passed to UI
                // Log.d(TAG, String.format("Fetching %s for %s", pdbId, queryString));
                // Pdb pdb = new Pdb(pdbId);
                // try {
                //     pdb.load();
                //     cursor.addRow(new Object[]{index++, pdb.getStructureId(), pdb.getTitle()});
                // } catch (FileNotFoundException e) {
                //     Log.e(TAG, String.format("Encountered an exception for %s.", queryString), e);
                // }
            }
            return pdbResults;
        } catch (IOException | ParserConfigurationException e) {
            Log.e(TAG, String.format("Encountered an exception for %s.", queryString), e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(LinkedList<Pdb> results) {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }

        MatrixCursor cursor = SearchView.getEmptyCursor(false, false);
        int cursorIndex = 0;
        for (Pdb pdb : results) {
            cursor.addRow(new Object[]{
                    cursorIndex++, pdb.getStructureId(), pdb.getTitle()
            });
        }
        adapter.changeCursor(cursor);

        new PdbTitleSearcher(this, adapter, results).execute(0);
    }

    @Override
    protected void onCancelled(LinkedList<Pdb> results) {
        // On cancelled, do nothing. I make this explicit because method used to set the progressBar
        // visibility to View.INVISIBLE. That was not a good idea: when a search was cancelled and
        // another started in its place immediately, a race condition was created. One would expect
        // onCancelled to be called first, setting the progressBar to invisible; then, onPreExecute
        // of the new pdbSearch would set progressBar to View.VISIBLE. In reality, onPreExecute is
        // often called before onCancelled, so the progressBar would not show. Instead, I set the
        // progressBar visibility to View.INVISIBLE explicitly in the SearchView methods.
        Log.d(TAG, "onCancelled called.");
        makeInvalid();
    }
}