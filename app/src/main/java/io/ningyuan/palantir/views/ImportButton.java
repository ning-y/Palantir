package io.ningyuan.palantir.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import io.ningyuan.palantir.R;
import io.ningyuan.palantir.SceneformActivity;

/**
 * {@link FloatingActionButton} extended to automatically register an
 * {@link android.view.View.OnClickListener} which runs the necessary steps to initiate a file
 * (Wavefront OBJ or binary glTF) import.
 */
public class ImportButton extends FloatingActionButton {
    public static final int IMPORT_FILE_RESULT = 1;
    public static final int IMPORT_MODE_GLB = 1;
    public static final int IMPORT_MODE_OBJ = 2;

    private SceneformActivity parentActivity;
    private int importModeToTrigger;

    public ImportButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.parentActivity = (SceneformActivity) context;
        this.setOnClickListener(new OnClickListener());
    }

    /**
     * Determine what value the {@link SceneformActivity#importMode} should take upon click of this
     * button.
     *
     * @param importModeToTrigger the value which {@link SceneformActivity#importMode} should be
     *                            set to upon clock of this button. Either {@link #IMPORT_MODE_GLB}
     *                            or {@link #IMPORT_MODE_OBJ}.
     * @see SceneformActivity#setImportMode(int)
     */
    public void setImportModeToTrigger(int importModeToTrigger) {
        this.importModeToTrigger = importModeToTrigger;
    }

    /**
     * The {@link android.view.View.OnClickListener} which will automatically be set in the
     * constructor {@link #ImportButton}.
     *
     * @see #ImportButton
     */
    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            importIntent.addCategory(Intent.CATEGORY_OPENABLE);
            // MIME type for glTF not yet supported; https://issuetracker.google.com/issues/121223582
            importIntent.setType("*/*");

            // Only startActivity if there is a resolvable activity; if not checked, will crash
            if (importIntent.resolveActivity(parentActivity.getPackageManager()) != null) {
                ((FloatingActionsMenu) getParent()).collapse();
                parentActivity.setImportMode(importModeToTrigger);
                parentActivity.startActivityForResult(importIntent, IMPORT_FILE_RESULT);
            } else {
                Toast toast = Toast.makeText(parentActivity, R.string.error_no_resolvable_activity, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}

