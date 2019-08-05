package io.ningyuan.palantir.views;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.ningyuan.palantir.R;

public class AboutView extends RelativeLayout {
    Context context;

    public AboutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void show() {
        this.setVisibility(VISIBLE);

        // TextView.setMovementMethod seems to prevent an ImageButton set (overlapping) on top of it
        // from receiving clicks. So, I've split the title and body of the About page into two
        // separate TextViews. The body will have its 'movement method' set appropriate for its
        // hyperlinks to be interact-able; but the title, which has no such need, will have its
        // movement method untouched so that the ImageButton closeButton can receive clicks.
        TextView titleTextView = findViewById(R.id.about_title);
        titleTextView.setText(Html.fromHtml(
                context.getString(R.string.about_title), Html.FROM_HTML_MODE_LEGACY
        ));

        TextView bodyTextView = findViewById(R.id.about_body);
        bodyTextView.setText(Html.fromHtml(
                context.getString(R.string.about_body), Html.FROM_HTML_MODE_LEGACY
        ));
        bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());

        ImageButton closeButton = findViewById(R.id.about_close);
        closeButton.setOnClickListener((View v) -> hide());
    }

    public void hide() {
        this.setVisibility(GONE);
    }
}
