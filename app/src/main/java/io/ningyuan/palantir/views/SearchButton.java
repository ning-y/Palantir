package io.ningyuan.palantir.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import io.ningyuan.palantir.SceneformActivity;

public class SearchButton extends FloatingActionButton {
    private SceneformActivity sceneformActivity;

    public SearchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.sceneformActivity = (SceneformActivity) context;
        this.setOnClickListener(new OnClickListener());
    }

    /**
     * The {@link android.view.View.OnClickListener} which will automatically be set in the
     * constructor {@link #SearchButton(Context, AttributeSet)}.
     *
     * @see #SearchButton(Context, AttributeSet)
     */
    public class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            sceneformActivity.updateModelNameTextView("RCSB Search is not yet implemented.");
            ((FloatingActionsMenu) getParent()).collapse();
        }
    }
}
