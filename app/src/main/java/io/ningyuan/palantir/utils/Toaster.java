package io.ningyuan.palantir.utils;

import android.content.Context;
import android.widget.Toast;

public class Toaster {
    public static void showToast(Context context, int resourceId, int duration) {
        Toast toast = Toast.makeText(context, context.getString(resourceId), duration);
        toast.show();
    }

    public static void showToast(Context context, String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
