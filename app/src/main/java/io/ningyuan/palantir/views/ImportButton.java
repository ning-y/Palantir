package io.ningyuan.palantir.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import io.ningyuan.palantir.R;
import io.ningyuan.palantir.MainActivity;
import io.ningyuan.palantir.utils.Toaster;

/**
 * {@link FloatingActionButton} extended to automatically register an
 * {@link android.view.View.OnClickListener} which runs the necessary steps to initiate a file
 * (Wavefront OBJ, binary glTF or PDB) import.
 */
public class ImportButton extends FloatingActionButton {
    public static final int IMPORT_FILE_RESULT = 1;
    public static final int IMPORT_MODE_GLB = 1;
    public static final int IMPORT_MODE_OBJ = 2;
    public static final int IMPORT_MODE_PDB = 3;

    private int importModeToTrigger;
    private MainActivity mainActivity;

    public ImportButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mainActivity = (MainActivity) context;
        this.setOnClickListener(new OnClickListener());
    }

    /**
     * Determine what value the {@link MainActivity#importMode} should take upon click of this
     * button.
     *
     * @param importModeToTrigger the value which {@link MainActivity#importMode} should be
     *                            set to upon clock of this button. Either {@link #IMPORT_MODE_GLB},
     *                            {@link #IMPORT_MODE_OBJ}, or {@link #IMPORT_MODE_PDB}.
     * @see MainActivity#setImportMode(int)
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
    public class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            importIntent.addCategory(Intent.CATEGORY_OPENABLE);
            // MIME type for glTF not yet supported; https://issuetracker.google.com/issues/121223582
            importIntent.setType("*/*");

            // Only startActivity if there is a resolvable activity; if not checked, will crash
            if (importIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
                ((FloatingActionsMenu) getParent()).collapse();
                mainActivity.setImportMode(importModeToTrigger);
                mainActivity.startActivityForResult(importIntent, IMPORT_FILE_RESULT);
            } else {
                Toaster.showToastLong(mainActivity, R.string.error_no_resolvable_activity);
            }
        }
    }
}

