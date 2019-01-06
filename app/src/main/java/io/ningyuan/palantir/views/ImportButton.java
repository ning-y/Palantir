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

public class ImportButton extends FloatingActionButton {
    public static final int IMPORT_FILE_RESULT = 1;
    public static final int IMPORT_MODE_GLB = 1;
    public static final int IMPORT_MODE_OBJ = 2;

    private SceneformActivity parentActivity;
    private Context context;
    private int importModeToTrigger;

    public ImportButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.parentActivity = (SceneformActivity) context;
        this.setOnClickListener(new OnClickListener());
    }

    public void setImportModeToTrigger(int importModeToTrigger) {
        this.importModeToTrigger = importModeToTrigger;
    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            importIntent.addCategory(Intent.CATEGORY_OPENABLE);
            // MIME type for glTF not yet supported; https://issuetracker.google.com/issues/121223582
            importIntent.setType("*/*");

            // Only startActivity if there is a resolvable activity; if not checked, will crash
            if (importIntent.resolveActivity(context.getPackageManager()) != null) {
                ((FloatingActionsMenu) getParent()).collapse();
                parentActivity.setImportMode(importModeToTrigger);
                parentActivity.startActivityForResult(importIntent, IMPORT_FILE_RESULT);
            } else {
                Toast toast = Toast.makeText(context, R.string.error_no_resolvable_activity, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}

