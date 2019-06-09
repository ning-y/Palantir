package io.ningyuan.palantir.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import io.ningyuan.palantir.MainActivity;

/**
 * Simply collapses the parent menu, and activates a search dialog on-click.
 */
public class SearchButton extends FloatingActionButton {
    private MainActivity mainActivity;

    public SearchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mainActivity = (MainActivity) context;
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
            ((FloatingActionsMenu) getParent()).collapse();
            mainActivity.onSearchRequested();
        }
    }
}
