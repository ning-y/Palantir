package io.ningyuan.palantir.utils;

import android.os.AsyncTask;
import android.widget.Toast;

import io.ningyuan.palantir.SceneformActivity;

/** Async Proof-of-concept */
public class AsyncPOC extends AsyncTask<String, Void, String> {
    private SceneformActivity sceneformActivity;
    private Object lock = new Object();

    public AsyncPOC(SceneformActivity sceneformActivity, Toast fiveSec, Toast tenSec) {
        this.sceneformActivity = sceneformActivity;
    }

    @Override
    protected void onPreExecute() {
        // it seems this does not run, but it does---the modelNameTextView is just immediately
        // overwritten by the catch clause in onActivityResult
        sceneformActivity.updateModelNameTextView("Starting AsyncPOC...");
    }

    @Override
    protected String doInBackground(String... stringParam) {
        // this probably happens in a separate thread---which is why it doesn't freeze the
        // phone-hand animation, and maybe why the toast doesn't work
        try {
            synchronized (lock) {
                lock.wait(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return stringParam[0];
    }

    @Override
    protected void onPostExecute(String output) {
        // i think this happens in the main thread
        // so, when the wait happens, the phone-hand animation freezes,
        // and this is perhaps also why the Toast only works here---maybe toasts are
        // thread restricted
        this.sceneformActivity.updateModelNameTextView(String.format("AsyncPOC done: %s", output));
    }
}
