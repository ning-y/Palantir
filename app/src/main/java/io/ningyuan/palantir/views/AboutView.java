package io.ningyuan.palantir.views;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
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
        TextView textView = findViewById(R.id.about_textview);
        textView.setText(Html.fromHtml(
                context.getString(R.string.about), Html.FROM_HTML_MODE_LEGACY
        ));
        // Enables interactions with hyperlinks
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void hide() {
        this.setVisibility(GONE);
    }
}
