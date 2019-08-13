package io.ningyuan.palantir.utils;

import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.CursorAdapter;

import java.io.IOException;
import java.util.LinkedList;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.palantir.views.SearchView;

/**
 * Represents the lazy loading of titles for each result in a PdbSearcher.
 */
public class PdbTitleSearcher extends AsyncTask<Integer, Void, Integer> {
    private static final String TAG = String.format("PALANTIR::%s", PdbTitleSearcher.class.getSimpleName());

    private LinkedList<Pdb> pdbs;
    private CursorAdapter adapter;
    private PdbSearcher pdbSearcher;

    public PdbTitleSearcher(PdbSearcher pdbSearcher, CursorAdapter adapter, LinkedList<Pdb> pdbs) {
        this.adapter = adapter;
        this.pdbSearcher = pdbSearcher;
        this.pdbs = pdbs;
    }

    @Override
    protected Integer doInBackground(Integer... integers) {
        int indexToLoad = integers[0];

        if (indexToLoad >= pdbs.size()) {
            return indexToLoad + 1;  // value doesn't matter, will terminate at onPostExecute
        }

        try {
            pdbs.get(indexToLoad).load();
        } catch (IOException e) {
            Log.e(TAG, null, e);
        }

        return indexToLoad + 1;
    }

    @Override
    protected void onPostExecute(Integer nextIndex) {
        MatrixCursor cursor = SearchView.getEmptyCursor(false);
        int cursorIndex = 0;
        for (Pdb pdb : pdbs) {
            cursor.addRow(new Object[]{
                    cursorIndex++, pdb.getStructureId(), pdb.getTitle()
            });
        }
        adapter.changeCursor(cursor);

        Log.d(TAG, String.format("pdbSearcher.isValid()=%b", pdbSearcher.isValid()));
        if (nextIndex < pdbs.size() && pdbSearcher.isValid()) {
            new PdbTitleSearcher(pdbSearcher, adapter, pdbs).execute(nextIndex);
        }
    }
}