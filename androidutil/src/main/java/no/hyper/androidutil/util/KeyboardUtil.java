package no.hyper.androidutil.util;

import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {
    public static void forceOpen(Context context, IBinder windowToken) {
        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(windowToken ,InputMethodManager.SHOW_FORCED, 0);
    }

    public static void forceClose(Context context, IBinder windowToken) {
        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
    }
}
