package io.ningyuan.palantir.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Convenience methods for showing {@link Toast}s.
 */
public class Toaster {
    /**
     * Show a toast of duration {@link Toast#LENGTH_SHORT}, using an app resource.
     *
     * @param context
     * @param resourceId
     */
    public static void showToastShort(Context context, int resourceId) {
        showToast(context, resourceId, Toast.LENGTH_SHORT);
    }

    /**
     * Show a toast of duration {@link Toast#LENGTH_SHORT}, using a {@link String}.
     *
     * @param context
     * @param message
     */
    public static void showToastShort(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    /**
     * Show a toast of duration {@link Toast#LENGTH_LONG}, using an app resource.
     *
     * @param context
     * @param resourceId
     */
    public static void showToastLong(Context context, int resourceId) {
        showToast(context, resourceId, Toast.LENGTH_LONG);
    }

    /**
     * Show a toast of duration {@link Toast#LENGTH_LONG}, using a {@link String}.
     *
     * @param context
     * @param message
     */
    public static void showToastLong(Context context, String message) {
        showToast(context, message, Toast.LENGTH_LONG);
    }

    private static void showToast(Context context, int resourceId, int duration) {
        Toast toast = Toast.makeText(context, context.getString(resourceId), duration);
        toast.show();
    }

    private static void showToast(Context context, String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
